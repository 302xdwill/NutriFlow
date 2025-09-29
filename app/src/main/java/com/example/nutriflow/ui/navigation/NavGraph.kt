package com.example.nutriflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutriflow.ui.auth.AuthViewModel
import com.example.nutriflow.ui.auth.LoginScreen
import com.example.nutriflow.ui.auth.RegisterScreen
import com.example.nutriflow.ui.home.HomeScreen

@Composable
fun NavGraph(
    viewModel: AuthViewModel
) {
    val navController = rememberNavController()
    // Observamos el estado del usuario para decidir la ruta inicial
    val currentUser by viewModel.currentUser.collectAsState()

    // ✅ Determinamos la ruta de inicio al iniciar la app
    val startDestination = if (currentUser != null) {
        Screen.MainContainer.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, viewModel = viewModel, onSuccess = {
                // Cuando el login es exitoso, navegamos al contenedor principal
                navController.navigate(Screen.MainContainer.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, viewModel = viewModel, onSuccess = {
                navController.navigate(Screen.Login.route)
            })
        }

        composable(Screen.MainContainer.route) {
            HomeScreen(
                authViewModel = viewModel,
                // ✅ IMPLEMENTACIÓN DEL CIERRE DE SESIÓN: Navega a Login y limpia la pila
                onLogoutSuccessNavigation = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.MainContainer.route) { inclusive = true }
                    }
                }
            )
        }
    }
}