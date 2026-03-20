package com.foyer.gestion

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.foyer.gestion.worker.ExpirationNotificationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class GestionFoyerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ExpirationNotificationWorker.createNotificationChannel(this)
        scheduleExpirationCheck()
    }

    private fun scheduleExpirationCheck() {
        val workRequest = PeriodicWorkRequestBuilder<ExpirationNotificationWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expiration_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
