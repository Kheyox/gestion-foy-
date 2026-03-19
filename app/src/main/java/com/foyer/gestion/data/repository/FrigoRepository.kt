package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.ArticleFrigo
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
class FrigoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun frigoRef(foyerId: String) =
        firestore.collection("foyers").document(foyerId).collection("frigo")

    fun getArticlesFlow(foyerId: String): Flow<List<ArticleFrigo>> = callbackFlow {
        val listener = frigoRef(foyerId)
            .orderBy("ajouteLe", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toObject(ArticleFrigo::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun ajouterArticle(foyerId: String, nom: String, quantite: String, categorie: String, dateExpiration: Timestamp?): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val article = ArticleFrigo(UUID.randomUUID().toString(), foyerId, nom, quantite, categorie, dateExpiration, uid)
            frigoRef(foyerId).document(article.id).set(article).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun supprimerArticle(foyerId: String, articleId: String): Result<Unit> {
        return try {
            frigoRef(foyerId).document(articleId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
