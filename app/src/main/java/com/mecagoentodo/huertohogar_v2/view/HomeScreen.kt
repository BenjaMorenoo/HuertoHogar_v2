package com.mecagoentodo.huertohogar_v2.view

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mecagoentodo.huertohogar_v2.model.Product
import com.mecagoentodo.huertohogar_v2.viewmodel.CartViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel = viewModel(), cartViewModel: CartViewModel) {
    val products by homeViewModel.products.collectAsState()
    val searchText by homeViewModel.searchText.collectAsState()
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var shakingProductId by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = homeViewModel::onSearchTextChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = CircleShape,
                singleLine = true
            )

            val groupedProducts = products.groupBy { it.category }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                groupedProducts.forEach { (category, productsInCategory) ->
                    item(key = category) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).animateItemPlacement()
                        )
                    }
                    items(productsInCategory, key = { it.id }) { product ->
                        Box(modifier = Modifier.animateItemPlacement()) {
                            ProductCard(
                                product = product,
                                isShaking = product.id == shakingProductId,
                                onAddToCart = {
                                    cartViewModel.addToCart(product)
                                    shakingProductId = product.id
                                    scope.launch {
                                        snackbarHostState.showSnackbar("¡${product.name} añadido al carrito!")
                                    }
                                },
                                onShakeComplete = { shakingProductId = null },
                                onClick = { selectedProduct = product }
                            )
                        }
                    }
                }
            }
        }
    }
    
    AnimatedVisibility(visible = selectedProduct != null, enter = fadeIn(), exit = fadeOut()) {
        if (selectedProduct != null) {
            ProductDetailsModal(product = selectedProduct!!, onDismiss = { selectedProduct = null })
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isShaking: Boolean,
    onAddToCart: () -> Unit,
    onShakeComplete: () -> Unit,
    onClick: () -> Unit
) {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0
    
    val shake = remember { Animatable(0f) }
    LaunchedEffect(isShaking) {
        if (isShaking) {
            shake.animateTo(
                targetValue = 0f, 
                animationSpec = keyframes {
                    durationMillis = 500
                    -10f at 50
                    10f at 100
                    -10f at 150
                    10f at 200
                    -5f at 250
                    5f at 300
                    0f at 350
                }
            )
            onShakeComplete()
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer { translationX = shake.value }
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "${format.format(product.price)} / ${product.unit}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(16.dp))

            FilledTonalIconButton(onClick = onAddToCart, shape = CircleShape) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = "Agregar al Carrito")
            }
        }
    }
}

@Composable
fun ProductDetailsModal(product: Product, onDismiss: () -> Unit) {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    format.maximumFractionDigits = 0

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                     Text("${product.code} - ${product.name}", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                     IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Precio: ${format.format(product.price)} por ${product.unit}", style = MaterialTheme.typography.titleMedium)
                Text("Stock: ${product.stock} ${product.unit}s", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(product.description, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}