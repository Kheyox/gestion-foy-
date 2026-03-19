package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.Evenement
import com.foyer.gestion.data.repository.CalendrierRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendrierUiState(
    val evenements: List<Evenement> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CalendrierViewModel @Inject constructor(
    private val repo: CalendrierRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalendrierUiState())
    val uiState: StateFlow<CalendrierUiState> = _uiState.asStateFlow()

    fun observer(foyerId: String) {
        viewModelScope.launch {
            repo.getEvenementsFlow(foyerId).collect { list ->
                _uiState.value = _uiState.value.copy(evenements = list)
            }
        }
    }

    fun creer(foyerId: String, titre: String, description: String, dateDebut: Timestamp, dateFin: Timestamp?) {
        viewModelScope.launch {
            val result = repo.creerEvenement(foyerId, titre, description, dateDebut, dateFin)
            if (result.isFailure) _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.localizedMessage)
        }
    }

    fun supprimer(foyerId: String, evtId: String) {
        viewModelScope.launch { repo.supprimerEvenement(foyerId, evtId) }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
