package com.example.nutriflow.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriflow.data.local.database.NutriFlowDatabase
import com.example.nutriflow.data.repository.UserRepositoryImpl
import com.example.nutriflow.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- ESTADO DE LA UI DEL PERFIL ---
data class ProfileUiState(
    // Este estado sólo se usa para mostrar el feedback de las operaciones
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false, // Nuevo: Para mostrar feedback de guardado
    val errorMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepositoryImpl
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Manejo de eventos de logout (se mantiene)
    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()

    init {
        val database = NutriFlowDatabase.getDatabase(application)
        userRepository = UserRepositoryImpl(database.userDao())
    }

    /**
     * Actualiza el perfil del usuario en el repositorio.
     */
    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, saveSuccess = false) }
            try {
                userRepository.updateUser(user)

                // Éxito:
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al actualizar: ${e.message}") }
            }
        }
    }

    /**
     * Función de Logout
     */
    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout()
                _logoutEvent.value = true
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al cerrar sesión: ${e.message}") }
            }
        }
    }

    /**
     * Función para resetear el estado de éxito después de que la UI lo haya consumido.
     */
    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}