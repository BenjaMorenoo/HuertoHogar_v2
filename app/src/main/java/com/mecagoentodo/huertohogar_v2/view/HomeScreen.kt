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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mecagoentodo.huertohogar_v2.R
import com.mecagoentodo.huertohogar_v2.model.Product
import com.mecagoentodo.huertohogar_v2.viewmodel.CartViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.HomeViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.ProductUiState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(homeViewModel: HomeViewModel, cartViewModel: CartViewModel) {
    val uiState by homeViewModel.uiState.collectAsState()
    
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var shakingProductId by remember { mutableStateOf<String?>(null) } 

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- Efecto para recargar los productos al volver a la pantalla ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                homeViewModel.loadProducts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is ProductUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProductUiState.Success -> {
                    CategoryFilter(
                        categories = state.categories,
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = { homeViewModel.selectCategory(it) }
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(state.products, key = { it.id }) { product ->
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
                is ProductUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message)
                    }
                }
            }
        }
    }
    
    AnimatedVisibility(visible = selectedProduct != null, enter = fadeIn(), exit = fadeOut()) {
        if (selectedProduct != null) {
            ProductDetailsModal(
                product = selectedProduct!!, 
                onDismiss = { selectedProduct = null },
                onAddToCart = {
                    cartViewModel.addToCart(selectedProduct!!)
                    scope.launch {
                        snackbarHostState.showSnackbar("¡${selectedProduct!!.name} añadido al carrito!")
                    }
                }
            )
        }
    }
}

@Composable
fun CategoryFilter(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                label = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    ElevatedFilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
        )
    )
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
                    -10f at 50; 10f at 100; -10f at 150; 10f at 200; -5f at 250; 5f at 300; 0f at 350
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
            coil.compose.AsyncImage(
                model = product.fullImageUrl,
                contentDescription = product.name,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_launcher_background), // Opcional
                error = painterResource(id = R.drawable.ic_launcher_background) // Opcional
            )

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
fun ProductDetailsModal(product: Product, onDismiss: () -> Unit, onAddToCart: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column {
                coil.compose.AsyncImage(
                    model = product.fullImageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_launcher_background), // Opcional
                    error = painterResource(id = R.drawable.ic_launcher_background) // Opcional
                )

                Column(modifier = Modifier.padding(24.dp)) {
                    Text(product.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(product.code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoChip(icon = Icons.Default.AttachMoney, text = "${NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply { maximumFractionDigits = 0 }.format(product.price)} / ${product.unit}")
                        InfoChip(icon = Icons.Outlined.Inventory2, text = "${product.stock} disponibles")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(product.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = onAddToCart, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Agregar al Carrito")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}