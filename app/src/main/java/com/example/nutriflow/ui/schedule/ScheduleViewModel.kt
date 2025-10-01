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
import com.example.nutriflow.domain.model.DailySummary
import com.example.nutriflow.domain.model.MealScheduleState
import com.example.nutriflow.domain.usecase.GetDailyMealsUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.nutriflow.data.worker.ReminderWorker

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val mealRepository: MealRepositoryImpl
    private val userRepository: UserRepositoryImpl
    private val plateRepository: PlateRepositoryImpl
    private val getDailyMealsUseCase: GetDailyMealsUseCase

    private val activeUserIdFlow: StateFlow<String?>

    private val _dailyMeals = MutableStateFlow<List<Meal>>(emptyList())
    val dailyMeals: StateFlow<List<Meal>> = _dailyMeals.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val _userGoals = MutableStateFlow<User?>(null)

    private val _addMealState = MutableStateFlow(MealScheduleState())
    val addMealState: StateFlow<MealScheduleState> = _addMealState.asStateFlow()

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

        activeUserIdFlow = userRepository.getActiveUserEmail()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )

        // --- LÓGICA DE CARGA BASADA EN EL ID ACTIVO Y LA FECHA ---

        viewModelScope.launch {
            activeUserIdFlow.filterNotNull().collectLatest { userId ->

                // 1. Cargar objetivos del usuario y mantenerlos actualizados
                launch {
                    userRepository.getUser(userId).collectLatest { user ->
                        _userGoals.value = user
                    }
                }

                // 2. Observa la fecha seleccionada y carga las comidas
                _selectedDate
                    .onEach { date -> loadMealsByDate(userId, date) }
                    .launchIn(viewModelScope)

                // 3. Cargar platos disponibles y mantenerlos actualizados
                plateRepository.getAllPlates(userId).collect { plates ->
                    _addMealState.update { it.copy(plateOptions = plates) }
                }
            }
        }
    }

    // --- LÓGICA DE DATOS Y RESUMEN ---

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

    private fun calculateSummary(meals: List<Meal>, user: User?): DailySummary {
        // ✅ CORRECCIÓN FINAL: Usamos mineralsGrams de Meal para obtener la grasa (totalFat)
        val totalCals = meals.sumOf { it.calories.toDouble() }
        val totalProtein = meals.sumOf { it.proteinGrams.toDouble() }
        val totalCarbs = meals.sumOf { it.carbsGrams.toDouble() }
        val totalFat = meals.sumOf { it.mineralsGrams.toDouble() } // ¡CORRECCIÓN AQUÍ!

        return DailySummary(
            totalCalories = totalCals,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,

            calorieGoal = user?.calorieGoal ?: 2000.0,
            proteinGoal = user?.proteinGoal ?: 100.0,
            carbsGoal = user?.carbsGoal ?: 250.0,
            fatGoal = user?.fatGoal ?: 70.0
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
        calendar.set(Calendar.MILLISECOND, 0)

        _addMealState.update { it.copy(mealTimeMillis = calendar.timeInMillis) }
    }

    fun onReminderMinutesChange(minutes: Int) {
        _addMealState.update { it.copy(reminderMinutesBefore = minutes) }
    }

    fun scheduleMeal(mealType: String) {
        val userId = activeUserIdFlow.value

        if (userId.isNullOrBlank() || _addMealState.value.selectedPlate == null) {
            _addMealState.update { it.copy(errorMessage = "Seleccione un plato e inicie sesión.") }
            return
        }

        _addMealState.update { it.copy(isLoading = true, errorMessage = null) }

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
                    mineralsGrams = plate.totalFat, // ¡CORRECCIÓN AQUÍ! Pasamos la grasa al campo de minerales
                    date = _selectedDate.value,
                    scheduledTime = state.mealTimeMillis,
                    reminderTime = reminderTime
                )

                // Guardar la comida en la base de datos
                mealRepository.saveMeal(mealToSchedule)

                // Programar la notificación
                scheduleNotification(mealToSchedule.name, reminderTime)

                _addMealState.update {
                    it.copy(isLoading = false, saveSuccess = true, errorMessage = null)
                }

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

            val reminderRequest = OneTimeWorkRequest.Builder(ReminderWorker::class.java)
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