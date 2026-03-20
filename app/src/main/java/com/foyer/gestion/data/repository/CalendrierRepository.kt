package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.Evenement
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendrierRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun evtsRef(foyerId: String) =
        firestore.collection("foyers").document(foyerId).collection("evenements")

    fun getEvenementsFlow(foyerId: String): Flow<List<Evenement>> = callbackFlow {
        val listener = evtsRef(foyerId)
            .orderBy("dateDebut", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toObject(Evenement::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun creerEvenement(foyerId: String, titre: String, description: String, dateDebut: Timestamp, dateFin: Timestamp?): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val evt = Evenement(UUID.randomUUID().toString(), foyerId, titre, description, dateDebut, dateFin, "BLUE", uid)
            evtsRef(foyerId).document(evt.id).set(evt).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun supprimerEvenement(foyerId: String, evtId: String): Result<Unit> {
        return try {
            evtsRef(foyerId).document(evtId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
