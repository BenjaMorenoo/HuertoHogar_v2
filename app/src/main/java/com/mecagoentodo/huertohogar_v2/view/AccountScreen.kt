package com.mecagoentodo.huertohogar_v2.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mecagoentodo.huertohogar_v2.viewmodel.AccountViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.LoginState
import com.mecagoentodo.huertohogar_v2.viewmodel.RegistrationState
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(viewModel: AccountViewModel = viewModel(), navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) {
                    Text("Iniciar Sesión", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) {
                    Text("Registrarse", modifier = Modifier.padding(16.dp))
                }
            }

            when (selectedTabIndex) {
                0 -> LoginForm(viewModel, snackbarHostState, navController)
                1 -> RegisterForm(viewModel, snackbarHostState)
            }
        }
    }
}

@Composable
fun LoginForm(viewModel: AccountViewModel, snackbarHostState: SnackbarHostState, navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡Bienvenido de vuelta!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.loginUser(email, password) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Iniciar Sesión")
        }

        LaunchedEffect(loginState) {
            if (loginState is LoginState.Success) {
                scope.launch {
                    snackbarHostState.showSnackbar("¡Inicio de sesión exitoso!")
                }.invokeOnCompletion { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
                viewModel.resetLoginState()
            } else if (loginState is LoginState.Error) {
                scope.launch {
                    snackbarHostState.showSnackbar((loginState as LoginState.Error).message)
                }
                viewModel.resetLoginState()
            }
        }
    }
}

@Composable
fun RegisterForm(viewModel: AccountViewModel, snackbarHostState: SnackbarHostState) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val registrationState by viewModel.registrationState.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crea tu cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Dirección") },
            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { 
                if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                    phone = it
                }
            },
            label = { Text("Teléfono") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.registerUser(name, email, address, phone, password, confirmPassword) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Registrarse")
        }

        LaunchedEffect(registrationState) {
            if (registrationState is RegistrationState.Success) {
                scope.launch {
                    snackbarHostState.showSnackbar("¡Registro exitoso!")
                }
                viewModel.resetRegistrationState()
            } else if (registrationState is RegistrationState.Error) {
                scope.launch {
                    snackbarHostState.showSnackbar((registrationState as RegistrationState.Error).message)
                }
                viewModel.resetRegistrationState()
            }
        }
    }
}