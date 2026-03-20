package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.Note
import com.foyer.gestion.data.repository.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repo: NotesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    fun observer(foyerId: String) {
        viewModelScope.launch {
            repo.getNotesFlow(foyerId).collect { list ->
                _uiState.value = _uiState.value.copy(notes = list)
            }
        }
    }

    fun creer(foyerId: String, titre: String, contenu: String) {
        viewModelScope.launch {
            val result = repo.creerNote(foyerId, titre, contenu)
            if (result.isFailure) _uiState.value = _uiState.value.copy(error = result.exceptionOrNull()?.localizedMessage)
        }
    }

    fun modifier(foyerId: String, note: Note) {
        viewModelScope.launch { repo.modifierNote(foyerId, note) }
    }

    fun supprimer(foyerId: String, noteId: String) {
        viewModelScope.launch { repo.supprimerNote(foyerId, noteId) }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
