package com.example.nutriflow.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = viewModel(),
    onGoalsSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1. Manejo del éxito del guardado (NAVEGACIÓN)
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.resetSaveSuccess()
            onGoalsSaved() // Navegar de vuelta
        }
    }

    // 2. Manejo de errores (FEEDBACK)
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error, "OK")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Metas Nutricionales") },
                navigationIcon = {
                    IconButton(onClick = onGoalsSaved) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::saveGoals,
                        enabled = !state.isSaving && !state.isLoading
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar Metas")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopCenter // Cambiado a TopCenter para el formulario
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 64.dp))
            } else {
                GoalsForm(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GoalsForm(state: GoalsState, viewModel: GoalsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Define tus objetivos diarios:",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Campos de entrada
        GoalInputField(
            label = "Objetivo de Calorías (kcal)",
            value = state.calorieGoalInput,
            onValueChange = viewModel::onCalorieGoalChange,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GoalInputField(
            label = "Objetivo de Proteína (g)",
            value = state.proteinGoalInput,
            onValueChange = viewModel::onProteinGoalChange,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GoalInputField(
            label = "Objetivo de Carbohidratos (g)",
            value = state.carbsGoalInput,
            onValueChange = viewModel::onCarbsGoalChange,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GoalInputField(
            label = "Objetivo de Grasa (g)",
            value = state.fatGoalInput,
            onValueChange = viewModel::onFatGoalChange,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = viewModel::saveGoals,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Guardar Cambios")
            }
        }
    }
}

@Composable
fun GoalInputField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange, // La validación se hace en el ViewModel
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        singleLine = true
    )
}