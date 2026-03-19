package com.foyer.gestion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foyer.gestion.data.model.Foyer
import com.foyer.gestion.data.model.Utilisateur
import com.foyer.gestion.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val user: FirebaseUser? = null,
    val utilisateur: Utilisateur? = null,
    val foyer: Foyer? = null,
    val foyerId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authStateFlow.collect { user ->
                if (user != null) {
                    val utilisateur = authRepository.getUtilisateur(user.uid)
                    val foyerId = utilisateur?.foyerId?.takeIf { it.isNotEmpty() }
                    val foyer = foyerId?.let { authRepository.getFoyer(it) }
                    _authState.value = AuthState(
                        user = user,
                        utilisateur = utilisateur,
                        foyer = foyer,
                        foyerId = foyerId,
                        isLoading = false
                    )
                } else {
                    _authState.value = AuthState(user = null, isLoading = false)
                }
            }
        }
    }

    fun inscrire(email: String, password: String, prenom: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            val result = authRepository.inscrire(email, password, prenom)
            if (result.isFailure) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun connecter(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            val result = authRepository.connecter(email, password)
            if (result.isFailure) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun deconnecter() {
        authRepository.deconnecter()
    }

    fun creerFoyer(nom: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            val result = authRepository.creerFoyer(nom)
            if (result.isSuccess) {
                val foyer = result.getOrNull()!!
                _authState.value = _authState.value.copy(
                    foyer = foyer,
                    foyerId = foyer.id,
                    isLoading = false
                )
            } else {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun rejoindreParCode(code: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            val result = authRepository.rejoindreParCode(code)
            if (result.isSuccess) {
                val foyer = result.getOrNull()!!
                _authState.value = _authState.value.copy(
                    foyer = foyer,
                    foyerId = foyer.id,
                    isLoading = false
                )
            } else {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
