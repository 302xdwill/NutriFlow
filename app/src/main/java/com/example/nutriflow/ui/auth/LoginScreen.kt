package com.example.nutriflow.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Importación necesaria
import com.example.nutriflow.ui.navigation.Screen // Asegúrate de que esta clase contenga Screen.Dashboard

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    onSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // 1. Observar el estado de autenticación del ViewModel
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var localError by remember { mutableStateOf<String?>(null) } // Error para mostrar en UI
    val isLoading = authState is AuthViewModel.AuthState.Loading

    // 2. Efecto lateral para manejar el éxito y el error de autenticación
    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Success -> {
                // Navegar al dashboard
                navController.navigate(Screen.MainContainer.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is AuthViewModel.AuthState.Error -> {
                // Mostrar error del ViewModel
                localError = (authState as AuthViewModel.AuthState.Error).message
            }
            else -> {
                // Limpiar errores si el estado es Idle o Loading
                localError = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Bienvenido de Nuevo",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; localError = null }, // Limpiar error al escribir
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            isError = localError != null
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; localError = null }, // Limpiar error al escribir
            label = { Text("Contraseña") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector  = image, contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = localError != null
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar error si existe
        localError?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            // 3. LLAMADA CORREGIDA AL VIEWMODEL
            onClick = { viewModel.loginUser(email, password) },
            modifier = Modifier.fillMaxWidth(),
            // Deshabilitar si está cargando o si los campos están vacíos
            enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar Sesión")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
            Text("¿No tienes una cuenta? Regístrate")
        }
    }
}