package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.ArticleFrigo
import com.foyer.gestion.data.repository.FrigoRepository
import com.foyer.gestion.data.repository.ProduitInfo
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Emplacement(val label: String, val emoji: String) {
    REFRIGERATEUR("Réfrigérateur", "🧊"),
    CONGELATEUR("Congélateur", "❄️"),
    GARDE_MANGER("Garde-manger", "📦")
}

enum class FrigoMode { NORMAL, SCANNING, SEARCHING_ONLINE, ADDING }

data class FrigoUiState(
    val articles: List<ArticleFrigo> = emptyList(),
    val emplacementActif: Emplacement = Emplacement.REFRIGERATEUR,
    val mode: FrigoMode = FrigoMode.NORMAL,
    val searchQuery: String = "",
    val searchResults: List<ProduitInfo> = emptyList(),
    val isSearching: Boolean = false,
    val produitPreselectionne: ProduitInfo? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val articlesFiltres: List<ArticleFrigo>
        get() = articles.filter { it.categorie == emplacementActif.label }
}

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

    fun setEmplacement(emplacement: Emplacement) {
        _uiState.value = _uiState.value.copy(emplacementActif = emplacement)
    }

    fun setMode(mode: FrigoMode) {
        _uiState.value = _uiState.value.copy(
            mode = mode,
            searchQuery = "",
            searchResults = emptyList(),
            produitPreselectionne = if (mode == FrigoMode.ADDING) _uiState.value.produitPreselectionne else null
        )
    }

    fun onBarcodeDetected(foyerId: String, barcode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, mode = FrigoMode.ADDING)
            val produit = repo.rechercherParCodeBarre(barcode)
            // Si produit non trouvé, on ouvre quand même le formulaire avec nom vide
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                produitPreselectionne = produit ?: ProduitInfo("", null)
            )
        }
    }

    fun rechercherSurInternet(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchQuery = query)
            val results = repo.rechercherSurInternet(query)
            _uiState.value = _uiState.value.copy(isSearching = false, searchResults = results)
        }
    }

    fun selectionnerProduit(produit: ProduitInfo) {
        _uiState.value = _uiState.value.copy(
            produitPreselectionne = produit,
            mode = FrigoMode.ADDING
        )
    }

    fun ajouter(
        foyerId: String,
        nom: String,
        quantite: String,
        categorie: String,
        dateExpiration: Timestamp?,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            val result = repo.ajouterArticle(foyerId, nom, quantite, categorie, dateExpiration, imageUrl)
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    fun supprimer(foyerId: String, articleId: String) {
        viewModelScope.launch { repo.supprimerArticle(foyerId, articleId) }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
