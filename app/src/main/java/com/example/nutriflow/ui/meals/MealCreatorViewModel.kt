package com.example.nutriflow.ui.meals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriflow.data.local.database.NutriFlowDatabase
import com.example.nutriflow.data.repository.IngredientRepositoryImpl
import com.example.nutriflow.data.repository.PlateRepositoryImpl
import com.example.nutriflow.data.repository.UserRepositoryImpl
import com.example.nutriflow.domain.model.Ingredient
import com.example.nutriflow.domain.model.Plate
import com.example.nutriflow.domain.model.PlateIngredient
import com.example.nutriflow.domain.repository.IngredientRepository
import com.example.nutriflow.domain.repository.PlateRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first // <-- ¡IMPORTANTE! Se necesita para obtener el valor del Flow

// --- ESTADOS DE LA UI ---

// Estado que representa la receta que el usuario está construyendo
data class PlateCreatorState(
    val plateName: String = "",
    val plateDescription: String = "",
    val components: List<PlateIngredient> = emptyList(), // Ingredientes seleccionados con peso
    val availableIngredients: List<Ingredient> = emptyList(), // Lista maestra de ingredientes del usuario
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class MealCreatorViewModel(application: Application) : AndroidViewModel(application) {

    private val plateRepository: PlateRepository
    private val ingredientRepository: IngredientRepository
    private val userRepository: UserRepositoryImpl
    private val database: NutriFlowDatabase // Necesario para inyectar al PlateRepository

    private val _uiState = MutableStateFlow(PlateCreatorState())
    val uiState: StateFlow<PlateCreatorState> = _uiState.asStateFlow()

    private var activeUserId: String = ""

    init {
        database = NutriFlowDatabase.getDatabase(application)
        plateRepository = PlateRepositoryImpl(
            database.plateDao(),
            database.ingredientDao(),
            database
        )
        ingredientRepository = IngredientRepositoryImpl(database.ingredientDao())
        userRepository = UserRepositoryImpl(database.userDao())

        // 1. Obtener el ID del usuario activo
        viewModelScope.launch {
            // ✅ CORRECCIÓN: Usamos .first() para obtener el valor único del Flow (Línea 62 del error)
            val userIdFlow = userRepository.getActiveUserEmail()
            val userId = userIdFlow.first()

            if (userId != null) {
                activeUserId = userId // Asignación de String (valor) a String (variable)
                // 2. Cargar los ingredientes disponibles
                loadAvailableIngredients(activeUserId)
            } else {
                _uiState.update { it.copy(errorMessage = "Error: Usuario no autenticado.") }
            }
        }

        // 3. Flujo reactivo para recalcular macros cuando los componentes cambien
        _uiState
            .map { it.components }
            .distinctUntilChanged()
            .onEach { components ->
                calculateTotals(components)
            }
            .launchIn(viewModelScope)
    }

    private fun loadAvailableIngredients(userId: String) {
        viewModelScope.launch {
            ingredientRepository.getAllIngredients(userId).collect { ingredients ->
                _uiState.update { it.copy(availableIngredients = ingredients) }
            }
        }
    }

    // --- MANEJO DEL FORMULARIO ---

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(plateName = newName, saveSuccess = false, errorMessage = null) }
    }

    fun onDescriptionChange(newDesc: String) {
        _uiState.update { it.copy(plateDescription = newDesc, saveSuccess = false, errorMessage = null) }
    }

    // --- GESTIÓN DE COMPONENTES DEL PLATO ---

    /**
     * Añade un ingrediente base a la lista de componentes del plato con un peso inicial de 0.
     */
    fun addIngredientToPlate(ingredient: Ingredient) {
        val newComponent = PlateIngredient(
            plateId = 0, // ID temporal, se asigna al guardar
            ingredient = ingredient,
            weightGrams = 0.0
        )
        _uiState.update {
            it.copy(components = it.components + newComponent)
        }
    }

    /**
     * Actualiza el peso de un componente específico.
     */
    fun updateComponentWeight(index: Int, weightString: String) {
        val weight = weightString.toDoubleOrNull() ?: 0.0
        val currentComponents = _uiState.value.components.toMutableList()

        if (index >= 0 && index < currentComponents.size) {
            currentComponents[index] = currentComponents[index].copy(weightGrams = weight)
            _uiState.update { it.copy(components = currentComponents) }
        }
    }

    /**
     * Elimina un componente del plato.
     */
    fun removeComponent(component: PlateIngredient) {
        _uiState.update {
            it.copy(components = it.components.filter { c -> c != component })
        }
    }

    // --- CÁLCULO DE MACROS ---

    private fun calculateTotals(components: List<PlateIngredient>) {
        if (components.isEmpty()) {
            _uiState.update {
                it.copy(totalCalories = 0.0, totalProtein = 0.0, totalCarbs = 0.0, totalFat = 0.0)
            }
            return
        }

        var totalCals = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0

        for (component in components) {
            val weight = component.weightGrams
            val calPerGram = component.ingredient.caloriesPerGram
            val type = component.ingredient.type

            // Cálculo base de calorías
            totalCals += weight * calPerGram

            // Asignación de macros (Usando factores estándar: 4/4/9 kcal/g)
            when (type) {
                "PROTEIN" -> totalProtein += (weight * calPerGram) / 4.0
                "CARB" -> totalCarbs += (weight * calPerGram) / 4.0
                "FAT" -> totalFat += (weight * calPerGram) / 9.0
                // MINERAL se ignora en la contribución a macros principales (Protein, Carb, Fat)
            }
        }

        // Redondeo de totales para la UI
        _uiState.update {
            it.copy(
                totalCalories = totalCals.roundTo(2),
                totalProtein = totalProtein.roundTo(2),
                totalCarbs = totalCarbs.roundTo(2),
                totalFat = totalFat.roundTo(2)
            )
        }
    }

    // Función de extensión simple para redondear Double
    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (this * multiplier).roundToInt() / multiplier
    }


    // --- FUNCIÓN DE GUARDADO ---

    fun savePlate() {
        if (activeUserId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Error de autenticación.", isSaving = false) }
            return
        }
        if (_uiState.value.plateName.isBlank() || _uiState.value.components.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Debe nombrar el plato y añadir al menos un ingrediente.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val state = _uiState.value
                val finalPlate = Plate(
                    name = state.plateName.trim(),
                    description = state.plateDescription.trim(),
                    userId = activeUserId,
                    components = state.components.filter { it.weightGrams > 0 }, // Solo guardar componentes con peso
                    totalCalories = state.totalCalories,
                    totalProtein = state.totalProtein,
                    totalCarbs = state.totalCarbs,
                    totalFat = state.totalFat
                )

                plateRepository.savePlate(finalPlate)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Error al guardar el plato: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun resetForm() {
        _uiState.update {
            it.copy(
                plateName = "",
                plateDescription = "",
                components = emptyList(),
                totalCalories = 0.0,
                totalProtein = 0.0,
                totalCarbs = 0.0,
                totalFat = 0.0,
                isSaving = false,
                saveSuccess = false,
                errorMessage = null
            )
        }
    }
}