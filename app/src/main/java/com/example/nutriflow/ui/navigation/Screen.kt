package com.example.nutriflow.ui.navigation

sealed class Screen(val route: String) {
    // ------------------------------------------
    // Rutas de Autenticación
    // ------------------------------------------
    data object Login : Screen("login")
    data object Register : Screen("register")

    // ------------------------------------------
    // Contenedor Principal (Host del Bottom Navigation)
    // Este es el destino al que navegas después del Login.
    // ------------------------------------------
    data object MainContainer : Screen("main_container")

    // ------------------------------------------
    // Rutas Internas del Bottom Nav (Navegación dentro de MainContainer)
    // ------------------------------------------
    data object Schedule : Screen("schedule_route")       // Resumen Diario
    data object Ingredients : Screen("ingredients_route") // Registro de Ingredientes
    data object Profile : Screen("profile_route")         // Perfil/Ajustes

    // ------------------------------------------
    // Rutas de Navegación Profunda (Destinos sin Bottom Nav)
    // ------------------------------------------
    data object MealCreator : Screen("meal_creator") // Navegación desde Schedule o Ingredients
    data object Stats : Screen("stats")              // Navegación desde Profile
    data object Goals : Screen("goals")              // Navegación desde Profile
}