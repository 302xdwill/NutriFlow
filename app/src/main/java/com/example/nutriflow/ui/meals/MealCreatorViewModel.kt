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
// ⚠️ ASEGÚRATE DE QUE PlateCreatorState ESTÉ IMPORTADO O DEFINIDO EN OTRO LADO
import com.example.nutriflow.ui.meals.PlateCreatorState // <--- ¡Asegura que esta línea exista si moviste la clase!
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MealCreatorViewModel(application: Application) : AndroidViewModel(application) {

    private val plateRepository: PlateRepository
    private val ingredientRepository: IngredientRepository
    private val userRepository: UserRepositoryImpl
    private val database: NutriFlowDatabase

    // Estado de la UI
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
            val userId = userRepository.getActiveUserEmail().first()

            if (userId != null) {
                activeUserId = userId
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

    // --- MANEJO DEL BUSCADOR ---

    fun onIngredientSearchQueryChange(query: String) {
        val filtered = if (query.isBlank()) {
            emptyList()
        } else {
            // Filtramos la lista maestra (availableIngredients)
            _uiState.value.availableIngredients.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        _uiState.update {
            it.copy(
                ingredientSearchQuery = query,
                filteredIngredients = filtered,
                // Si el usuario empieza a escribir de nuevo, deseleccionamos el ingrediente anterior.
                selectedIngredientForAdding = null
            )
        }
    }

    fun onIngredientAmountChange(amount: String) {
        // Permitir solo números y manejar el String (incluyendo un punto decimal para peso exacto)
        val filtered = amount.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(ingredientAmount = filtered, errorMessage = null) }
    }

    fun selectIngredientForInput(ingredient: Ingredient) {
        // Almacenar el objeto Ingredient en el estado.
        _uiState.update {
            it.copy(
                ingredientSearchQuery = ingredient.name,
                filteredIngredients = emptyList(), // Limpiamos la lista de resultados
                selectedIngredientForAdding = ingredient, // ¡Guardamos la referencia!
                errorMessage = null
            )
        }
    }

    // --- GESTIÓN DE COMPONENTES DEL PLATO ---

    /**
     * Añade un ingrediente base a la lista de componentes del plato con el peso especificado.
     */
    fun addIngredientToPlate() {
        val state = _uiState.value
        val amount = state.ingredientAmount.toDoubleOrNull()

        // Validar usando la referencia directa del objeto
        val selectedIngredient = state.selectedIngredientForAdding

        if (selectedIngredient == null || amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Seleccione un ingrediente válido e ingrese una cantidad > 0.") }
            return
        }

        // 2. Crear y añadir el nuevo componente
        val newComponent = PlateIngredient(
            plateId = 0, // ID temporal
            ingredient = selectedIngredient,
            weightGrams = amount
        )

        // 3. Limpiar y actualizar el estado
        _uiState.update {
            it.copy(
                components = it.components + newComponent,
                ingredientSearchQuery = "",
                ingredientAmount = "",
                filteredIngredients = emptyList(),
                selectedIngredientForAdding = null, // Reseteamos la selección para el próximo ingrediente
                errorMessage = null // Limpiamos cualquier error previo
            )
        }
    }

    /**
     * Actualiza el peso de un componente específico.
     */
    fun updateComponentWeight(index: Int, weightString: String) {
        // Permitir solo números y manejar el String (incluyendo un punto decimal)
        val filteredWeightString = weightString.filter { it.isDigit() || it == '.' }
        val weight = filteredWeightString.toDoubleOrNull() ?: 0.0
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
        var totalProteinGrams = 0.0
        var totalCarbsGrams = 0.0
        var totalFatGrams = 0.0

        for (component in components) {
            val weight = component.weightGrams
            val calPerGram = component.ingredient.caloriesPerGram
            val type = component.ingredient.type

            // Cálculo de calorías totales (el peso * cal/gramo)
            totalCals += weight * calPerGram

            // Cálculo de los macros (en GRAMOS)
            when (type) {
                "PROT" -> totalProteinGrams += weight
                "CARB" -> totalCarbsGrams += weight
                "FAT" -> totalFatGrams += weight
                // Los minerales son ignorados en el cálculo de los 3 macros
            }
        }

        // Redondeo de totales para la UI
        _uiState.update {
            it.copy(
                totalCalories = totalCals.roundTo(2),
                totalProtein = totalProteinGrams.roundTo(2),
                totalCarbs = totalCarbsGrams.roundTo(2),
                totalFat = totalFatGrams.roundTo(2)
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
        // Validar que hay un nombre y al menos un componente con peso > 0
        val validComponents = _uiState.value.components.filter { it.weightGrams > 0 }

        if (_uiState.value.plateName.isBlank() || validComponents.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Debe nombrar el plato y añadir al menos un ingrediente con cantidad mayor a 0.") }
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
                    components = validComponents, // Solo guardar componentes válidos
                    totalCalories = state.totalCalories,
                    totalProtein = state.totalProtein,
                    totalCarbs = state.totalCarbs,
                    totalFat = state.totalFat
                )

                // Guardar el plato
                plateRepository.savePlate(finalPlate)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true, // Esto activará el LaunchedEffect en la UI para volver
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

    // Funciones de utilidad
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
                errorMessage = null,
                ingredientSearchQuery = "",
                ingredientAmount = "",
                filteredIngredients = emptyList(),
                selectedIngredientForAdding = null
            )
        }
    }
}