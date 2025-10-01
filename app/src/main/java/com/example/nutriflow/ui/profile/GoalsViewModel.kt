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
import kotlin.math.roundToInt

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

    private var currentUser: User? = null

    init {
        val database = NutriFlowDatabase.getDatabase(application)
        userRepository = UserRepositoryImpl(database.userDao())
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Obtener el ID de usuario activo
                val activeUserId = userRepository.getActiveUserEmail().firstOrNull()

                if (activeUserId != null) {
                    // Usamos collect para reaccionar si el usuario es actualizado
                    userRepository.getUser(activeUserId).collect { user ->
                        if (user != null) {
                            currentUser = user
                            _uiState.update {
                                it.copy(
                                    userId = user.email,
                                    // Redondeo simple para inputs.
                                    calorieGoalInput = user.calorieGoal.roundTo(0).toString(),
                                    proteinGoalInput = user.proteinGoal.roundTo(0).toString(),
                                    carbsGoalInput = user.carbsGoal.roundTo(0).toString(),
                                    fatGoalInput = user.fatGoal.roundTo(0).toString(),
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

    private fun Double.roundTo(decimals: Int): Int {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (this * multiplier).roundToInt()
    }

    // --- MANEJO DE CAMBIOS (Se mantienen y se refuerza el filtro) ---

    private fun filterInput(newValue: String): String {
        return newValue.filter { it.isDigit() || it == '.' }
    }

    fun onCalorieGoalChange(newValue: String) { _uiState.update { it.copy(calorieGoalInput = filterInput(newValue)) } }
    fun onProteinGoalChange(newValue: String) { _uiState.update { it.copy(proteinGoalInput = filterInput(newValue)) } }
    fun onCarbsGoalChange(newValue: String) { _uiState.update { it.copy(carbsGoalInput = filterInput(newValue)) } }
    fun onFatGoalChange(newValue: String) { _uiState.update { it.copy(fatGoalInput = filterInput(newValue)) } }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    // --- FUNCIÓN DE GUARDADO (Corregida) ---

    fun saveGoals() {
        val state = _uiState.value

        // Validación y conversión
        val calories = state.calorieGoalInput.toDoubleOrNull()
        val protein = state.proteinGoalInput.toDoubleOrNull()
        val carbs = state.carbsGoalInput.toDoubleOrNull()
        val fat = state.fatGoalInput.toDoubleOrNull()

        if (calories == null || protein == null || carbs == null || fat == null ||
            calories <= 0 || protein < 0 || carbs < 0 || fat < 0) {
            _uiState.update { it.copy(errorMessage = "Por favor, ingresa valores válidos (Calorías > 0).") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                if (currentUser != null) {
                    val updatedUser = currentUser!!.copy(
                        calorieGoal = calories,
                        proteinGoal = protein,
                        carbsGoal = carbs,
                        fatGoal = fat
                    )
                    userRepository.updateUser(updatedUser)

                    currentUser = updatedUser // Actualizamos la referencia local

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