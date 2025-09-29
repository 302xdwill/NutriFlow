package com.example.nutriflow.ui.meals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriflow.domain.model.Ingredient
import com.example.nutriflow.domain.model.PlateIngredient
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealCreatorScreen(
    viewModel: MealCreatorViewModel = viewModel(),
    onPlateSaved: () -> Unit // Navegación de vuelta al horario
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 1. Manejo del éxito del guardado
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.resetForm() // Limpia el formulario
            onPlateSaved()       // Navega de vuelta
            scope.launch {
                snackbarHostState.showSnackbar("Plato '${state.plateName}' guardado con éxito.")
            }
            viewModel.resetSaveSuccess()
        }
    }

    // 2. Manejo de errores
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
                title = { Text("Armar Mi Plato") },
                actions = {
                    IconButton(
                        onClick = viewModel::savePlate,
                        enabled = !state.isSaving && state.components.isNotEmpty() && state.plateName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar Plato")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 3. Tarjeta de Resumen (siempre visible)
            PlateSummaryCard(state)

            // 4. Secciones de Creación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ocupa el espacio restante
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Columna 1: Formulario de Detalle y Componentes (60% ancho)
                Column(modifier = Modifier.weight(0.6f)) {
                    PlateDetailsForm(viewModel, state)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Componentes del Plato", style = MaterialTheme.typography.titleMedium)
                    ComponentList(viewModel, state.components)
                }
                // Columna 2: Selector de Ingredientes (40% ancho)
                IngredientSelector(viewModel, state.availableIngredients, state.components.map { it.ingredient.name })
            }
        }
    }
}

// ---------------------- COMPONENTES ----------------------

@Composable
fun PlateSummaryCard(state: PlateCreatorState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = state.plateName.ifBlank { "Nuevo Plato" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("🔥 Calorías:", "${state.totalCalories.toInt()} kcal")
                SummaryItem("🥩 Proteína:", "${state.totalProtein.toInt()} g")
                SummaryItem("🍚 Carbos:", "${state.totalCarbs.toInt()} g")
                SummaryItem("🥑 Grasa:", "${state.totalFat.toInt()} g")
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(value, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun PlateDetailsForm(viewModel: MealCreatorViewModel, state: PlateCreatorState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = state.plateName,
            onValueChange = viewModel::onNameChange,
            label = { Text("Nombre del Plato") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = state.plateDescription,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Descripción (Opcional)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun IngredientSelector(
    viewModel: MealCreatorViewModel,
    availableIngredients: List<Ingredient>,
    addedIngredientNames: List<String>
) {
    // Implementación simple: lista todos los ingredientes disponibles
    // Se puede mejorar con un campo de búsqueda o filtros por tipo.
    Card(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Añadir Ingredientes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (availableIngredients.isEmpty()) {
                Text("No hay ingredientes registrados. Ve a la sección 'Ingredientes' para crear uno.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn {
                    items(availableIngredients) { ingredient ->
                        val isAdded = addedIngredientNames.contains(ingredient.name)
                        IngredientSelectListItem(
                            ingredient = ingredient,
                            isAdded = isAdded,
                            onAdd = { viewModel.addIngredientToPlate(ingredient) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientSelectListItem(ingredient: Ingredient, isAdded: Boolean, onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isAdded, onClick = onAdd)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(ingredient.name, fontWeight = FontWeight.SemiBold)
                Text("${ingredient.type} | ${"%.2f".format(ingredient.caloriesPerGram)} kcal/g", style = MaterialTheme.typography.bodySmall)
            }
            if (isAdded) {
                Icon(Icons.Default.Done, contentDescription = "Añadido", tint = MaterialTheme.colorScheme.secondary)
            } else {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    }
}

@Composable
fun ComponentList(viewModel: MealCreatorViewModel, components: List<PlateIngredient>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(components) { index, component ->
            ComponentItem(viewModel, index, component)
        }
    }
}

@Composable
fun ComponentItem(viewModel: MealCreatorViewModel, index: Int, component: PlateIngredient) {
    val currentWeight = component.weightGrams

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(0.4f)) {
                Text(component.ingredient.name, fontWeight = FontWeight.Bold)
                Text("Tipo: ${component.ingredient.type}", style = MaterialTheme.typography.bodySmall)
            }

            // Campo de Peso en Gramos
            OutlinedTextField(
                value = if (currentWeight == 0.0) "" else currentWeight.toString(),
                onValueChange = { viewModel.updateComponentWeight(index, it) },
                label = { Text("Peso (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(100.dp),
                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.SemiBold),
                singleLine = true
            )

            // Calorías calculadas para este componente
            val componentCalories = component.weightGrams * component.ingredient.caloriesPerGram
            Text(
                "${componentCalories.toInt()} kcal",
                modifier = Modifier.width(80.dp),
                color = MaterialTheme.colorScheme.secondary
            )

            IconButton(onClick = { viewModel.removeComponent(component) }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}