package com.example.nutriflow.ui.ingredients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriflow.domain.model.Ingredient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientScreen(viewModel: IngredientViewModel = viewModel()) {
    // 1. Estados del ViewModel
    val formState by viewModel.formState.collectAsState()
    // Obtenemos la lista filtrada de la UI
    val filteredIngredients by viewModel.filteredIngredients.collectAsState()
    val allIngredients by viewModel.ingredients.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState() // Nuevo estado para el filtro

    // 2. Efecto para mostrar el Toast de éxito
    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) {
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
            // Sección de Formulario (No se toca, solo cambiamos el MacroTypeSelector)
            IngredientForm(viewModel, formState)

            Spacer(modifier = Modifier.height(16.dp))

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            // Sección de Lista y Filtro
            IngredientList(
                ingredients = filteredIngredients, // Usamos la lista filtrada
                totalCount = allIngredients.size,
                selectedFilter = selectedFilter,
                onFilterSelected = viewModel::onFilterChange
            )
        }
    }
}

// ... (Código de IngredientForm se mantiene igual)

@Composable
fun MacroTypeSelector(selectedType: String, onTypeSelected: (String) -> Unit) {
    // Tipos de nutrientes: Añadimos etiquetas amigables para el usuario
    val types = mapOf(
        "PROTEIN" to "PROT",
        "CARB" to "CARB",
        "FAT" to "GRASA", // Cambiado de FAT a GRASA
        "MINERAL" to "MINE"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        types.forEach { (typeKey, typeLabel) ->
            FilterChip(
                selected = selectedType == typeKey,
                onClick = { onTypeSelected(typeKey) },
                label = { Text(typeLabel) } // Muestra la etiqueta amigable
            )
        }
    }
}

@Composable
fun IngredientList(
    ingredients: List<Ingredient>,
    totalCount: Int,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingredientes Registrados ($totalCount)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            // Botón de filtro para la lista
            Icon(
                Icons.Default.FilterList,
                contentDescription = "Filtrar por tipo",
                modifier = Modifier.clickable { onFilterSelected(if (selectedFilter.isNullOrBlank()) "PROTEIN" else "") } // Alternar el filtro
            )
        }

        // Chips de Filtro de Lista
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val types = listOf("TODOS", "PROTEIN", "CARB", "FAT", "MINERAL")
            types.forEach { type ->
                FilterChip(
                    selected = selectedFilter == type || (type == "TODOS" && selectedFilter.isBlank()),
                    onClick = { onFilterSelected(if (type == "TODOS") "" else type) },
                    label = { Text(type.take(4), style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }


    if (ingredients.isEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = "Información", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(
                if (selectedFilter.isBlank()) "No hay ingredientes registrados aún." else "No hay ingredientes de este tipo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    // ... (El código de IngredientListItem se mantiene igual, ya tiene un buen estilo)
    Card(
        // ...
    ) {
        Row(
            // ...
        ) {
            Column {
                Text(
                    text = ingredient.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Tipo: ${when(ingredient.type) {
                        "PROTEIN" -> "PROT"
                        "CARB" -> "CARB"
                        "FAT" -> "GRASA"
                        "MINERAL" -> "MINE"
                        else -> ingredient.type
                    }}",
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
@Composable
fun IngredientForm(viewModel: IngredientViewModel, state: IngredientFormState) {
    // Asumo que tienes la definición de IngredientFormState en tu otro archivo
    // y que lo corregiste como te indiqué en el paso anterior.
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Registrar Nuevo Ingrediente", fontWeight = FontWeight.Bold)

        // Campo de Nombre
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Nombre del Ingrediente") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        // Campo de Calorías por Gramo
        OutlinedTextField(
            value = state.calPerGram,
            onValueChange = viewModel::onCalPerGramChange,
            label = { Text("Kcal por Gramo (ej: 4.0)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        // Selector de Tipo de Macro
        MacroTypeSelector(
            selectedType = state.type,
            onTypeSelected = viewModel::onTypeChange
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botón de Guardar
        Button(
            onClick = viewModel::saveIngredient,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Guardando..." else "Guardar Ingrediente")
        }

        // Manejo de Error
        state.errorMessage?.let { error ->
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
    }
}