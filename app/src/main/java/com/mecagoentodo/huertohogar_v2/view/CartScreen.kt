package com.mecagoentodo.huertohogar_v2.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mecagoentodo.huertohogar_v2.data.CartItem
import com.mecagoentodo.huertohogar_v2.viewmodel.CartViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.CheckoutState
import com.mecagoentodo.huertohogar_v2.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CartScreen(cartViewModel: CartViewModel, userViewModel: UserViewModel, navController: NavController) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val checkoutState by cartViewModel.checkoutState.collectAsState()
    val loggedInUser by userViewModel.loggedInUser.collectAsState()

    // --- Cálculos de Precios ---
    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val iva = subtotal * 0.19
    val total = subtotal + iva

    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Tu carrito está vacío.")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        CartItemCard(item, cartViewModel)
                    }
                }

                // --- Resumen del Total Desglosado ---
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SummaryRow(label = "Subtotal", amount = format.format(subtotal))
                        Spacer(modifier = Modifier.height(8.dp))
                        SummaryRow(label = "IVA (19%)", amount = format.format(iva))
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = format.format(total),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                if (loggedInUser != null) {
                                    navController.navigate(Screen.Checkout.route)
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Debes iniciar sesión para pagar.")
                                    }
                                    navController.navigate(Screen.Account.route)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Ir a Pagar")
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(checkoutState) {
        if (checkoutState is CheckoutState.Success) {
            scope.launch {
                snackbarHostState.showSnackbar("¡Compra realizada con éxito!")
            }
            cartViewModel.resetCheckoutState()
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(amount, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun CartItemCard(item: CartItem, cartViewModel: CartViewModel) {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.productName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = format.format(item.price * item.quantity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- Controles de Cantidad ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { cartViewModel.decreaseQuantity(item) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Disminuir cantidad")
                }
                Text("${item.quantity}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { cartViewModel.increaseQuantity(item) }) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad")
                }
            }
        }
    }
}