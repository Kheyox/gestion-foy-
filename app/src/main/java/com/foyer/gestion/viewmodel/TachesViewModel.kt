package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.StatutTache
import com.foyer.gestion.data.model.Tache
import com.foyer.gestion.data.repository.TachesRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TachesUiState(
    val taches: List<Tache> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TachesViewModel @Inject constructor(
    private val tachesRepository: TachesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TachesUiState())
    val uiState: StateFlow<TachesUiState> = _uiState.asStateFlow()

    fun observerTaches(foyerId: String) {
        viewModelScope.launch {
            tachesRepository.getTachesFlow(foyerId).collect { taches ->
                _uiState.value = _uiState.value.copy(taches = taches)
            }
        }
    }

    fun creerTache(
        foyerId: String,
        titre: String,
        description: String,
        priorite: String,
        assigneA: String,
        echeance: Timestamp? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = tachesRepository.creerTache(foyerId, titre, description, priorite, assigneA, echeance)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = result.exceptionOrNull()?.localizedMessage
            )
        }
    }

    fun changerStatut(foyerId: String, tacheId: String, statut: StatutTache) {
        viewModelScope.launch {
            tachesRepository.changerStatut(foyerId, tacheId, statut)
        }
    }

    fun supprimerTache(foyerId: String, tacheId: String) {
        viewModelScope.launch {
            tachesRepository.supprimerTache(foyerId, tacheId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
