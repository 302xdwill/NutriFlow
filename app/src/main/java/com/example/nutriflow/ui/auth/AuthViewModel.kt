package com.example.nutriflow.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriflow.domain.model.Goal
import com.example.nutriflow.domain.model.User
import com.example.nutriflow.domain.repository.GoalsRepository
import com.example.nutriflow.domain.repository.UserRepository
import com.example.nutriflow.domain.usecase.LoginUserUseCase
import com.example.nutriflow.domain.usecase.RegisterUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ---------------------- 1. CONSTANTES DE METAS PREDETERMINADAS ----------------------

private const val DEFAULT_CALORIES = 2000.0
private const val DEFAULT_PROTEIN = 150.0
private const val DEFAULT_CARBS = 250.0
private const val DEFAULT_FAT = 60.0
private const val GOAL_CALORIES = "Calorías"
private const val GOAL_PROTEIN = "Proteína"
private const val GOAL_CARBS = "Carbohidratos"
private const val GOAL_FAT = "Grasas"

// ---------------------- 2. VIEW MODEL ----------------------

class AuthViewModel(
    private val userRepository: UserRepository,
    private val goalsRepository: GoalsRepository,
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    sealed class AuthState {
        data object Idle : AuthState()
        data object Loading : AuthState()
        data class Success(val isNewUser: Boolean) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    init {
        viewModelScope.launch {
            userRepository.getCurrentUserFlow().collect { user ->
                _currentUser.value = user
            }
        }
    }

    // --- LÓGICA DE LOGIN ---
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                loginUserUseCase(email, password)
                _authState.value = AuthState.Success(isNewUser = false)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de inicio de sesión: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Registra un nuevo usuario e inmediatamente establece sus metas nutricionales.
     */
    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Creamos un modelo de usuario básico
                val basicUser = User(
                    email = email,
                    lastName = "",
                    age = 0,
                    weight = 0.0,
                    height = 0.0,
                    photoUrl = null,
                    calorieGoal = DEFAULT_CALORIES,
                    proteinGoal = DEFAULT_PROTEIN,
                    carbsGoal = DEFAULT_CARBS,
                    fatGoal = DEFAULT_FAT,
                )

                // 1. Llama al caso de uso de registro
                val newUserId = registerUserUseCase(basicUser, password)

                // 2. Establecer metas predeterminadas
                saveDefaultGoals(newUserId)

                _authState.value = AuthState.Success(isNewUser = true)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Error de registro: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Función privada para crear y guardar los objetos Goal por defecto.
     */
    private fun saveDefaultGoals(userId: String) {
        val defaultGoals = listOf(
            Goal(userId = userId, type = GOAL_CALORIES, value = DEFAULT_CALORIES),
            Goal(userId = userId, type = GOAL_PROTEIN, value = DEFAULT_PROTEIN),
            Goal(userId = userId, type = GOAL_CARBS, value = DEFAULT_CARBS),
            Goal(userId = userId, type = GOAL_FAT, value = DEFAULT_FAT)
        )

        viewModelScope.launch {
            goalsRepository.insertGoals(defaultGoals)
        }
    }

    // --- LÓGICA DE LOGOUT ---
    fun onLogoutSuccess() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }
}