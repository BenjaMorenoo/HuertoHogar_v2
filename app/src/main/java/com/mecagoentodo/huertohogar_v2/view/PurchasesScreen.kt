package com.mecagoentodo.huertohogar_v2.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mecagoentodo.huertohogar_v2.data.PurchaseWithItems
import com.mecagoentodo.huertohogar_v2.viewmodel.PurchasesViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.UserViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PurchasesScreen(purchasesViewModel: PurchasesViewModel, userViewModel: UserViewModel) {
    val loggedInUser by userViewModel.loggedInUser.collectAsState()
    
    if (loggedInUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Inicia sesión para ver tus compras.")
        }
        return
    }

    val purchases by purchasesViewModel.getPurchasesForUser(loggedInUser!!.id).collectAsState(initial = emptyList())
    var selectedPurchase by remember { mutableStateOf<PurchaseWithItems?>(null) }

    if (purchases.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aún no has realizado ninguna compra.")
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(purchases, key = { it.purchase.id }) {
                PurchaseCard(purchase = it, onClick = { selectedPurchase = it })
            }
        }
    }

    if (selectedPurchase != null) {
        PurchaseDetailsModal(purchase = selectedPurchase!!, onDismiss = { selectedPurchase = null })
    }
}

@Composable
fun PurchaseCard(purchase: PurchaseWithItems, onClick: () -> Unit) {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Compra del ${dateFormat.format(purchase.purchase.date)}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total: ${format.format(purchase.purchase.total)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Dirección: ${purchase.purchase.shippingAddress}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PurchaseDetailsModal(purchase: PurchaseWithItems, onDismiss: () -> Unit) {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0

    Dialog(onDismissRequest = onDismiss) {
        Card {
             Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Text("Boleta de Compra", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                     IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // --- Lista de Productos ---
                purchase.items.forEach {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${it.productName} x${it.quantity}")
                        Text(format.format(it.price * it.quantity))
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                
                // --- Total y Dirección ---
                SummaryRow(label = "Total Pagado", amount = format.format(purchase.purchase.total), isTotal = true)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enviado a: ${purchase.purchase.shippingAddress}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, amount: String, isTotal: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if(isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium)
        Text(amount, style = if(isTotal) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium, fontWeight = if(isTotal) FontWeight.Bold else FontWeight.Normal)
    }
}