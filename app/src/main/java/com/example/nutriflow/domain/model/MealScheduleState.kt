package com.example.nutriflow.domain.model

data class MealScheduleState(
    val plateOptions: List<Plate> = emptyList(),
    val selectedPlate: Plate? = null,
    val mealTimeMillis: Long = System.currentTimeMillis(),
    val reminderMinutesBefore: Int = 15,
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)