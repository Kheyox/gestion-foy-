package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.Anniversaire
import com.foyer.gestion.data.repository.AnniversairesRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnniversairesUiState(
    val anniversaires: List<Anniversaire> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AnniversairesViewModel @Inject constructor(
    private val repo: AnniversairesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AnniversairesUiState())
    val uiState: StateFlow<AnniversairesUiState> = _uiState.asStateFlow()

    fun observer(foyerId: String) {
        viewModelScope.launch {
            repo.getAnniversairesFlow(foyerId).collect { list ->
                _uiState.value = _uiState.value.copy(anniversaires = list)
            }
        }
    }

    fun ajouter(foyerId: String, prenom: String, nom: String, dateNaissance: Timestamp, emoji: String, note: String) {
        viewModelScope.launch {
            val result = repo.ajouterAnniversaire(foyerId, prenom, nom, dateNaissance, emoji, note)
            if (result.isFailure) _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.localizedMessage)
        }
    }

    fun supprimer(foyerId: String, anniversaireId: String) {
        viewModelScope.launch { repo.supprimerAnniversaire(foyerId, anniversaireId) }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
