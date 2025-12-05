package com.mecagoentodo.huertohogar_v2.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.mecagoentodo.huertohogar_v2.model.Product
import com.mecagoentodo.huertohogar_v2.viewmodel.AdminUiState
import com.mecagoentodo.huertohogar_v2.viewmodel.AdminViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.HomeViewModel
import com.mecagoentodo.huertohogar_v2.viewmodel.ProductUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(adminViewModel: AdminViewModel, homeViewModel: HomeViewModel) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val adminUiState by adminViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(adminUiState) {
        when (val state = adminUiState) {
            is AdminUiState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                homeViewModel.refreshProducts()
                adminViewModel.resetState()
                showCreateDialog = false
            }
            is AdminUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message, withDismissAction = true)
                }
                adminViewModel.resetState()
            }
            else -> { /* Idle or Loading */ }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Producto")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = homeUiState) {
                is ProductUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is ProductUiState.Success -> {
                    val products = state.products
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(products, key = { it.id }) { product ->
                            AdminProductCard(
                                product = product,
                                onSave = { updatedProductData, imageUri ->
                                    adminViewModel.updateProduct(product.id, updatedProductData, imageUri)
                                },
                                onDelete = { productId ->
                                    adminViewModel.deleteProduct(productId)
                                }
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

        if (showCreateDialog) {
            CreateProductDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { productData, imageUri ->
                    adminViewModel.createProduct(productData, imageUri)
                },
                adminUiState = adminUiState
            )
        }
    }
}

@Composable
fun AdminProductCard(
    product: Product,
    onSave: (Map<String, Any>, Uri?) -> Unit,
    onDelete: (String) -> Unit
) {
    var price by remember { mutableStateOf(product.price.toString()) }
    var stock by remember { mutableStateOf(product.stock.toString()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        isEditing = true
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it; isEditing = true },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it; isEditing = true },
                label = { Text("Stock") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Image Selector
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { imageLauncher.launch("image/*") }) {
                    Text("Cambiar Imagen")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Image(
                    painter = rememberAsyncImagePainter(
                        model = selectedImageUri ?: product.imageUrl
                    ),
                    contentDescription = "Previsualización",
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onDelete(product.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Eliminar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val dataToUpdate = mutableMapOf<String, Any>()
                        price.toDoubleOrNull()?.let { if(it != product.price) dataToUpdate["price"] = it }
                        stock.toDoubleOrNull()?.let { if(it != product.stock) dataToUpdate["stock"] = it }
                        
                        onSave(dataToUpdate, selectedImageUri)
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isEditing
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
fun CreateProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Any>, Uri?) -> Unit,
    adminUiState: AdminUiState
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
    }
    val isLoading = adminUiState is AdminUiState.Loading

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Crear Nuevo Producto", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoría") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unidad (ej: kg, ud)") })
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock inicial") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
                
                Spacer(modifier = Modifier.height(16.dp))

                // Image Selector
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Imagen del Producto")
                    Button(onClick = { imageLauncher.launch("image/*") }) {
                        Text("Seleccionar")
                    }
                }
                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(model = selectedImageUri),
                        contentDescription = "Previsualización de la imagen",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                     if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val productData = mapOf(
                                "name" to name,
                                "code" to code,
                                "category" to category,
                                "price" to (price.toDoubleOrNull() ?: 0.0),
                                "unit" to unit,
                                "stock" to (stock.toDoubleOrNull() ?: 0.0),
                                "description" to description
                            )
                            onConfirm(productData, selectedImageUri)
                        }) {
                            Text("Crear")
                        }
                    }
                }
            }
        }
    }
}