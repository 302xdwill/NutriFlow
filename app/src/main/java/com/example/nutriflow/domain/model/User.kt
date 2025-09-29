package com.example.nutriflow.domain.model

data class User(
    val id: String = "",
    val name: String = "", // <-- ESTE DEBE EXISTIR
    val lastName: String = "", // <-- ESTE DEBE EXISTIR
    val email: String = "",
    val age: Int,
    val weight: Double,
    val height: Double,
    val photoUrl: String?,
    val calorieGoal: Double,
    val proteinGoal: Double,
    val carbsGoal: Double,
    val fatGoal: Double
)