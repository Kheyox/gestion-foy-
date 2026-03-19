package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.Transaction
import com.foyer.gestion.data.model.TypeTransaction
import com.foyer.gestion.data.repository.BudgetRepository
import com.foyer.gestion.data.repository.BudgetResume
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BudgetUiState(
    val transactions: List<Transaction> = emptyList(),
    val resume: BudgetResume = BudgetResume(),
    val moisSelectionne: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val anneeSelectionnee: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    fun observerTransactions(foyerId: String) {
        viewModelScope.launch {
            budgetRepository.getTransactionsFlow(foyerId).collect { transactions ->
                val resume = budgetRepository.calculerResume(
                    transactions,
                    _uiState.value.moisSelectionne,
                    _uiState.value.anneeSelectionnee
                )
                _uiState.value = _uiState.value.copy(transactions = transactions, resume = resume)
            }
        }
    }

    fun changerMois(mois: Int, annee: Int) {
        val resume = budgetRepository.calculerResume(_uiState.value.transactions, mois, annee)
        _uiState.value = _uiState.value.copy(
            moisSelectionne = mois,
            anneeSelectionnee = annee,
            resume = resume
        )
    }

    fun ajouterTransaction(
        foyerId: String,
        titre: String,
        montant: Double,
        type: TypeTransaction,
        categorie: String,
        note: String,
        date: Timestamp = Timestamp.now()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = budgetRepository.ajouterTransaction(
                foyerId, titre, montant, type, categorie, note, date
            )
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = result.exceptionOrNull()?.localizedMessage
            )
        }
    }

    fun supprimerTransaction(foyerId: String, transactionId: String) {
        viewModelScope.launch {
            budgetRepository.supprimerTransaction(foyerId, transactionId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
