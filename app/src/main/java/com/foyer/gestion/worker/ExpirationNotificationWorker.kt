package com.foyer.gestion.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.foyer.gestion.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

class ExpirationNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "frigo_expiration"
        const val CHANNEL_NAME = "Expiration aliments"

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alertes d'expiration des aliments du frigo"
                }
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()
            val firestore = FirebaseFirestore.getInstance()

            // Récupérer le foyerId de l'utilisateur
            val userDoc = firestore.collection("utilisateurs").document(uid).get().await()
            val foyerId = userDoc.getString("foyerId") ?: return Result.success()

            // Récupérer les articles du frigo
            val articles = firestore.collection("foyers")
                .document(foyerId)
                .collection("frigo")
                .get()
                .await()

            val now = Date()
            val in3Days = Date(now.time + TimeUnit.DAYS.toMillis(3))
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var notifId = 1000

            articles.documents.forEach { doc ->
                val nom = doc.getString("nom") ?: return@forEach
                val dateExpTs = doc.getTimestamp("dateExpiration") ?: return@forEach
                val dateExp = dateExpTs.toDate()

                if (dateExp.after(now) && dateExp.before(in3Days)) {
                    val diffMs = dateExp.time - now.time
                    val jours = TimeUnit.MILLISECONDS.toDays(diffMs).toInt()

                    val message = when {
                        jours <= 0 -> "$nom expire aujourd'hui !"
                        jours == 1 -> "$nom expire demain !"
                        else -> "$nom expire dans $jours jours"
                    }

                    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("⚠️ Aliment bientôt périmé")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .build()

                    notificationManager.notify(notifId++, notification)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
