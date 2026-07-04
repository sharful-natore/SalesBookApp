package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.example.ui.components.GlassCard
import com.example.ui.components.loc
import com.example.data.model.Product
import com.example.viewmodel.SalesBookViewModel

@Composable
fun ProductsScreen(viewModel: SalesBookViewModel) {
    val productsList by viewModel.products.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
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
        containerColor = Color.Transparent,
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = loc(isEnglish, "পণ্য ও ইনভেন্টরি", "Products & Inventory"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(loc(isEnglish, "নাম দিয়ে পণ্য খুঁজুন...", "Search products by name..."), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("product_search_input"),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(8.dp)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                loc(isEnglish, "কোনো পণ্য পাওয়া যায়নি!", "No products found matching search!")
                            } else {
                                loc(
                                    isEnglish,
                                    "ইনভেন্টরি খালি! নিচে ডানদিকের বোতাম চেপে নতুন পণ্য যোগ করুন।",
                                    "Your inventory is empty! Use the + button at the bottom right to add items."
                                )
                            },
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            isEnglish = isEnglish,
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
            title = loc(isEnglish, "নতুন পণ্য যোগ করুন", "Add New Product"),
            isEnglish = isEnglish,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, buyPrice, sellPrice, stock, unit ->
                viewModel.addProduct(name, buyPrice, sellPrice, stock, unit) {
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Product Dialog
    productToEdit?.let { product ->
        ProductFormDialog(
            title = loc(isEnglish, "পণ্য সংশোধন করুন", "Edit Product Details"),
            product = product,
            isEnglish = isEnglish,
            onDismiss = { productToEdit = null },
            onConfirm = { name, buyPrice, sellPrice, stock, unit ->
                viewModel.updateProduct(product.copy(name = name, purchasePrice = buyPrice, sellingPrice = sellPrice, stock = stock, unit = unit)) {
                    productToEdit = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text(loc(isEnglish, "পণ্য মুছে ফেলার সতর্কতা", "Delete Product Alert"), fontWeight = FontWeight.Bold) },
            text = { Text(loc(isEnglish, "'${product.name}' পণ্যটি কি ইনভেন্টরি থেকে নিশ্চিতভাবে মুছে ফেলতে চান?", "Are you sure you want to permanently delete '${product.name}' from inventory?")) },
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
                    Text(loc(isEnglish, "হ্যাঁ, ডিলিট করুন", "Yes, Delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text(loc(isEnglish, "বাতিল", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    isEnglish: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
        padding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(loc(isEnglish, "ক্রয়মূল্য", "Purchase"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        Text("৳${product.purchasePrice}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
                    }
                    Column {
                        Text(loc(isEnglish, "বিক্রয়মূল্য", "Selling"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        Text("৳${product.sellingPrice}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                    }
                    Column {
                        Text(loc(isEnglish, "স্টক", "Stock"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                        Text("${product.stock} ${product.unit}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = if (product.stock <= 5) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp).testTag("edit_product_${product.id}")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Product",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp).testTag("delete_product_${product.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Product",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    title: String,
    product: Product? = null,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, purchasePrice: Double, sellingPrice: Double, stock: Int, unit: String) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var purchasePrice by remember { mutableStateOf(product?.purchasePrice?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(product?.sellingPrice?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var unit by remember { mutableStateOf(product?.unit ?: "Pcs") }

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
                    label = { Text(loc(isEnglish, "পণ্যের নাম", "Product Name")) },
                    modifier = Modifier.fillMaxWidth().testTag("product_dialog_name"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text(loc(isEnglish, "ক্রয়মূল্য", "Purchase Price (৳)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("product_dialog_buy_price"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text(loc(isEnglish, "বিক্রয়মূল্য", "Selling Price (৳)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("product_dialog_sell_price"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text(loc(isEnglish, "স্টক সংখ্যা", "Current Stock")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("product_dialog_stock"),
                    singleLine = true
                )

                // Unit Selection
                Text(
                    text = loc(isEnglish, "পরিমাপের একক", "Unit of Measure"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val unitOptions = listOf("Pcs", "Kg", "Ltr", "Bag", "Box", "Metre")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    unitOptions.forEach { option ->
                        val isSelected = unit == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { unit = option },
                            label = { Text(option) },
                            modifier = Modifier.testTag("unit_chip_$option")
                        )
                    }
                }

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
                        errorMsg = loc(isEnglish, "দয়া করে পণ্যের নাম লিখুন।", "Please enter product name.")
                        return@Button
                    }
                    val buy = purchasePrice.toDoubleOrNull()
                    if (buy == null || buy < 0) {
                        errorMsg = loc(isEnglish, "দয়া করে সঠিক ক্রয়মূল্য লিখুন।", "Please enter a valid purchase price.")
                        return@Button
                    }
                    val sell = sellingPrice.toDoubleOrNull()
                    if (sell == null || sell < 0) {
                        errorMsg = loc(isEnglish, "দয়া করে সঠিক বিক্রয়মূল্য লিখুন।", "Please enter a valid selling price.")
                        return@Button
                    }
                    val stk = stock.toIntOrNull()
                    if (stk == null || stk < 0) {
                        errorMsg = loc(isEnglish, "দয়া করে সঠিক স্টক সংখ্যা লিখুন।", "Please enter a valid stock level.")
                        return@Button
                    }

                    onConfirm(name.trim(), buy, sell, stk, unit)
                },
                modifier = Modifier.testTag("product_dialog_confirm")
            ) {
                Text(loc(isEnglish, "নিশ্চিত করুন", "Confirm"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(loc(isEnglish, "বাতিল", "Cancel"))
            }
        }
    )
}
