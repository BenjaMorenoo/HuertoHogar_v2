package com.mecagoentodo.huertohogar_v2.view

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mecagoentodo.huertohogar_v2.viewmodel.AccountViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.AccountViewModelFactory
import com.mecagoentodo.huertohogar_v2.viewmodel.CartViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.HomeViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModel setup
    val application = LocalContext.current.applicationContext as Application
    val userViewModel: UserViewModel = viewModel()
    val loggedInUser by userViewModel.loggedInUser.collectAsState()
    val accountViewModelFactory = AccountViewModelFactory(application, userViewModel)
    val accountViewModel: AccountViewModel = viewModel(factory = accountViewModelFactory)
    val homeViewModel: HomeViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()

    val screens = listOf(Screen.Home, Screen.Account, Screen.Profile, Screen.Cart)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // --- Cabecera del Menú Lateral ---
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Huerto Hogar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(loggedInUser?.name ?: "Invitado", style = MaterialTheme.typography.bodyMedium)
                }
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // --- Opciones de Navegación ---
                screens.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (screen == Screen.Profile) {
                                if (loggedInUser != null) {
                                    navController.navigate(Screen.Profile.route)
                                } else {
                                    navController.navigate(Screen.Account.route)
                                }
                            } else {
                                navController.navigate(screen.route)
                            }
                        },
                        icon = { 
                            val icon = when (screen) {
                                Screen.Home -> Icons.Default.Home
                                Screen.Account -> Icons.Default.Person
                                Screen.Profile -> Icons.Outlined.AccountCircle
                                Screen.Cart -> Icons.Default.ShoppingCart
                                Screen.Checkout -> Icons.Default.CreditCard // Añadido para que sea exhaustivo
                            }
                             Icon(icon, contentDescription = screen.title)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- Botón de Cerrar Sesión ---
                if (loggedInUser != null) {
                    NavigationDrawerItem(
                        label = { Text("Cerrar Sesión") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            userViewModel.logout()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(Icons.Outlined.ExitToApp, contentDescription = "Cerrar Sesión") },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(screens.find { it.route == currentRoute }?.title ?: "Inicio") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(paddingValues)) {
                composable(Screen.Home.route) {
                    HomeScreen(homeViewModel = homeViewModel, cartViewModel = cartViewModel)
                }
                composable(Screen.Account.route) {
                    AccountScreen(viewModel = accountViewModel, navController = navController)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(userViewModel = userViewModel, navController = navController)
                }
                composable(Screen.Cart.route) {
                    CartScreen(cartViewModel = cartViewModel, userViewModel = userViewModel, navController = navController)
                }
                composable(Screen.Checkout.route) {
                    CheckoutScreen(userViewModel = userViewModel, cartViewModel = cartViewModel, navController = navController)
                }
            }
        }
    }
}