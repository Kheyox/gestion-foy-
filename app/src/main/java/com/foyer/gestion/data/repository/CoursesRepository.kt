package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.ArticleCourse
import com.foyer.gestion.data.model.ListeCourses
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoursesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun listesRef(foyerId: String) =
        firestore.collection("foyers").document(foyerId).collection("listes_courses")

    fun getListesFlow(foyerId: String): Flow<List<ListeCourses>> = callbackFlow {
        val listener = listesRef(foyerId)
            .orderBy("modifieLe", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val listes = snap?.documents?.mapNotNull { it.toObject(ListeCourses::class.java) }
                trySend(listes ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun creerListe(foyerId: String, nom: String): Result<ListeCourses> {
        return try {
            val liste = ListeCourses(
                id = UUID.randomUUID().toString(),
                foyerId = foyerId,
                nom = nom
            )
            listesRef(foyerId).document(liste.id).set(liste).await()
            Result.success(liste)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun supprimerListe(foyerId: String, listeId: String): Result<Unit> {
        return try {
            listesRef(foyerId).document(listeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getArticlesFlow(foyerId: String, listeId: String): Flow<List<ArticleCourse>> = callbackFlow {
        val listener = listesRef(foyerId).document(listeId)
            .addSnapshotListener { snap, _ ->
                val liste = snap?.toObject(ListeCourses::class.java)
                trySend(liste?.articles ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun ajouterArticle(
        foyerId: String,
        listeId: String,
        nom: String,
        quantite: String,
        unite: String,
        categorie: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val article = ArticleCourse(
                id = UUID.randomUUID().toString(),
                nom = nom,
                quantite = quantite,
                unite = unite,
                categorie = categorie,
                ajoutePar = uid
            )
            val docRef = listesRef(foyerId).document(listeId)
            firestore.runTransaction { transaction ->
                val snap = transaction.get(docRef)
                val liste = snap.toObject(ListeCourses::class.java)!!
                val articles = liste.articles.toMutableList()
                articles.add(article)
                transaction.update(docRef, mapOf(
                    "articles" to articles,
                    "modifieLe" to Timestamp.now()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun cocherArticle(
        foyerId: String,
        listeId: String,
        articleId: String,
        estCoche: Boolean
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val docRef = listesRef(foyerId).document(listeId)
            firestore.runTransaction { transaction ->
                val snap = transaction.get(docRef)
                val liste = snap.toObject(ListeCourses::class.java)!!
                val articles = liste.articles.map { article ->
                    if (article.id == articleId) {
                        article.copy(
                            estCoche = estCoche,
                            cochePar = if (estCoche) uid else "",
                            cocheLe = if (estCoche) Timestamp.now() else null
                        )
                    } else article
                }
                transaction.update(docRef, mapOf("articles" to articles))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun supprimerArticle(
        foyerId: String,
        listeId: String,
        articleId: String
    ): Result<Unit> {
        return try {
            val docRef = listesRef(foyerId).document(listeId)
            firestore.runTransaction { transaction ->
                val snap = transaction.get(docRef)
                val liste = snap.toObject(ListeCourses::class.java)!!
                val articles = liste.articles.filter { it.id != articleId }
                transaction.update(docRef, mapOf("articles" to articles))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun viderCoches(foyerId: String, listeId: String): Result<Unit> {
        return try {
            val docRef = listesRef(foyerId).document(listeId)
            firestore.runTransaction { transaction ->
                val snap = transaction.get(docRef)
                val liste = snap.toObject(ListeCourses::class.java)!!
                val articles = liste.articles.filter { !it.estCoche }
                transaction.update(docRef, mapOf("articles" to articles))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
