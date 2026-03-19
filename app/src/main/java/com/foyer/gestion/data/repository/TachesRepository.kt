package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.StatutTache
import com.foyer.gestion.data.model.Tache
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
class TachesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun tachesRef(foyerId: String) =
        firestore.collection("foyers").document(foyerId).collection("taches")

    fun getTachesFlow(foyerId: String): Flow<List<Tache>> = callbackFlow {
        val listener = tachesRef(foyerId)
            .orderBy("creeLe", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val taches = snap?.documents?.mapNotNull { it.toObject(Tache::class.java) }
                trySend(taches ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun creerTache(
        foyerId: String,
        titre: String,
        description: String,
        priorite: String,
        assigneA: String,
        echeance: Timestamp?
    ): Result<Tache> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val tache = Tache(
                id = UUID.randomUUID().toString(),
                foyerId = foyerId,
                titre = titre,
                description = description,
                priorite = priorite,
                assigneA = assigneA,
                creePar = uid,
                echeance = echeance
            )
            tachesRef(foyerId).document(tache.id).set(tache).await()
            Result.success(tache)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun changerStatut(
        foyerId: String,
        tacheId: String,
        statut: StatutTache
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("statut" to statut.name)
            if (statut == StatutTache.TERMINEE) {
                updates["termineLe"] = Timestamp.now()
            }
            tachesRef(foyerId).document(tacheId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun supprimerTache(foyerId: String, tacheId: String): Result<Unit> {
        return try {
            tachesRef(foyerId).document(tacheId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun modifierTache(foyerId: String, tache: Tache): Result<Unit> {
        return try {
            tachesRef(foyerId).document(tache.id).set(tache).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
