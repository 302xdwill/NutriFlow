package com.example.nutriflow.domain.model

// Usa Double para la precisión de los gramos y calorías
data class DailySummary(
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalMinerals: Double = 0.0, // Asumimos que estás usando un campo 'minerals'

    val calorieGoal: Double = 0.0,
    val proteinGoal: Double = 0.0,
    val carbsGoal: Double = 0.0,
    val mineralsGoal: Double = 0.0
)