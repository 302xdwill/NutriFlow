package com.example.nutriflow.ui.ingredients

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriflow.domain.model.Ingredient


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientScreen(viewModel: IngredientViewModel = viewModel()) {
    // 1. Estados del ViewModel
    val formState by viewModel.formState.collectAsState()
    val ingredients by viewModel.ingredients.collectAsState()

    // 2. Efecto para mostrar el Toast de éxito
    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) {
            // Muestra un mensaje de éxito
            // En una aplicación real, usarías un Snackbar o Toast
            println("Ingrediente ${formState.name} guardado con éxito.")
            viewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mis Ingredientes") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sección de Formulario
            IngredientForm(viewModel, formState)

            Spacer(modifier = Modifier.height(16.dp))

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // Sección de Lista
            IngredientList(ingredients)
        }
    }
}

@Composable
fun IngredientForm(viewModel: IngredientViewModel, state: IngredientFormState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Registrar Nuevo Ingrediente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo Nombre
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nombre del Ingrediente (Ej: Pechuga de Pollo)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            // Selector de Tipo de Macro
            MacroTypeSelector(
                selectedType = state.type,
                onTypeSelected = viewModel::onTypeChange
            )

            // Campo Calorías por Gramo
            OutlinedTextField(
                value = state.calPerGram,
                onValueChange = viewModel::onCalPerGramChange,
                label = { Text("Calorías por Gramo (Ej: 1.65)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                trailingIcon = { Text("kcal/g", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            )

            // Mensajes de Error/Éxito
            AnimatedVisibility(visible = state.errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Botón Guardar
            Button(
                onClick = viewModel::saveIngredient,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Ingrediente")
                }
            }
        }
    }
}

@Composable
fun MacroTypeSelector(selectedType: String, onTypeSelected: (String) -> Unit) {
    val types = listOf("PROTEIN", "CARB", "FAT", "MINERAL")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        types.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(type.take(4)) } // Muestra solo 4 letras para ahorrar espacio
            )
        }
    }
}

@Composable
fun IngredientList(ingredients: List<Ingredient>) {
    Text(
        text = "Ingredientes Registrados (${ingredients.size})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
    )

    if (ingredients.isEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = "Información")
            Spacer(Modifier.width(8.dp))
            Text("No hay ingredientes registrados aún.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(ingredients) { ingredient ->
                IngredientListItem(ingredient)
            }
        }
    }
}

@Composable
fun IngredientListItem(ingredient: Ingredient) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = ingredient.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tipo: ${ingredient.type}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "${"%.2f".format(ingredient.caloriesPerGram)} kcal/g",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}