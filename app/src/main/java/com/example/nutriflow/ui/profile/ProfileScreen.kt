package com.example.nutriflow.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutriflow.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import com.example.nutriflow.domain.model.User
import com.example.nutriflow.ui.profile.ProfileUiState // Importación del estado (debe estar en ProfileViewModel.kt)


// NOTA: La data class ProfileUiState debe estar definida SÓLO en ProfileViewModel.kt
// La importación de arriba resuelve el error de redeclaración.


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel,
    onNavigateToGoals: () -> Unit,
    onNavigateToStats: () -> Unit,
    onLogoutSuccessNavigation: () -> Unit
) {
    val user by authViewModel.currentUser.collectAsState()
    val state by profileViewModel.uiState.collectAsState()
    val logoutEvent by profileViewModel.logoutEvent.collectAsState()

    LaunchedEffect(logoutEvent) {
        if (logoutEvent) {
            authViewModel.onLogoutSuccess()
            onLogoutSuccessNavigation()
        }
    }

    var isEditing by remember { mutableStateOf(false) }

    // ✅ CORRECCIÓN 1: Simplificar la inicialización a valores por defecto válidos.
    // Asume que User.kt tiene valores por defecto para los demás campos.
    var editableUser by remember(user) {
        mutableStateOf(user ?: User(
            email = "cargando...",
            id = TODO(),
            name = TODO(),
            lastName = TODO(),
            age = TODO(),
            weight = TODO(),
            height = TODO(),
            photoUrl = TODO(),
            calorieGoal = TODO(),
            proteinGoal = TODO(),
            carbsGoal = TODO(),
            fatGoal = TODO()
        ))
    }

    LaunchedEffect(user) {
        // ✅ CORRECCIÓN 2: Simplificar la inicialización a valores por defecto válidos.
        editableUser = user ?: User(
            email = "cargando...",
            id = TODO(),
            name = TODO(),
            lastName = TODO(),
            age = TODO(),
            weight = TODO(),
            height = TODO(),
            photoUrl = TODO(),
            calorieGoal = TODO(),
            proteinGoal = TODO(),
            carbsGoal = TODO(),
            fatGoal = TODO()
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Perfil") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isLoading || user == null) {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
            } else {
                // Tarjeta de Información del Usuario
                UserProfileHeader(
                    user = user!!,
                    onEditPhoto = {
                        scope.launch { snackbarHostState.showSnackbar("Función de subir foto aún no implementada.") }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sección de Edición de Datos Personales
                DatosPersonalesSection(
                    user = editableUser,
                    onUserChange = { editableUser = it },
                    isEditing = isEditing,
                    onToggleEdit = { isEditing = !isEditing },
                    onSave = {
                        profileViewModel.updateUserProfile(editableUser)
                        isEditing = false
                        scope.launch { snackbarHostState.showSnackbar("Perfil actualizado con éxito.") }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Opciones de Navegación
                SettingsSectionTitle("Metas y Progreso")
                ProfileMenuItem(
                    icon = Icons.Default.TrackChanges,
                    title = "Metas Nutricionales",
                    subtitle = "Actualiza tus objetivos de calorías y macros.",
                    onClick = onNavigateToGoals
                )
                ProfileMenuItem(
                    icon = Icons.Default.Assessment,
                    title = "Estadísticas y Progreso",
                    subtitle = "Historial de consumo y gráficos.",
                    onClick = onNavigateToStats
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de Cerrar Sesión
                OutlinedButton(
                    onClick = profileViewModel::logout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión")
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Definición de la función renombrada: solo acepta 'user' y 'onEditPhoto'
@Composable
fun UserProfileHeader(user: User, onEditPhoto: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sección de Foto de Perfil
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(bottom = 8.dp)) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                FloatingActionButton(
                    onClick = onEditPhoto,
                    modifier = Modifier.size(32.dp),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Editar Foto", modifier = Modifier.size(20.dp))
                }
            }
            Text("${user.name} ${user.lastName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(user.email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// El resto de las funciones auxiliares se mantienen sin cambios

@Composable
fun DatosPersonalesSection(
    user: User,
    onUserChange: (User) -> Unit,
    isEditing: Boolean,
    onToggleEdit: () -> Unit,
    onSave: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsSectionTitle("Datos Personales", isNested = true)
                IconButton(onClick = onToggleEdit) {
                    Icon(if (isEditing) Icons.Default.Close else Icons.Default.Edit, contentDescription = if (isEditing) "Cancelar Edición" else "Editar")
                }
            }

            // Campos Editables
            UserTextField(
                label = "Nombre",
                value = user.name,
                enabled = isEditing,
                onValueChange = { onUserChange(user.copy(name = it)) }
            )
            UserTextField(
                label = "Apellido",
                value = user.lastName,
                enabled = isEditing,
                onValueChange = { onUserChange(user.copy(lastName = it)) }
            )
            UserTextField(
                label = "Edad (años)",
                value = if (user.age == 0 && !isEditing) "No especificado" else user.age.toString(),
                keyboardType = KeyboardType.Number,
                enabled = isEditing,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) onUserChange(user.copy(age = it.toIntOrNull() ?: 0))
                }
            )
            UserTextField(
                label = "Peso (kg)",
                value = if (user.weight == 0.0 && !isEditing) "No especificado" else user.weight.toString(),
                keyboardType = KeyboardType.Number,
                enabled = isEditing,
                onValueChange = {
                    if (it.matches(Regex("^\\d*(\\.\\d*)?$")) || it.isEmpty()) onUserChange(user.copy(weight = it.toDoubleOrNull() ?: 0.0))
                }
            )
            UserTextField(
                label = "Altura (cm)",
                value = if (user.height == 0.0 && !isEditing) "No especificado" else user.height.toString(),
                keyboardType = KeyboardType.Number,
                enabled = isEditing,
                onValueChange = {
                    if (it.matches(Regex("^\\d*(\\.\\d*)?$")) || it.isEmpty()) onUserChange(user.copy(height = it.toDoubleOrNull() ?: 0.0))
                }
            )

            if (isEditing) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text("Guardar Datos")
                }
            }
        }
    }
}

@Composable
fun UserTextField(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp).padding(end = 8.dp), tint = MaterialTheme.colorScheme.secondary)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)

    }
    Divider()
}

@Composable
fun SettingsSectionTitle(title: String, isNested: Boolean = false) {
    Text(
        text = title,
        style = if (isNested) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    )
    if (!isNested) Divider()
}