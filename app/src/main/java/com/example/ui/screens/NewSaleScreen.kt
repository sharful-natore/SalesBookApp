package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.Product
import com.example.viewmodel.SalesBookViewModel

@Composable
fun NewSaleScreen(viewModel: SalesBookViewModel) {
    val context = LocalContext.current
    
    val productsList by viewModel.products.collectAsStateWithLifecycle()
    val customersList by viewModel.customers.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val paidAmountInput by viewModel.paidAmountInput.collectAsStateWithLifecycle()
    val activePaymentType by viewModel.paymentType.collectAsStateWithLifecycle()

    var productSearchQuery by remember { mutableStateOf("") }
    var showCustomerSelector by remember { mutableStateOf(false) }

    val filteredProducts = remember(productsList, productSearchQuery) {
        if (productSearchQuery.trim().isEmpty()) {
            productsList.filter { it.stock > 0 }
        } else {
            productsList.filter {
                it.stock > 0 && it.name.contains(productSearchQuery, ignoreCase = true)
            }
        }
    }

    val totalAmount = viewModel.getTotalCartAmount()
    val paidAmount = paidAmountInput.toDoubleOrNull() ?: 0.0
    val dueAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1: Heading
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "নতুন বেচাকেনা হিসাব (New Sale)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Step 2: Product Search & Quick Pick
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "১. পণ্য যোগ করুন (Select Product)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        placeholder = { Text("পণ্যের নাম লিখে সার্চ করুন...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sale_product_search"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    if (filteredProducts.isEmpty()) {
                        Text(
                            text = "কোনো পণ্য পাওয়া যায়নি (অথবা স্টক খালি)।",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        // Horizontal quick pick list
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            items(filteredProducts, key = { it.id }) { product ->
                                Card(
                                    modifier = Modifier
                                        .widthIn(min = 100.dp, max = 150.dp)
                                        .clickable { viewModel.addToCart(product) }
                                        .testTag("add_to_cart_${product.id}"),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = product.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "৳${product.sellingPrice}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "স্টক: ${product.stock} টি",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Current Shopping Cart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "২. শপিং কার্ট (Cart Items)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (cartItems.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearCart() }) {
                                Text("সব মুছুন (Clear)")
                            }
                        }
                    }

                    if (cartItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কার্ট খালি! ওপর থেকে পণ্য যোগ করুন।",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        cartItems.forEach { cartItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cartItem.product.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "একক মূল্য: ৳${cartItem.product.sellingPrice} | মোট: ৳${cartItem.product.sellingPrice * cartItem.quantity}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(cartItem.product, cartItem.quantity - 1) },
                                        modifier = Modifier.size(32.dp).testTag("decrease_${cartItem.product.id}")
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = cartItem.quantity.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.testTag("qty_${cartItem.product.id}")
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(cartItem.product, cartItem.quantity + 1) },
                                        enabled = cartItem.quantity < cartItem.product.stock,
                                        modifier = Modifier.size(32.dp).testTag("increase_${cartItem.product.id}")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeFromCart(cartItem.product) },
                                        modifier = Modifier.size(32.dp).testTag("remove_${cartItem.product.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Step 4: Customer Selector Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomerSelector = true }
                        .padding(16.dp)
                        .testTag("select_customer_trigger"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "৩. ক্রেতা নির্বাচন (Customer)",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = selectedCustomer?.name ?: "খুচরা ক্রেতা (Walk-in Customer)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select"
                    )
                }
            }
        }

        // Step 5: Billing, Calculations & Payment Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "৪. পেমেন্ট ও ভাউচার হিসাব (Billing)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Math breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("মোট বিল (Total Bill)", color = Color.Gray, fontSize = 14.sp)
                            Text("৳${totalAmount}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("বাকি খাতা (Due Amount)", color = Color.Gray, fontSize = 14.sp)
                            Text(
                                "৳${dueAmount}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (dueAmount > 0) Color(0xFFC62828) else Color.Black
                            )
                        }
                    }

                    // Payment Type selection chip row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentStatusChip(
                            label = "পুরো নগদ (Cash)",
                            isSelected = activePaymentType == "Full Cash",
                            onClick = { viewModel.setPaymentStatus("Full Cash") },
                            modifier = Modifier.weight(1f).testTag("chip_cash")
                        )
                        PaymentStatusChip(
                            label = "পুরো বাকি (Due)",
                            isSelected = activePaymentType == "Full Due",
                            onClick = { viewModel.setPaymentStatus("Full Due") },
                            modifier = Modifier.weight(1f).testTag("chip_due")
                        )
                        PaymentStatusChip(
                            label = "আংশিক (Partial)",
                            isSelected = activePaymentType == "Partial Paid",
                            onClick = { viewModel.setPaymentStatus("Partial Paid") },
                            modifier = Modifier.weight(1f).testTag("chip_partial")
                        )
                    }

                    // Numeric Paid Amount input
                    OutlinedTextField(
                        value = paidAmountInput,
                        onValueChange = { viewModel.setPaidAmount(it) },
                        label = { Text("জমাকৃত টাকা (Paid Amount)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Text("৳ ", fontWeight = FontWeight.Bold, color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sale_paid_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        enabled = activePaymentType == "Partial Paid" || totalAmount == 0.0
                    )

                    // Submit button
                    Button(
                        onClick = {
                            viewModel.checkout(
                                onSuccess = {
                                    Toast.makeText(context, "লেনদেন সফলভাবে সম্পন্ন হয়েছে!", Toast.LENGTH_LONG).show()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("complete_sale_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("বেচাকেনা সম্পন্ন করুন (Complete Sale)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Customer Selector Dialog
    if (showCustomerSelector) {
        CustomerSelectorDialog(
            customers = customersList,
            onDismiss = { showCustomerSelector = false },
            onSelect = { customer ->
                viewModel.selectCustomer(customer)
                showCustomerSelector = false
            }
        )
    }
}

@Composable
fun PaymentStatusChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .height(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomerSelectorDialog(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onSelect: (Customer?) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(customers, query) {
        if (query.trim().isEmpty()) {
            customers
        } else {
            customers.filter {
                it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ক্রেতা নির্বাচন করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("নাম বা ফোন নাম্বার খুঁজুন...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_customer_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Walk-in Option
                    item {
                        Card(
                            onClick = { onSelect(null) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().testTag("customer_walk_in")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("খুচরা ক্রেতা (Walk-in Customer)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("কোনো নির্দিষ্ট আইডি বা খাতা নেই", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    items(filtered) { customer ->
                        Card(
                            onClick = { onSelect(customer) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().testTag("customer_option_${customer.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("📞 ${customer.phone}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
