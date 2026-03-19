package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.Recette
import com.foyer.gestion.data.repository.RecettesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecettesUiState(
    val recettes: List<Recette> = emptyList(),
    val recetteSelectionnee: Recette? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RecettesViewModel @Inject constructor(
    private val repo: RecettesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecettesUiState())
    val uiState: StateFlow<RecettesUiState> = _uiState.asStateFlow()

    fun observer(foyerId: String) {
        viewModelScope.launch {
            repo.getRecettesFlow(foyerId).collect { list ->
                _uiState.value = _uiState.value.copy(recettes = list)
            }
        }
    }

    fun selectionner(recette: Recette?) {
        _uiState.value = _uiState.value.copy(recetteSelectionnee = recette)
    }

    fun creer(foyerId: String, titre: String, ingredients: String, instructions: String, dureeMinutes: Int, portions: Int) {
        viewModelScope.launch {
            val result = repo.creerRecette(foyerId, titre, ingredients, instructions, dureeMinutes, portions)
            if (result.isFailure) _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.localizedMessage)
        }
    }

    fun supprimer(foyerId: String, recetteId: String) {
        viewModelScope.launch { repo.supprimerRecette(foyerId, recetteId) }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
