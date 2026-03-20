package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.ArticleFrigo
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class ProduitInfo(
    val nom: String,
    val imageUrl: String?,
    val marque: String? = null
)

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

    suspend fun ajouterArticle(
        foyerId: String,
        nom: String,
        quantite: String,
        categorie: String,
        dateExpiration: Timestamp?,
        imageUrl: String? = null
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val article = ArticleFrigo(
                id = UUID.randomUUID().toString(),
                foyerId = foyerId,
                nom = nom,
                quantite = quantite,
                categorie = categorie,
                dateExpiration = dateExpiration,
                ajoutePar = uid,
                imageUrl = imageUrl
            )
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

    // ── Open Food Facts ────────────────────────────────────────────────────────

    suspend fun rechercherParCodeBarre(barcode: String): ProduitInfo? = withContext(Dispatchers.IO) {
        try {
            val conn = URL("https://world.openfoodfacts.org/api/v0/product/$barcode.json")
                .openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "MonFoyer-Android/1.0")
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            if (conn.responseCode != 200) return@withContext null
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            if (json.optInt("status", 0) != 1) return@withContext null
            val product = json.getJSONObject("product")
            val nom = product.optString("product_name_fr")
                .ifEmpty { product.optString("product_name") }
            if (nom.isBlank()) return@withContext null
            ProduitInfo(
                nom = nom,
                imageUrl = product.optString("image_front_url").ifEmpty { null },
                marque = product.optString("brands").ifEmpty { null }
            )
        } catch (e: Exception) { null }
    }

    suspend fun rechercherSurInternet(query: String): List<ProduitInfo> = withContext(Dispatchers.IO) {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            // API v2 REST (plus fiable que l'ancien endpoint CGI)
            val conn = URL(
                "https://world.openfoodfacts.org/api/v2/search?search_terms=$encoded&page_size=20"
            ).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "MonFoyer/1.0 (gestion foyer Android)")
            conn.setRequestProperty("Accept", "application/json")
            conn.instanceFollowRedirects = true
            conn.connectTimeout = 12000
            conn.readTimeout = 15000

            val code = conn.responseCode
            if (code != 200) return@withContext emptyList()

            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val products = json.optJSONArray("products") ?: return@withContext emptyList()

            (0 until products.length()).mapNotNull { i ->
                val p = products.getJSONObject(i)
                val nom = p.optString("product_name_fr")
                    .ifEmpty { p.optString("product_name") }
                    .ifEmpty { p.optString("abbreviated_product_name") }
                if (nom.isBlank()) return@mapNotNull null
                ProduitInfo(
                    nom = nom,
                    imageUrl = p.optString("image_front_small_url")
                        .ifEmpty { p.optString("image_front_url") }
                        .ifEmpty { null },
                    marque = p.optString("brands").ifEmpty { null }
                )
            }.distinctBy { it.nom }.take(15)
        } catch (e: Exception) {
            android.util.Log.e("FrigoSearch", "Erreur recherche: ${e.javaClass.simpleName}: ${e.message}")
            emptyList()
        }
    }
}
