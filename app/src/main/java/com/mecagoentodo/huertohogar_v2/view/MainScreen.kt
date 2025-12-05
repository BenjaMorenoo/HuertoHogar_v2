package com.mecagoentodo.huertohogar_v2.view

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.mecagoentodo.huertohogar_v2.data.network.RetrofitClient
import com.mecagoentodo.huertohogar_v2.data.repository.ProductRepository
import com.mecagoentodo.huertohogar_v2.data.repository.UserRepository
import com.mecagoentodo.huertohogar_v2.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- Infraestructura de Datos y ViewModels ---
    val application = LocalContext.current.applicationContext as Application
    
    val apiService = remember { RetrofitClient.instance }
    val productRepository = remember { ProductRepository(apiService) }
    val userRepository = remember { UserRepository(apiService) }

    val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory(userRepository))
    val accountViewModel: AccountViewModel = viewModel(factory = AccountViewModelFactory(userRepository, userViewModel))
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(productRepository))
    val cartViewModel: CartViewModel = viewModel(factory = CartViewModelFactory(application, productRepository))
    val purchasesViewModel: PurchasesViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(application, productRepository))
    
    val loggedInUser by userViewModel.loggedInUser.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()

    val userScreens = listOf(Screen.Home, Screen.Purchases, Screen.Profile, Screen.Cart)
    val adminScreens = listOf(Screen.Admin)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
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

                val screensToShow = if (loggedInUser?.isAdmin == true) adminScreens else userScreens

                screensToShow.forEach { screen ->
                    NavigationDrawerItem(
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (screen == Screen.Profile || screen == Screen.Purchases || screen == Screen.Admin) {
                                if (loggedInUser != null) {
                                    navController.navigate(screen.route)
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
                                Screen.Purchases -> Icons.Outlined.ReceiptLong
                                Screen.Profile -> Icons.Outlined.AccountCircle
                                Screen.Cart -> Icons.Default.ShoppingCart
                                Screen.Admin -> Icons.Outlined.AdminPanelSettings
                                else -> Icons.Default.Help // Para casos inesperados
                            }
                             Icon(icon, contentDescription = screen.title)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

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
                    title = { 
                        val title = when(currentRoute) {
                            Screen.Admin.route -> Screen.Admin.title
                            else -> (userScreens.find { it.route == currentRoute } ?: Screen.Home).title
                        }
                        Text(title) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        if(loggedInUser?.isAdmin != true) { // Ocultar si es admin
                            BadgedBox(badge = {
                                if (cartItems.isNotEmpty()) {
                                    val quantity = cartItems.sumOf { it.quantity }
                                    Badge { Text(if (quantity > 9) "9+" else "$quantity") }
                                }
                            }) {
                                IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                )
            },
        ) { paddingValues ->
            NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(paddingValues)) {
                composable(Screen.Home.route) { HomeScreen(homeViewModel = homeViewModel, cartViewModel = cartViewModel) }
                composable(Screen.Account.route) { AccountScreen(viewModel = accountViewModel, navController = navController) }
                composable(Screen.Profile.route) { ProfileScreen(userViewModel = userViewModel, navController = navController) }
                composable(Screen.Cart.route) { CartScreen(cartViewModel = cartViewModel, userViewModel = userViewModel, navController = navController) }
                composable(Screen.Checkout.route) { CheckoutScreen(userViewModel = userViewModel, cartViewModel = cartViewModel, navController = navController) }
                composable(Screen.Purchases.route) { PurchasesScreen(purchasesViewModel = purchasesViewModel, userViewModel = userViewModel) }
                composable(Screen.Admin.route) { AdminScreen(adminViewModel = adminViewModel, homeViewModel = homeViewModel) }
            }
        }
    }
}
