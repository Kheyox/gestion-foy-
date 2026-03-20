package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.Note
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
class NotesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun notesRef(foyerId: String) =
        firestore.collection("foyers").document(foyerId).collection("notes")

    fun getNotesFlow(foyerId: String): Flow<List<Note>> = callbackFlow {
        val listener = notesRef(foyerId)
            .orderBy("modifieLe", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.toObject(Note::class.java) } ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun creerNote(foyerId: String, titre: String, contenu: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val note = Note(UUID.randomUUID().toString(), foyerId, titre, contenu, uid)
            notesRef(foyerId).document(note.id).set(note).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun modifierNote(foyerId: String, note: Note): Result<Unit> {
        return try {
            val updated = note.copy(modifieLe = Timestamp.now())
            notesRef(foyerId).document(note.id).set(updated).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun supprimerNote(foyerId: String, noteId: String): Result<Unit> {
        return try {
            notesRef(foyerId).document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
