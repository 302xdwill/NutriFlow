package com.example.nutriflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.nutriflow.data.local.database.NutriFlowDatabase // ✅ Asumo que esta clase existe
import com.example.nutriflow.data.repository.UserRepositoryImpl // ✅ Asumo tus implementaciones
import com.example.nutriflow.data.repository.GoalsRepositoryImpl // ✅ Asumo tus implementaciones
import com.example.nutriflow.domain.usecase.LoginUserUseCase
import com.example.nutriflow.domain.usecase.RegisterUserUseCase
import com.example.nutriflow.ui.auth.AuthViewModel
import com.example.nutriflow.ui.auth.AuthViewModelFactory // ✅ Nueva Importación
import com.example.nutriflow.ui.navigation.NavGraph
import com.example.nutriflow.ui.theme.NutriFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar la base de datos y repositorios
        val database = NutriFlowDatabase.getDatabase(applicationContext)

        val userRepository = UserRepositoryImpl(database.userDao())
        val goalsRepository = GoalsRepositoryImpl(database.goalDao())

        // 2. Inicializar Casos de Uso
        val registerUserUseCase = RegisterUserUseCase(userRepository) // Adapta si tus casos de uso necesitan más
        val loginUserUseCase = LoginUserUseCase(userRepository) // Adapta si tus casos de uso necesitan más

        // 3. Crear el Factory con las dependencias
        val authViewModelFactory = AuthViewModelFactory(
            userRepository = userRepository,
            goalsRepository = goalsRepository,
            registerUserUseCase = registerUserUseCase,
            loginUserUseCase = loginUserUseCase
        )

        // 4. Creamos la instancia del ViewModel usando el Factory
        // ESTA ES LA CORRECCIÓN DE LA LÍNEA 16
        val authViewModel = ViewModelProvider(this, authViewModelFactory)[AuthViewModel::class.java]

        setContent {
            NutriFlowTheme {
                // Aquí llamamos a nuestro NavGraph
                NavGraph(viewModel = authViewModel)
            }
        }
    }
}