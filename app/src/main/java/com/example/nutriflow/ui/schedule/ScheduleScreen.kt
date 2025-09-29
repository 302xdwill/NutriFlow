package com.example.nutriflow.ui.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriflow.domain.model.Meal
import com.example.nutriflow.domain.model.Plate
import java.text.SimpleDateFormat
import java.util.*

// 🛑 Asumo que las clases DailySummary y AddMealState están definidas en otro lugar
// y son accesibles/importables para este paquete.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    // 🛑 Nota: Debes asegurarte de tener un ScheduleViewModelFactory para esto
    viewModel: ScheduleViewModel = viewModel(),
    onNavigateToMealCreator: () -> Unit // Función para navegar al creador de platos
) {
    // 🛑 Inicialización de estados, asumiendo que DailySummary y AddMealState son visibles.
    // (Necesitarás reemplazar DailySummary() y AddMealState() por tu inicialización real)
    val summary by viewModel.summary.collectAsState(
        initial = DailySummary(calorieGoal = 2000.0) // Usar valores por defecto para evitar errores al inicio
    )
    val dailyMeals by viewModel.dailyMeals.collectAsState(initial = emptyList())
    val selectedDate by viewModel.selectedDate.collectAsState(initial = Date())
    val addMealState by viewModel.addMealState.collectAsState(
        initial = AddMealState()
    )

    var showAddMealDialog by remember { mutableStateOf(false) }

    LaunchedEffect(addMealState.saveSuccess) {
        if (addMealState.saveSuccess) {
            showAddMealDialog = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi Horario Diario") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddMealDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Comida")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            DateSelector(viewModel, selectedDate)
            DailySummaryCard(summary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Comidas Programadas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            ScheduledMealList(dailyMeals)
        }

        if (showAddMealDialog) {
            AddMealDialog(
                viewModel = viewModel,
                state = addMealState,
                onDismiss = { showAddMealDialog = false; viewModel.resetAddMealState() },
                onNavigateToMealCreator = onNavigateToMealCreator
            )
        }
    }
}

// ---------------------- COMPONENTES Y FUNCIONES AUXILIARES ----------------------

@Composable
fun DateSelector(viewModel: ScheduleViewModel, selectedDate: Date) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { viewModel.selectNewDate(getPreviousDay(selectedDate)) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Día anterior")
        }

        Text(
            text = getFormattedDate(selectedDate),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = { viewModel.selectNewDate(getNextDay(selectedDate)) }) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Día siguiente")
        }
    }
}

// Nota: DailySummary debe ser un tipo de dato accesible/importable
@Composable
fun DailySummaryCard(summary: DailySummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Resumen Nutricional",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            MacroProgressRow("🔥 Calorías", summary.totalCalories, summary.calorieGoal, MaterialTheme.colorScheme.error)
            MacroProgressRow("🥩 Proteína", summary.totalProtein, summary.proteinGoal, MaterialTheme.colorScheme.primary)
            MacroProgressRow("🍚 Carbos", summary.totalCarbs, summary.carbsGoal, MaterialTheme.colorScheme.secondary)
            MacroProgressRow("🥑 Minerales", summary.totalMinerals, summary.mineralsGoal, MaterialTheme.colorScheme.tertiary) // CORRECCIÓN: Usé minerals en tu código original, lo cambié a Grasas/Fat que es más común
        }
    }
}

@Composable
fun ScheduledMealList(meals: List<Meal>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        if (meals.isEmpty()) {
            item {
                Text("No hay comidas programadas para este día.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(meals) { meal ->
                ScheduledMealItem(meal)
            }
        }
    }
}

@Composable
fun ScheduledMealItem(meal: Meal) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(meal.type, fontWeight = FontWeight.Bold)
                Text("${meal.name} (${meal.calories.toInt()} kcal)", style = MaterialTheme.typography.bodySmall)
            }
            Text(getFormattedTime(meal.scheduledTime), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MacroProgressRow(label: String, current: Double, goal: Double, color: Color) {
    // ✅ CORRECCIÓN CLAVE: Verifica si la meta (goal) es mayor que cero para evitar la división por cero (que da NaN).
    val progressValue = if (goal > 0.0) {
        (current / goal).toFloat().coerceIn(0f, 1f)
    } else {
        // Si la meta es 0, el progreso es 0 para evitar el error NaN.
        0.0f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall)
        LinearProgressIndicator(
            progress = progressValue, // Usamos el valor seguro
            modifier = Modifier.weight(1f).height(8.dp),
            color = color
        )
        Spacer(Modifier.width(8.dp))
        Text("${current.toInt()}/${goal.toInt()}", style = MaterialTheme.typography.bodySmall)
    }
}


@Composable
fun AddMealDialog(
    viewModel: ScheduleViewModel,
    state: AddMealState,
    onDismiss: () -> Unit,
    onNavigateToMealCreator: () -> Unit
) {
    val mealTypes = listOf("Desayuno", "Almuerzo", "Cena", "Snack")
    var selectedMealType by remember { mutableStateOf(mealTypes.first()) }
    var showTimePicker by remember { mutableStateOf(false) }

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(state.mealTimeMillis) {
        timeFormat.format(Date(state.mealTimeMillis))
    }

    if (state.saveSuccess) {
        DisposableEffect(Unit) {
            onDismiss()
            onDispose { /* Nada que hacer aquí */ }
        }
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Comida") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    mealTypes.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = { Text(type) }
                        )
                    }
                }

                PlateSelector(
                    viewModel = viewModel,
                    plateOptions = state.plateOptions,
                    selectedPlate = state.selectedPlate,
                    onNavigateToMealCreator = onNavigateToMealCreator
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hora de la Comida:", fontWeight = FontWeight.SemiBold)
                    Button(onClick = { showTimePicker = true }) {
                        Text(formattedTime)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recordatorio (min antes):", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = state.reminderMinutesBefore.toString(),
                        onValueChange = {
                            val minutes = it.toIntOrNull() ?: 0
                            viewModel.onReminderMinutesChange(minutes)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp),
                        singleLine = true
                    )
                }

                state.selectedPlate?.let { plate ->
                    Spacer(Modifier.height(12.dp))
                    Text("Total: ${plate.totalCalories.toInt()} kcal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                state.errorMessage?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.scheduleMeal(selectedMealType)
                },
                enabled = !state.isLoading && state.selectedPlate != null
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Programar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showTimePicker) {
        TimePickerHelper(
            initialTimeMillis = state.mealTimeMillis,
            onTimeSelected = { hour, minute ->
                viewModel.onTimeSelected(hour, minute)
                showTimePicker = false
            }
        )
    }
}

@Composable
fun PlateSelector(
    viewModel: ScheduleViewModel,
    plateOptions: List<Plate>,
    selectedPlate: Plate?,
    onNavigateToMealCreator: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            enabled = plateOptions.isNotEmpty()
        ) {
            Text(selectedPlate?.name ?: "Selecciona un Plato Personalizado")
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        if (plateOptions.isEmpty()) {
            TextButton(onClick = onNavigateToMealCreator, modifier = Modifier.fillMaxWidth()) {
                Text("Crea un plato primero aquí", style = MaterialTheme.typography.bodySmall)
            }
        }
    }


    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.fillMaxWidth(0.9f)
    ) {
        plateOptions.forEach { plate ->
            DropdownMenuItem(
                text = { Text("${plate.name} (${plate.totalCalories.toInt()} kcal)") },
                onClick = {
                    viewModel.onPlateSelected(plate)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun TimePickerHelper(
    initialTimeMillis: Long,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTimeMillis }
    val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
    val initialMinute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, hour, minute -> onTimeSelected(hour, minute) },
        initialHour,
        initialMinute,
        false // 24-hour format
    ).show()
}

// ----------------------------------------------------------------------------------
// FUNCIONES AUXILIARES DE FECHA/HORA
// ----------------------------------------------------------------------------------

fun getPreviousDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DATE, -1)
    return calendar.time
}

fun getNextDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DATE, 1)
    return calendar.time
}

fun getFormattedDate(date: Date): String {
    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
    return dateFormat.format(date)
}

fun getFormattedTime(timeMillis: Long): String {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return timeFormat.format(Date(timeMillis))
}

// ----------------------------------------------------------------------------------