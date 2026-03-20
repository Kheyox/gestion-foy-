package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.ArticleFrigo
import com.foyer.gestion.data.repository.FrigoRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FrigoUiState(
    val articles: List<ArticleFrigo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FrigoViewModel @Inject constructor(
    private val repo: FrigoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FrigoUiState())
    val uiState: StateFlow<FrigoUiState> = _uiState.asStateFlow()

    fun observer(foyerId: String) {
        viewModelScope.launch {
            repo.getArticlesFlow(foyerId).collect { list ->
                _uiState.value = _uiState.value.copy(articles = list)
            }
        }
    }

    fun ajouter(foyerId: String, nom: String, quantite: String, categorie: String, dateExpiration: Timestamp?) {
        viewModelScope.launch {
            val result = repo.ajouterArticle(foyerId, nom, quantite, categorie, dateExpiration)
            if (result.isFailure) _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.localizedMessage)
        }
    }

    fun supprimer(foyerId: String, articleId: String) {
        viewModelScope.launch { repo.supprimerArticle(foyerId, articleId) }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
