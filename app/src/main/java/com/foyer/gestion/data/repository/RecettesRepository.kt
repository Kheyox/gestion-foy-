package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.Recette
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
class RecettesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun recettesRef(foyerId: String) =
        firestore.collection("foyers").document(foyerId).collection("recettes")

    fun getRecettesFlow(foyerId: String): Flow<List<Recette>> = callbackFlow {
        val listener = recettesRef(foyerId)
            .orderBy("ajouteLe", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toObject(Recette::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun creerRecette(foyerId: String, titre: String, ingredients: String, instructions: String, dureeMinutes: Int, portions: Int): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val recette = Recette(UUID.randomUUID().toString(), foyerId, titre, ingredients, instructions, dureeMinutes, portions, uid)
            recettesRef(foyerId).document(recette.id).set(recette).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun supprimerRecette(foyerId: String, recetteId: String): Result<Unit> {
        return try {
            recettesRef(foyerId).document(recetteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
