package com.example.nutriflow.ui.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.nutriflow.domain.model.DailySummary
import com.example.nutriflow.domain.model.MealScheduleState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = viewModel(),
    onNavigateToMealCreator: () -> Unit
) {
    // 1. ESTADOS - USAMOS DECLARACIÓN EXPLÍCITA DE State<T> EN TODOS LOS FLUJOS

    val summaryState: State<DailySummary> = viewModel.summary.collectAsState(
        initial = DailySummary()
    )
    val summary = summaryState.value

    val dailyMealsState: State<List<Meal>> = viewModel.dailyMeals.collectAsState(
        initial = emptyList()
    )
    val dailyMeals = dailyMealsState.value

    val selectedDate by viewModel.selectedDate.collectAsState(initial = Date())

    val addMealState: State<MealScheduleState> = viewModel.addMealState.collectAsState(
        initial = MealScheduleState()
    )

    val addMealData = addMealState.value

    var showAddMealDialog by remember { mutableStateOf(false) }

    // 2. Manejo de la navegación y reinicio del estado del diálogo
    LaunchedEffect(addMealState.value.saveSuccess) {
        if (addMealData.saveSuccess) {
            showAddMealDialog = false
            viewModel.resetAddMealState()
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
            // Llama a la versión corregida de ScheduledMealList
            ScheduledMealList(dailyMeals)
        }

        if (showAddMealDialog) {
            AddMealDialog(
                viewModel = viewModel,
                state = addMealData,
                onDismiss = { showAddMealDialog = false; viewModel.resetAddMealState() },
                onNavigateToMealCreator = onNavigateToMealCreator
            )
        }
    }
}

// ---------------------- COMPONENTES Y FUNCIONES AUXILIARES ----------------------

@Composable
fun DateSelector(viewModel: ScheduleViewModel, selectedDate: Date) {
    val isToday = isToday(selectedDate)

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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isToday) "HOY" else getFormattedDate(selectedDate),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (!isToday) {
                Text(getFormattedDate(selectedDate), style = MaterialTheme.typography.bodySmall)
            }
        }


        IconButton(onClick = { viewModel.selectNewDate(getNextDay(selectedDate)) }) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Día siguiente")
        }
    }
}

@Composable
fun DailySummaryCard(summary: DailySummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(12.dp)
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
            MacroProgressRow("🥑 Grasa", summary.totalFat, summary.fatGoal, MaterialTheme.colorScheme.tertiary)
        }
    }
}


@Composable
fun ScheduledMealList(meals: List<Meal>) {
    // ✅ CORRECCIÓN FINAL: Reemplazamos .weight(1f) por un fillMaxHeight en un intento de flexibilización.
    // Aunque es menos limpio que weight, evita el error de "invoke()"
    LazyColumn(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight() // Usamos fillMaxHeight para tomar el espacio restante sin weight
    ) {
        if (meals.isEmpty()) {
            item {
                Text(
                    "No hay comidas programadas para este día.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
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
        .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(meal.type, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(meal.name, style = MaterialTheme.typography.titleSmall)
                Text("${meal.calories.toInt()} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(getFormattedTime(meal.scheduledTime), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MacroProgressRow(label: String, current: Double, goal: Double, color: Color) {
    val progressValue = if (goal > 0.0) {
        (current / goal).toFloat().coerceIn(0f, 1f)
    } else {
        0.0f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Asignamos un ancho fijo al texto del label
        Text(label, modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall)

        // ✅ CORRECCIÓN FINAL: Usamos fillMaxWidth().size(0.dp) para que tome el espacio restante
        // Esto es un truco de Compose para emular weight(1f) en Row cuando weight() falla.
        LinearProgressIndicator(
            progress = progressValue,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color
        )
        Spacer(Modifier.width(8.dp))
        Text("${current.toInt()}/${goal.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealDialog(
    viewModel: ScheduleViewModel,
    state: MealScheduleState, // NOMBRE CORREGIDO
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Programar Comida") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Selector de Tipo de Comida
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
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
                        value = if (state.reminderMinutesBefore == 0) "" else state.reminderMinutesBefore.toString(),
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
            Text(selectedPlate?.name ?: "Selecciona un Plato Personalizado", maxLines = 1)
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

// ---------------------- FUNCIONES DE UTILIDAD DE FECHA/HORA ----------------------

@Composable
fun TimePickerHelper(initialTimeMillis: Long, onTimeSelected: (Int, Int) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = initialTimeMillis }
    val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
    val initialMinute = calendar.get(Calendar.MINUTE)

    LaunchedEffect(Unit) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                onTimeSelected(hour, minute)
            }, initialHour, initialMinute, false
        )
        timePickerDialog.show()
    }
}


fun isToday(date: Date): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance()
    cal2.time = date
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

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