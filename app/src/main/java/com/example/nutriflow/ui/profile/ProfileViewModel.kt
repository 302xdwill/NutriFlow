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

// El estado de la UI que ya definiste en ProfileScreen.kt (solo para referencia)
data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Asumimos que inicializas el repositorio aquí
    private val userRepository: UserRepositoryImpl
    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Manejo de eventos de logout
    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent.asStateFlow()

    init {
        val database = NutriFlowDatabase.getDatabase(application)
        userRepository = UserRepositoryImpl(database.userDao())
        // Puedes cargar el usuario aquí si es necesario, pero ProfileScreen ya lo hace vía AuthViewModel.
    }

    // 1. ✅ FUNCIÓN CLAVE FALTANTE: Actualiza el perfil del usuario
    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // El repositorio debe manejar la conversión de User (dominio) a UserEntity (Room)
                userRepository.updateUser(user)

                // Opcional: Recargar el usuario o simplemente actualizar el estado
                _uiState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error al actualizar: ${e.message}") }
            }
        }
    }

    // 2. Función de Logout
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
}