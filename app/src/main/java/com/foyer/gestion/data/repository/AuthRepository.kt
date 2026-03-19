package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.Foyer
import com.foyer.gestion.data.model.Utilisateur
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun inscrire(email: String, password: String, prenom: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            val utilisateur = Utilisateur(
                uid = user.uid,
                prenom = prenom,
                email = email,
                avatarEmoji = listOf("😊", "🌟", "🦋", "🌸", "🐧", "🦁", "🐻", "🌈").random()
            )
            firestore.collection("utilisateurs").document(user.uid).set(utilisateur).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connecter(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deconnecter() = auth.signOut()

    suspend fun creerFoyer(nom: String): Result<Foyer> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val code = UUID.randomUUID().toString().take(6).uppercase()
            val foyer = Foyer(
                id = UUID.randomUUID().toString(),
                nom = nom,
                membres = listOf(uid),
                codeInvitation = code,
                creePar = uid
            )
            firestore.collection("foyers").document(foyer.id).set(foyer).await()
            firestore.collection("utilisateurs").document(uid)
                .update("foyerId", foyer.id).await()
            Result.success(foyer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejoindreParCode(code: String): Result<Foyer> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val snapshot = firestore.collection("foyers")
                .whereEqualTo("codeInvitation", code.uppercase())
                .get().await()
            if (snapshot.isEmpty) return Result.failure(Exception("Code invalide"))
            val doc = snapshot.documents.first()
            val foyer = doc.toObject(Foyer::class.java)!!
            val nouveauxMembres = foyer.membres.toMutableList()
            if (!nouveauxMembres.contains(uid)) nouveauxMembres.add(uid)
            firestore.collection("foyers").document(foyer.id)
                .update("membres", nouveauxMembres).await()
            firestore.collection("utilisateurs").document(uid)
                .update("foyerId", foyer.id).await()
            Result.success(foyer.copy(membres = nouveauxMembres))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUtilisateur(uid: String): Utilisateur? {
        return try {
            val doc = firestore.collection("utilisateurs").document(uid).get().await()
            doc.toObject(Utilisateur::class.java)
        } catch (e: Exception) { null }
    }

    suspend fun getFoyer(foyerId: String): Foyer? {
        return try {
            val doc = firestore.collection("foyers").document(foyerId).get().await()
            doc.toObject(Foyer::class.java)
        } catch (e: Exception) { null }
    }

    fun getFoyerFlow(foyerId: String): Flow<Foyer?> = callbackFlow {
        val listener = firestore.collection("foyers").document(foyerId)
            .addSnapshotListener { snap, _ -> trySend(snap?.toObject(Foyer::class.java)) }
        awaitClose { listener.remove() }
    }
}
