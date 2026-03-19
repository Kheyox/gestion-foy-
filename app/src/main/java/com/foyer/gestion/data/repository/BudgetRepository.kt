package com.foyer.gestion.data.repository

import com.foyer.gestion.data.model.Transaction
import com.foyer.gestion.data.model.TypeTransaction
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
class BudgetRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun transactionsRef(foyerId: String) =
        firestore.collection("foyers").document(foyerId).collection("transactions")

    fun getTransactionsFlow(foyerId: String): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionsRef(foyerId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                val transactions = snap?.documents?.mapNotNull { it.toObject(Transaction::class.java) }
                trySend(transactions ?: emptyList())
            }
        awaitClose { listener.remove() }
    }

    suspend fun ajouterTransaction(
        foyerId: String,
        titre: String,
        montant: Double,
        type: TypeTransaction,
        categorie: String,
        note: String,
        date: Timestamp
    ): Result<Transaction> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                foyerId = foyerId,
                titre = titre,
                montant = montant,
                type = type.name,
                categorie = categorie,
                ajoutePar = uid,
                date = date,
                note = note
            )
            transactionsRef(foyerId).document(transaction.id).set(transaction).await()
            Result.success(transaction)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun supprimerTransaction(foyerId: String, transactionId: String): Result<Unit> {
        return try {
            transactionsRef(foyerId).document(transactionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // Calculs du mois courant
    fun calculerResume(transactions: List<Transaction>, mois: Int, annee: Int): BudgetResume {
        val filtrees = transactions.filter { t ->
            val cal = java.util.Calendar.getInstance()
            cal.time = t.date.toDate()
            cal.get(java.util.Calendar.MONTH) + 1 == mois &&
                    cal.get(java.util.Calendar.YEAR) == annee
        }
        val depenses = filtrees.filter { it.type == TypeTransaction.DEPENSE.name }
            .sumOf { it.montant }
        val revenus = filtrees.filter { it.type == TypeTransaction.REVENU.name }
            .sumOf { it.montant }
        return BudgetResume(depenses = depenses, revenus = revenus, solde = revenus - depenses)
    }
}

data class BudgetResume(
    val depenses: Double = 0.0,
    val revenus: Double = 0.0,
    val solde: Double = 0.0
)
