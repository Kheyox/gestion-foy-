package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.ArticleCourse
import com.foyer.gestion.data.model.ListeCourses
import com.foyer.gestion.data.repository.AuthRepository
import com.foyer.gestion.data.repository.CoursesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CoursesUiState(
    val listes: List<ListeCourses> = emptyList(),
    val articles: List<ArticleCourse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CoursesViewModel @Inject constructor(
    private val coursesRepository: CoursesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoursesUiState())
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    private val foyerId: String
        get() = authRepository.currentUser?.let { user ->
            // Récupéré depuis l'auth state, on passe en paramètre à chaque méthode
            ""
        } ?: ""

    fun observerListes(foyerId: String) {
        viewModelScope.launch {
            coursesRepository.getListesFlow(foyerId).collect { listes ->
                _uiState.value = _uiState.value.copy(listes = listes)
            }
        }
    }

    fun observerArticles(foyerId: String, listeId: String) {
        viewModelScope.launch {
            coursesRepository.getArticlesFlow(foyerId, listeId).collect { articles ->
                _uiState.value = _uiState.value.copy(articles = articles)
            }
        }
    }

    fun creerListe(foyerId: String, nom: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = coursesRepository.creerListe(foyerId, nom)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = result.exceptionOrNull()?.localizedMessage
            )
        }
    }

    fun supprimerListe(foyerId: String, listeId: String) {
        viewModelScope.launch {
            coursesRepository.supprimerListe(foyerId, listeId)
        }
    }

    fun ajouterArticle(
        foyerId: String, listeId: String,
        nom: String, quantite: String, unite: String, categorie: String
    ) {
        viewModelScope.launch {
            coursesRepository.ajouterArticle(foyerId, listeId, nom, quantite, unite, categorie)
        }
    }

    fun cocherArticle(foyerId: String, listeId: String, articleId: String, estCoche: Boolean) {
        viewModelScope.launch {
            coursesRepository.cocherArticle(foyerId, listeId, articleId, estCoche)
        }
    }

    fun supprimerArticle(foyerId: String, listeId: String, articleId: String) {
        viewModelScope.launch {
            coursesRepository.supprimerArticle(foyerId, listeId, articleId)
        }
    }

    fun viderCoches(foyerId: String, listeId: String) {
        viewModelScope.launch {
            coursesRepository.viderCoches(foyerId, listeId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
