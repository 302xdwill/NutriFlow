package com.example.nutriflow.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nutriflow.ui.auth.AuthViewModel
import com.example.nutriflow.ui.ingredients.IngredientScreen
import com.example.nutriflow.ui.meals.MealCreatorScreen
import com.example.nutriflow.ui.profile.ProfileScreen
import com.example.nutriflow.ui.schedule.ScheduleScreen
import com.example.nutriflow.ui.navigation.Screen
import com.example.nutriflow.ui.profile.GoalsScreen

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    // ✅ Nuevo: Callback de navegación para cerrar sesión
    onLogoutSuccessNavigation: () -> Unit
) {
    val navController = rememberNavController()

    // 1. Define los destinos y sus etiquetas en ESPAÑOL
    val navItems = listOf(
        Pair(Screen.Schedule, "Horario"),
        Pair(Screen.Ingredients, "Ingredientes"),
        Pair(Screen.Profile, "Perfil")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { (screen, label) -> // Usamos el par (screen, label)
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                Screen.Schedule -> Icon(Icons.Default.DateRange, contentDescription = null)
                                Screen.Ingredients -> Icon(Icons.Default.Fastfood, contentDescription = null)
                                Screen.Profile -> Icon(Icons.Default.Person, contentDescription = null)
                                else -> {}
                            }
                        },
                        // ✅ Usamos la etiqueta en español
                        label = { Text(label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        // 2. Define el NavHost interno para las pantallas de la aplicación
        NavHost(
            navController = navController,
            startDestination = Screen.Schedule.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Rutas de la Barra Inferior
            composable(Screen.Schedule.route) {
                ScheduleScreen {
                    navController.navigate(Screen.MealCreator.route)
                }
            }
            composable(Screen.Ingredients.route) {
                IngredientScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onNavigateToGoals = { navController.navigate(Screen.Goals.route) },
                    onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                    // ✅ Pasamos el callback de navegación al ProfileScreen
                    onLogoutSuccessNavigation = onLogoutSuccessNavigation
                )
            }

            // Rutas de Navegación Profunda (sin Bottom Nav)
            composable(Screen.MealCreator.route) {
                MealCreatorScreen(onPlateSaved = { navController.popBackStack() })
            }

            composable(Screen.Goals.route) {
                // ✅ Usamos el GoalsScreen real
                GoalsScreen(onGoalsSaved = { navController.popBackStack() })
            }

            composable(Screen.Stats.route) {
                // ✅ Nuevo: Ruta para Estadísticas (Usamos un placeholder)
                Text("Pantalla de Estadísticas (StatsScreen)", modifier = Modifier.padding(16.dp))
            }
        }
    }
}