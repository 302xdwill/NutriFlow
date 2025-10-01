package com.example.nutriflow.domain.model

// Usa Double para la precisión de los gramos y calorías
data class DailySummary(
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0, // Cambiado de totalMinerals a Fat
    val calorieGoal: Double = 2000.0,
    val proteinGoal: Double = 100.0,
    val carbsGoal: Double = 250.0,
    val fatGoal: Double = 70.0 // Cambiado de mineralsGoal a Fat
)