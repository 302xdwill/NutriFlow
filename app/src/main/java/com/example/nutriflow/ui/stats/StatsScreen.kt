package com.example.nutriflow.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
// ✅ IMPORTACIONES FALTANTES AÑADIDAS AQUÍ
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Nota: En un proyecto real, necesitarías un StatsViewModel
// class StatsViewModel(...) : ViewModel() { ... }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    // statsViewModel: StatsViewModel = viewModel() // Descomentar cuando el ViewModel esté listo
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas y Análisis") },
                navigationIcon = {
                    Icon(Icons.Default.BarChart, contentDescription = null)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Placeholder para la Selección de Periodo (Semanal/Mensual)
            PeriodSelector()

            Spacer(modifier = Modifier.height(16.dp))

            // Resumen de Métricas Clave
            StatsSummaryCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de Gráficos de Tendencias
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Tendencias de Macro-nutrientes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item { ChartCard(title = "Promedio Semanal de Calorías") }
                item { ChartCard(title = "Distribución de Proteínas vs Metas") }
                item { ChartCard(title = "Consistencia de Carbohidratos") }
            }
        }
    }
}

@Composable
fun PeriodSelector() {
    // ✅ ESTO AHORA FUNCIONARÁ GRACIAS A LAS NUEVAS IMPORTACIONES
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Semana", "Mes", "Todo")

    TabRow(selectedTabIndex = selectedTab) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                text = { Text(title) }
            )
        }
    }
}

@Composable
fun StatsSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Resumen General (Últimos 7 días)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Prom. Cal", "2150 kcal")
                StatItem("Días en Meta", "5 / 7")
                StatItem("Prom. Prot", "110 g")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun ChartCard(title: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Este es el espacio donde se dibujaría un gráfico (ej: usando MPAndroidChart o Compose-Charts)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("Gráfico de Líneas/Barras (Integración futura)")
            }
        }
    }
}