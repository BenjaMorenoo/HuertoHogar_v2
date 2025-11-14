package com.mecagoentodo.huertohogar_v2.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mecagoentodo.huertohogar_v2.viewmodel.UpdateState
import com.mecagoentodo.huertohogar_v2.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(userViewModel: UserViewModel, navController: NavController) {
    val loggedInUser by userViewModel.loggedInUser.collectAsState()
    val updateState by userViewModel.updateState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (loggedInUser != null) {
        val user = loggedInUser!!

        var address by remember { mutableStateOf(user.address) }
        var phone by remember { mutableStateOf(user.phone.removePrefix("+569")) }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar de Perfil",
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(user.name, style = MaterialTheme.typography.headlineSmall)
                Text(user.email, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(32.dp))

                // --- Campos Editables ---
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Home, null) })
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = phone, onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Phone, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), prefix = { Text("+569") })
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Nueva Contraseña (opcional)") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirmar Contraseña") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Lock, null) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
                
                Spacer(modifier = Modifier.weight(1f))

                Button(onClick = { userViewModel.updateUser(address, phone, password, confirmPassword) }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                    Text("Guardar Cambios")
                }
            }
        }

        LaunchedEffect(updateState) {
            if (updateState is UpdateState.Success) {
                scope.launch {
                    snackbarHostState.showSnackbar("¡Datos actualizados con éxito!")
                }
                userViewModel.resetUpdateState()
            } else if (updateState is UpdateState.Error) {
                scope.launch {
                    snackbarHostState.showSnackbar((updateState as UpdateState.Error).message)
                }
                userViewModel.resetUpdateState()
            }
        }

    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Por favor, inicia sesión para ver tu perfil.")
        }
    }
}