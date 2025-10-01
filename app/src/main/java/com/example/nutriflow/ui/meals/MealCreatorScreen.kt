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
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
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
                IngredientSelector(viewModel, state) // Pasamos todo el estado
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
    state: PlateCreatorState
) {
    val addedIngredientNames = state.components.map { it.ingredient.name }

    Card(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Añadir Ingredientes", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            // Campo de búsqueda principal
            OutlinedTextField(
                value = state.ingredientSearchQuery,
                onValueChange = viewModel::onIngredientSearchQueryChange,
                label = { Text("Buscar Ingrediente") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true
            )

            // Si hay un ingrediente seleccionado, mostramos el campo de cantidad y el botón Añadir
            state.selectedIngredientForAdding?.let { ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Nombre del ingrediente seleccionado
                    Text(ingredient.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)

                    // Campo de cantidad
                    OutlinedTextField(
                        value = state.ingredientAmount,
                        onValueChange = viewModel::onIngredientAmountChange,
                        label = { Text("Gramos") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp),
                        singleLine = true,
                        trailingIcon = { Text("g", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )

                    // Botón de Añadir (llama a la función sin argumentos)
                    IconButton(
                        onClick = viewModel::addIngredientToPlate,
                        enabled = state.ingredientAmount.toDoubleOrNull()?.let { it > 0 } ?: false,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Añadir al Plato", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Lista de resultados de búsqueda o lista completa
            if (state.availableIngredients.isEmpty()) {
                Text("No hay ingredientes registrados. Ve a la sección 'Ingredientes' para crear uno.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    val listToShow = if (state.ingredientSearchQuery.isBlank()) {
                        state.availableIngredients
                    } else {
                        state.filteredIngredients
                    }

                    items(listToShow) { ingredient ->
                        val isAdded = addedIngredientNames.contains(ingredient.name)
                        // ✅ CORRECCIÓN: Llama a selectIngredientForInput, no addIngredientToPlate
                        IngredientSelectListItem(
                            ingredient = ingredient,
                            isAdded = isAdded,
                            onSelect = { viewModel.selectIngredientForInput(ingredient) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientSelectListItem(ingredient: Ingredient, isAdded: Boolean, onSelect: () -> Unit) {
    val isSelectedForInput = LocalViewModelStoreOwner.current?.let {
        (viewModel<MealCreatorViewModel>().uiState.collectAsState().value.selectedIngredientForAdding == ingredient)
    } ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectedForInput) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isAdded, onClick = onSelect)
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
                Icon(Icons.Default.Add, contentDescription = "Seleccionar")
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