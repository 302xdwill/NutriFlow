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
import kotlinx.coroutines.flow.firstOrNull

// Estado que maneja los campos editables y el estado de la operación
data class GoalsState(
    val userId: String = "",
    val calorieGoalInput: String = "2000",
    val proteinGoalInput: String = "100",
    val carbsGoalInput: String = "250",
    val fatGoalInput: String = "70",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepositoryImpl
    private val _uiState = MutableStateFlow(GoalsState())
    val uiState: StateFlow<GoalsState> = _uiState.asStateFlow()

    private var currentUser: User? = null // Mantener la referencia completa del usuario

    init {
        val database = NutriFlowDatabase.getDatabase(application)
        // Asegúrate de que tienes un patrón de inyección de dependencias adecuado.
        // Por ahora, lo mantenemos como lo definiste.
        userRepository = UserRepositoryImpl(database.userDao())
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // ✅ Obtenemos el ID de usuario activo
                val activeUserEmailFlow = userRepository.getActiveUserEmail()
                val activeUserId = activeUserEmailFlow.firstOrNull()

                if (activeUserId != null) {
                    // Usamos .collect para seguir observando si el repositorio cambia el usuario
                    // Corregimos: Usamos .first() o .firstOrNull() si solo necesitamos el valor inicial,
                    // o envolvemos el collect en un try-catch que maneje la cancelación del Job.
                    // Para un ViewModel, collect es generalmente mejor para mantener la reactividad.
                    userRepository.getUser(activeUserId).collect { user ->
                        if (user != null) {
                            currentUser = user
                            _uiState.update {
                                it.copy(
                                    userId = user.email,
                                    // Aseguramos que los valores sean Double antes de convertirlos a String
                                    calorieGoalInput = user.calorieGoal.toString(),
                                    proteinGoalInput = user.proteinGoal.toString(),
                                    carbsGoalInput = user.carbsGoal.toString(),
                                    fatGoalInput = user.fatGoal.toString(),
                                    isLoading = false
                                )
                            }
                        } else {
                            _uiState.update { it.copy(errorMessage = "Usuario no encontrado.", isLoading = false) }
                        }
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Error de autenticación.", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al cargar metas: ${e.localizedMessage}", isLoading = false) }
            }
        }
    }

    // --- MANEJO DE CAMBIOS ---
    // Las funciones de cambio de valor son correctas.
    fun onCalorieGoalChange(newValue: String) {
        val filteredValue = newValue.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(calorieGoalInput = filteredValue) }
    }

    fun onProteinGoalChange(newValue: String) {
        val filteredValue = newValue.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(proteinGoalInput = filteredValue) }
    }

    fun onCarbsGoalChange(newValue: String) {
        val filteredValue = newValue.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(carbsGoalInput = filteredValue) }
    }

    fun onFatGoalChange(newValue: String) {
        val filteredValue = newValue.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(fatGoalInput = filteredValue) }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    // --- FUNCIÓN DE GUARDADO ---
    fun saveGoals() {
        val state = _uiState.value

        // Validación básica
        val calories = state.calorieGoalInput.toDoubleOrNull()
        val protein = state.proteinGoalInput.toDoubleOrNull()
        val carbs = state.carbsGoalInput.toDoubleOrNull()
        val fat = state.fatGoalInput.toDoubleOrNull()

        if (calories == null || protein == null || carbs == null || fat == null ||
            calories <= 0 || protein < 0 || carbs < 0 || fat < 0) {
            _uiState.update { it.copy(errorMessage = "Por favor, ingresa valores válidos y positivos.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                if (currentUser != null) {
                    // ✅ CORRECCIÓN CLAVE: Se eliminaron las comas sueltas.
                    val updatedUser = currentUser!!.copy(
                        calorieGoal = calories,
                        proteinGoal = protein,
                        carbsGoal = carbs,
                        fatGoal = fat
                        // El resto de las propiedades del usuario se copian automáticamente
                    )
                    userRepository.updateUser(updatedUser)

                    // Actualizamos la referencia local del usuario
                    currentUser = updatedUser

                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "Error: Datos de usuario no disponibles.") }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Error al guardar: ${e.localizedMessage}") }
            }
        }
    }
}