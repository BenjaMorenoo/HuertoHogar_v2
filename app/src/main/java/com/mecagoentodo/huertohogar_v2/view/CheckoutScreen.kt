package com.mecagoentodo.huertohogar_v2.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mecagoentodo.huertohogar_v2.viewmodel.CartViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.UserViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CheckoutScreen(userViewModel: UserViewModel, cartViewModel: CartViewModel, navController: NavController) {
    val loggedInUser by userViewModel.loggedInUser.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    var showConfirmationDialog by remember { mutableStateOf(false) }

    if (loggedInUser == null) {
        navController.navigate(Screen.Account.route)
        return
    }

    var usePrimaryAddress by remember { mutableStateOf(true) }
    var alternativeAddress by remember { mutableStateOf("") }

    Scaffold {
        Column(modifier = Modifier.fillMaxSize().padding(it).padding(16.dp)) {
            // --- Resumen de la Boleta ---
            Text("Resumen de la Compra", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            val subtotal = cartItems.sumOf { item -> item.price * item.quantity }
            val iva = subtotal * 0.19
            val total = subtotal + iva
            val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
            format.maximumFractionDigits = 0
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SummaryRow(label = "Subtotal", amount = format.format(subtotal))
                    SummaryRow(label = "IVA (19%)", amount = format.format(iva))
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    SummaryRow(label = "Total a Pagar", amount = format.format(total), isTotal = true)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Selección de Dirección ---
            Text("Dirección de Envío", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = usePrimaryAddress, onClick = { usePrimaryAddress = true })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Usar mi dirección guardada", style = MaterialTheme.typography.bodyLarge)
                        Text(loggedInUser!!.address, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !usePrimaryAddress, onClick = { usePrimaryAddress = false })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Usar otra dirección", style = MaterialTheme.typography.bodyLarge)
                    }
                    if (!usePrimaryAddress) {
                        OutlinedTextField(value = alternativeAddress, onValueChange = { alternativeAddress = it }, label = { Text("Nueva dirección de envío") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Botón de Pagar Ahora ---
            Button(
                onClick = { showConfirmationDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = usePrimaryAddress || alternativeAddress.isNotBlank()
            ) {
                Text("Pagar Ahora")
            }
        }
    }

    // --- Diálogo de Confirmación de Pago ---
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Pago Exitoso!") },
            text = { Text("Tu compra ha sido realizada con éxito. Gracias por preferir Huerto Hogar.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        cartViewModel.checkout()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
private fun SummaryRow(label: String, amount: String, isTotal: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if(isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium)
        Text(amount, style = if(isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium, fontWeight = if(isTotal) FontWeight.Bold else FontWeight.Normal)
    }
}