package com.example.nutriflow.ui.ingredients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriflow.data.local.database.NutriFlowDatabase
import com.example.nutriflow.data.repository.IngredientRepositoryImpl
import com.example.nutriflow.data.repository.UserRepositoryImpl
import com.example.nutriflow.domain.model.Ingredient
import com.example.nutriflow.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first // ⬅️ Importación necesaria
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Estado del formulario de registro de ingredientes
data class IngredientFormState(
    // ✅ CORRECCIÓN 1: El nombre es un String
    val name: String = "",
    // ✅ CORRECCIÓN 2: El tipo es un String
    val type: String = "PROTEIN",
    val calPerGram: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class IngredientViewModel(application: Application) : AndroidViewModel(application) {

    private val ingredientRepository: IngredientRepository
    private val userRepository: UserRepositoryImpl // Necesario para obtener el ID del usuario

    // Estado del formulario de la UI
    private val _formState = MutableStateFlow(IngredientFormState())
    val formState: StateFlow<IngredientFormState> = _formState.asStateFlow()
    // Nuevo estado para el filtro seleccionado por el usuario.
    private val _selectedFilter = MutableStateFlow("") // "" = Todos
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    // Lista de ingredientes registrados por el usuario
    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients.asStateFlow()
    // Nuevo StateFlow que combina la lista original y el filtro.
    val filteredIngredients: StateFlow<List<Ingredient>> = combine(_ingredients, _selectedFilter) { ingredients, filter ->
        if (filter.isBlank()) {
            ingredients
        } else {
            ingredients.filter { it.type == filter }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    // ID del usuario activo
    private var activeUserId: String = ""

    init {
        val database = NutriFlowDatabase.getDatabase(application)
        ingredientRepository = IngredientRepositoryImpl(database.ingredientDao())
        userRepository = UserRepositoryImpl(database.userDao())

        // 1. Obtener el ID del usuario activo al iniciar el ViewModel
        viewModelScope.launch {
            // ✅ CORRECCIÓN: Usamos .first() para obtener el valor único emitido por el Flow.
            val userId = userRepository.getActiveUserEmail().first()

            if (userId != null) {
                activeUserId = userId // Asignación de String (valor) a String (variable)
                // 2. Iniciar la carga reactiva de ingredientes para este usuario
                loadAllIngredients()
            } else {
                _formState.update { it.copy(errorMessage = "Error: Usuario no autenticado.") }
            }
        }
    }
    fun onFilterChange(newFilter: String) {
        _selectedFilter.value = newFilter
    }
    // --- LÓGICA DE ACTUALIZACIÓN DEL FORMULARIO ---

    fun onNameChange(newName: String) {
        _formState.update { it.copy(name = newName, saveSuccess = false, errorMessage = null) }
    }

    fun onTypeChange(newType: String) {
        // Aseguramos que solo aceptamos tipos válidos (PROTEIN, CARB, FAT, MINERAL)
        _formState.update { it.copy(type = newType, saveSuccess = false, errorMessage = null) }
    }

    fun onCalPerGramChange(newCalPerGram: String) {
        // Permitir solo números (y potencialmente decimales, dependiendo de la configuración del teclado)
        val filtered = newCalPerGram.filter { it.isDigit() || it == '.' }
        _formState.update { it.copy(calPerGram = filtered, saveSuccess = false, errorMessage = null) }
    }

    // --- LÓGICA DE PERSISTENCIA ---

    private fun loadAllIngredients() {
        if (activeUserId.isBlank()) return

        viewModelScope.launch {
            // Utilizamos collect en lugar de first() aquí para mantener la observación continua
            ingredientRepository.getAllIngredients(activeUserId).collect { list ->
                _ingredients.value = list
            }
        }
    }

    fun saveIngredient() {
        if (activeUserId.isBlank()) {
            _formState.update { it.copy(errorMessage = "Error de autenticación. No se puede guardar.") }
            return
        }

        // Validación básica
        val name = _formState.value.name.trim()
        val calPerGramString = _formState.value.calPerGram.trim()
        val type = _formState.value.type

        if (name.isEmpty() || calPerGramString.isEmpty()) {
            _formState.update { it.copy(errorMessage = "Por favor, complete todos los campos.") }
            return
        }

        val calPerGram = calPerGramString.toDoubleOrNull()
        if (calPerGram == null || calPerGram <= 0) {
            _formState.update { it.copy(errorMessage = "Las calorías por gramo deben ser un número positivo.") }
            return
        }

        _formState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val newIngredient = Ingredient(
                    name = name,
                    type = type,
                    caloriesPerGram = calPerGram,
                    userId = activeUserId
                )
                ingredientRepository.saveIngredient(newIngredient)

                _formState.update {
                    it.copy(
                        name = "", // Limpiar el formulario
                        calPerGram = "", // Limpiar el formulario
                        isSaving = false,
                        saveSuccess = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _formState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Error al guardar el ingrediente: ${e.message}"
                    )
                }
            }
        }
    }

    // Función para restablecer el estado de éxito (útil después de mostrar el Toast)
    fun resetSaveSuccess() {
        _formState.update { it.copy(saveSuccess = false) }
    }
}