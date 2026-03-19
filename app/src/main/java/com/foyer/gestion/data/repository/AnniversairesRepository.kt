package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.Anniversaire
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
class AnniversairesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun annivsRef(foyerId: String) =
        firestore.collection("foyers").document(foyerId).collection("anniversaires")

    fun getAnniversairesFlow(foyerId: String): Flow<List<Anniversaire>> = callbackFlow {
        val listener = annivsRef(foyerId)
            .orderBy("ajouteLe", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toObject(Anniversaire::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun ajouterAnniversaire(foyerId: String, prenom: String, nom: String, dateNaissance: Timestamp, emoji: String, note: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val anniv = Anniversaire(UUID.randomUUID().toString(), foyerId, prenom, nom, dateNaissance, emoji, note, uid)
            annivsRef(foyerId).document(anniv.id).set(anniv).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun supprimerAnniversaire(foyerId: String, anniversaireId: String): Result<Unit> {
        return try {
            annivsRef(foyerId).document(anniversaireId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
