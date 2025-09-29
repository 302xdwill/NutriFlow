package com.example.nutriflow.ui.meals

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriflow.domain.model.Meal
import com.example.nutriflow.ui.auth.AuthViewModel
import java.util.Date
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.nutriflow.domain.model.User // Asegurando la importación del modelo User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    mealViewModel: MealViewModel = viewModel<MealViewModel>(),
    authViewModel: AuthViewModel = viewModel<AuthViewModel>(),
    onMealSaved: () -> Unit
) {
    // Es mejor usar un valor inicial explícito para StateFlows
    val currentUser by authViewModel.currentUser.collectAsState(initial = null)
    val context = LocalContext.current

    // Estados para los campos de la comida
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    val type = "Manual"

    Scaffold(
        topBar = { TopAppBar(title = { Text("Registrar Nuevo Plato") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nombre del Plato
            OutlinedTextField(
                value = name,
                onValueChange = { name = it }, // ✅ Uso correcto de onValueChange
                label = { Text("Nombre del Plato") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Calorías (Línea 50 del error)
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it.filter { char -> char.isDigit() || char == '.' } }, // ✅ Uso correcto de onValueChange
                label = { Text("Calorías (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Proteína
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it.filter { char -> char.isDigit() || char == '.' } }, // ✅ Uso correcto de onValueChange
                label = { Text("Proteína (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Carbohidratos
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it.filter { char -> char.isDigit() || char == '.' } }, // ✅ Uso correcto de onValueChange
                label = { Text("Carbohidratos (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Grasas/Minerales
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it.filter { char -> char.isDigit() || char == '.' } }, // ✅ Uso correcto de onValueChange
                label = { Text("Grasas/Minerales (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Aseguramos el tipo del usuario para obtener el email
                    val userEmail = (currentUser as? User)?.email

                    if (userEmail.isNullOrBlank() || name.isBlank() || calories.isBlank()) {
                        Toast.makeText(context, "Error: Rellene campos y asegure sesión.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val newMeal = Meal(
                        userId = userEmail,
                        name = name.trim(),
                        type = type,
                        calories = calories.toDoubleOrNull() ?: 0.0,
                        proteinGrams = protein.toDoubleOrNull() ?: 0.0,
                        carbsGrams = carbs.toDoubleOrNull() ?: 0.0,
                        mineralsGrams = fat.toDoubleOrNull() ?: 0.0,
                        date = Date(),
                        scheduledTime = Date().time,
                        reminderTime = null
                    )

                    mealViewModel.saveMeal(newMeal) { success ->
                        if (success) {
                            Toast.makeText(context, "✅ ¡Plato registrado con éxito!", Toast.LENGTH_SHORT).show()
                            onMealSaved()
                        } else {
                            Toast.makeText(context, "❌ Error al guardar el plato.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar Plato")
            }
        }
    }
}