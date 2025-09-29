package com.example.nutriflow.ui.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.nutriflow.data.local.database.NutriFlowDatabase
import com.example.nutriflow.data.repository.MealRepositoryImpl
import com.example.nutriflow.data.repository.PlateRepositoryImpl
import com.example.nutriflow.data.repository.UserRepositoryImpl
import com.example.nutriflow.domain.model.Meal
import com.example.nutriflow.domain.model.Plate
import com.example.nutriflow.domain.model.User
import com.example.nutriflow.domain.usecase.GetDailyMealsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// Modelos locales (para la compilación del ViewModel)
data class DailySummary(
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalMinerals: Double = 0.0, // Mapea a FatGoal
    val calorieGoal: Double = 2000.0,
    val proteinGoal: Double = 100.0,
    val carbsGoal: Double = 250.0,
    val mineralsGoal: Double = 70.0 // Mapea a FatGoal
)

data class AddMealState(
    val plateOptions: List<Plate> = emptyList(),
    val selectedPlate: Plate? = null,
    val mealTimeMillis: Long = System.currentTimeMillis(),
    val reminderMinutesBefore: Int = 30,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val mealRepository: MealRepositoryImpl
    private val userRepository: UserRepositoryImpl
    private val plateRepository: PlateRepositoryImpl
    private val getDailyMealsUseCase: GetDailyMealsUseCase

    // ✅ CORRECCIÓN DE ERROR DE TIPO: Usamos StateFlow para el ID activo.
    private val activeUserIdFlow: StateFlow<String?>

    private val _dailyMeals = MutableStateFlow<List<Meal>>(emptyList())
    val dailyMeals: StateFlow<List<Meal>> = _dailyMeals.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _userGoals = MutableStateFlow<User?>(null)

    private val _addMealState = MutableStateFlow(AddMealState())
    val addMealState: StateFlow<AddMealState> = _addMealState.asStateFlow()

    // Combina comidas y objetivos para calcular el resumen diario
    val summary: StateFlow<DailySummary> = combine(_dailyMeals, _userGoals) { meals, user ->
        calculateSummary(meals, user)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailySummary()
    )

    init {
        val database = NutriFlowDatabase.getDatabase(application)
        mealRepository = MealRepositoryImpl(database.mealDao())
        userRepository = UserRepositoryImpl(database.userDao())
        plateRepository = PlateRepositoryImpl(database.plateDao(), database.ingredientDao(), database)
        getDailyMealsUseCase = GetDailyMealsUseCase(mealRepository)

        // ✅ CORRECCIÓN DE ERROR DE TIPO: Inicializamos el StateFlow usando la función del repositorio.
        activeUserIdFlow = userRepository.getActiveUserEmail()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

        // --- LÓGICA DE CARGA BASADA EN EL ID ACTIVO Y LA FECHA ---

        viewModelScope.launch {
            // Se ejecuta solo cuando activeUserIdFlow emite un ID no nulo.
            activeUserIdFlow.filterNotNull().collectLatest { userId ->

                // 1. Cargar objetivos del usuario
                launch {
                    userRepository.getUser(userId).collectLatest { user ->
                        _userGoals.value = user
                    }
                }

                // 2. Observa la fecha seleccionada y carga las comidas
                _selectedDate
                    .onEach { date -> loadMealsByDate(userId, date) }
                    .launchIn(viewModelScope)

                // 3. Cargar platos disponibles
                // Nota: Usamos collect para mantener actualizada la lista si cambia.
                plateRepository.getAllPlates(userId).collect { plates ->
                    _addMealState.update { it.copy(plateOptions = plates) }
                }
            }
        }
    }

    // --- LÓGICA DE DATOS ---

    private fun loadMealsByDate(userId: String, date: Date) {
        viewModelScope.launch {
            getDailyMealsUseCase(userId, date).collectLatest { meals ->
                _dailyMeals.value = meals
            }
        }
    }

    fun selectNewDate(newDate: Date) {
        _selectedDate.value = newDate
    }

    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return dateFormat.format(_selectedDate.value)
    }

    private fun calculateSummary(meals: List<Meal>, user: User?): DailySummary {
        val totalCals = meals.sumOf { it.calories }
        val totalProtein = meals.sumOf { it.proteinGrams }
        val totalCarbs = meals.sumOf { it.carbsGrams }
        val totalMinerals = meals.sumOf { it.mineralsGrams }

        return DailySummary(
            totalCalories = totalCals,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalMinerals = totalMinerals,

            calorieGoal = user?.calorieGoal ?: 2000.0,
            proteinGoal = user?.proteinGoal ?: 100.0,
            carbsGoal = user?.carbsGoal ?: 250.0,
            // Mapeamos FatGoal del usuario a MineralsGoal del Summary
            mineralsGoal = user?.fatGoal ?: 70.0
        )
    }

    // --- LÓGICA DE ADICIÓN DE COMIDAS ---

    fun onPlateSelected(plate: Plate?) {
        _addMealState.update { it.copy(selectedPlate = plate, errorMessage = null) }
    }

    fun onTimeSelected(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = _selectedDate.value
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        _addMealState.update { it.copy(mealTimeMillis = calendar.timeInMillis) }
    }

    fun onReminderMinutesChange(minutes: Int) {
        _addMealState.update { it.copy(reminderMinutesBefore = minutes) }
    }

    fun scheduleMeal(mealType: String) {
        val userId = activeUserIdFlow.value // Obtenemos el valor actual del StateFlow

        if (userId.isNullOrBlank() || _addMealState.value.selectedPlate == null) {
            _addMealState.update { it.copy(errorMessage = "Seleccione un plato e inicie sesión.") }
            return
        }

        _addMealState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val state = _addMealState.value
                val plate = state.selectedPlate!!

                val reminderTime = state.mealTimeMillis - (state.reminderMinutesBefore * 60 * 1000L)

                val mealToSchedule = Meal(
                    userId = userId,
                    name = plate.name,
                    type = mealType,
                    calories = plate.totalCalories,
                    proteinGrams = plate.totalProtein,
                    carbsGrams = plate.totalCarbs,
                    mineralsGrams = plate.totalFat, // Mapeamos Fat a Minerals
                    date = _selectedDate.value,
                    scheduledTime = state.mealTimeMillis,
                    reminderTime = reminderTime
                )

                // ✅ CORRECCIÓN: Usamos saveMeal() que sí existe en la interfaz.
                mealRepository.saveMeal(mealToSchedule)

                scheduleNotification(mealToSchedule.name, reminderTime)

                _addMealState.update {
                    it.copy(isLoading = false, saveSuccess = true, errorMessage = null)
                }
                resetAddMealState()

            } catch (e: Exception) {
                _addMealState.update {
                    it.copy(isLoading = false, errorMessage = "Error al programar: ${e.message}")
                }
            }
        }
    }

    private fun scheduleNotification(mealName: String, timeMillis: Long) {
        val currentTime = System.currentTimeMillis()
        val delay = timeMillis - currentTime

        if (delay > 0) {
            val data = Data.Builder().putString("MEAL_NAME", mealName).build()

            val reminderRequest = OneTimeWorkRequest.Builder(com.example.nutriflow.data.worker.ReminderWorker::class.java)
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(getApplication()).enqueue(reminderRequest)
        }
    }

    fun resetAddMealState() {
        _addMealState.update {
            it.copy(
                selectedPlate = null,
                mealTimeMillis = Date().time,
                reminderMinutesBefore = 30,
                saveSuccess = false,
                errorMessage = null
            )
        }
    }
}