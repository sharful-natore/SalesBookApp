package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.viewmodel.SalesBookViewModel

@Composable
fun ProductsScreen(viewModel: SalesBookViewModel) {
    val productsList by viewModel.products.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = remember(productsList, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            productsList
        } else {
            productsList.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "পণ্য ও ইনভেন্টরি (Products & Stock)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("নাম দিয়ে পণ্য খুজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Products Listing
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "কোনো পণ্য পাওয়া যায়নি!" else "ইনভেন্টরি খালি! নিচে ডানদিকের বোতাম চেপে পণ্য যোগ করুন।",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onEdit = { productToEdit = product },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddDialog) {
        ProductFormDialog(
            title = "নতুন পণ্য যোগ করুন",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, buyPrice, sellPrice, stock ->
                viewModel.addProduct(name, buyPrice, sellPrice, stock) {
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Product Dialog
    productToEdit?.let { product ->
        ProductFormDialog(
            title = "পণ্য সংশোধন করুন",
            product = product,
            onDismiss = { productToEdit = null },
            onConfirm = { name, buyPrice, sellPrice, stock ->
                viewModel.updateProduct(product.copy(name = name, purchasePrice = buyPrice, sellingPrice = sellPrice, stock = stock)) {
                    productToEdit = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("পণ্য মুছে ফেলার সতর্কতা", fontWeight = FontWeight.Bold) },
            text = { Text("'${product.name}' পণ্যটি কি ইনভেন্টরি থেকে নিশ্চিতভাবে মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProduct(product) {
                            productToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("delete_confirm_btn")
                ) {
                    Text("হ্যাঁ, ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ক্রয়মূল্য", fontSize = 11.sp, color = Color.Gray)
                        Text("৳${product.purchasePrice}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFC62828))
                    }
                    Column {
                        Text("বিক্রয়মূল্য", fontSize = 11.sp, color = Color.Gray)
                        Text("৳${product.sellingPrice}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                    }
                    Column {
                        Text("স্টক", fontSize = 11.sp, color = Color.Gray)
                        Text("${product.stock} টি", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (product.stock <= 5) Color.Red else Color.Black)
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_product_${product.id}")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Product",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_product_${product.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Product",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ProductFormDialog(
    title: String,
    product: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, purchasePrice: Double, sellingPrice: Double, stock: Int) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var purchasePrice by remember { mutableStateOf(product?.purchasePrice?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পণ্যের নাম (Product Name)") },
                    modifier = Modifier.fillMaxWidth().testTag("product_dialog_name"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("ক্রয়মূল্য (Purchase Price)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("product_dialog_buy_price"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("বিক্রয়মূল্য (Selling Price)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("product_dialog_sell_price"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("স্টক সংখ্যা (Current Stock)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("product_dialog_stock"),
                    singleLine = true
                )

                AnimatedVisibility(visible = errorMsg != null) {
                    Text(
                        text = errorMsg ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        errorMsg = "দয়া করে পণ্যের নাম লিখুন।"
                        return@Button
                    }
                    val buy = purchasePrice.toDoubleOrNull()
                    if (buy == null || buy < 0) {
                        errorMsg = "দয়া করে সঠিক ক্রয়মূল্য লিখুন।"
                        return@Button
                    }
                    val sell = sellingPrice.toDoubleOrNull()
                    if (sell == null || sell < 0) {
                        errorMsg = "দয়া করে সঠিক বিক্রয়মূল্য লিখুন।"
                        return@Button
                    }
                    val stk = stock.toIntOrNull()
                    if (stk == null || stk < 0) {
                        errorMsg = "দয়া করে সঠিক স্টক সংখ্যা লিখুন।"
                        return@Button
                    }

                    onConfirm(name.trim(), buy, sell, stk)
                },
                modifier = Modifier.testTag("product_dialog_confirm")
            ) {
                Text("নিশ্চিত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
