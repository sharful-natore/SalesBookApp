package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.components.loc
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
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    var productSearchQuery by remember { mutableStateOf("") }
    var showCustomerSelector by remember { mutableStateOf(false) }
    var showReviewDialog by remember { mutableStateOf(false) }

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = loc(isEnglish, "নতুন বেচাকেনা হিসাব", "New Sale Booking"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Step 2: Product Search & Quick Pick
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 14.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = loc(isEnglish, "১. পণ্য যোগ করুন", "1. Add Products to Cart"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = productSearchQuery,
                        onValueChange = { productSearchQuery = it },
                        placeholder = { Text(loc(isEnglish, "পণ্যের নাম লিখে সার্চ করুন...", "Search product by name..."), fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("sale_product_search"),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (filteredProducts.isEmpty()) {
                        Text(
                            text = loc(isEnglish, "কোনো পণ্য পাওয়া যায়নি (অথবা স্টক খালি)।", "No active products available (or out of stock)."),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        // Horizontal quick pick list
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            items(filteredProducts, key = { it.id }) { product ->
                                GlassCard(
                                    modifier = Modifier
                                        .widthIn(min = 120.dp, max = 160.dp)
                                        .clickable { viewModel.addToCart(product) }
                                        .testTag("add_to_cart_${product.id}"),
                                    padding = 10.dp
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = product.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "৳${product.sellingPrice}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "${loc(isEnglish, "স্টক: ", "Stock: ")}${product.stock} ${product.unit}",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
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
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 14.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = loc(isEnglish, "২. শপিং কার্ট", "2. Shopping Cart"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (cartItems.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearCart() }) {
                                Text(loc(isEnglish, "সব মুছুন", "Clear All"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    if (cartItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = loc(isEnglish, "কার্ট খালি! ওপর থেকে পণ্য যোগ করুন।", "Your cart is empty! Select products above."),
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        cartItems.forEach { cartItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cartItem.product.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = loc(
                                            isEnglish,
                                            "দর: ৳${cartItem.product.sellingPrice} | মোট: ৳${cartItem.product.sellingPrice * cartItem.quantity}",
                                            "Rate: ৳${cartItem.product.sellingPrice} | Total: ৳${cartItem.product.sellingPrice * cartItem.quantity}"
                                        ),
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(cartItem.product, cartItem.quantity - 1) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .testTag("decrease_${cartItem.product.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = cartItem.quantity.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 4.dp).testTag("qty_${cartItem.product.id}")
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateCartQuantity(cartItem.product, cartItem.quantity + 1) },
                                        enabled = cartItem.quantity < cartItem.product.stock,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (cartItem.quantity < cartItem.product.stock)
                                                    MaterialTheme.colorScheme.primaryContainer
                                                else
                                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            )
                                            .testTag("increase_${cartItem.product.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = if (cartItem.quantity < cartItem.product.stock)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeFromCart(cartItem.product) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .testTag("remove_${cartItem.product.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }

        // Step 4: Customer Selector Panel
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 14.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomerSelector = true }
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = loc(isEnglish, "৩. ক্রেতা নির্বাচন", "3. Selected Customer"),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedCustomer?.name ?: loc(isEnglish, "খুচরা ক্রেতা (Walk-in Customer)", "Walk-in Customer"),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Step 5: Billing, Calculations & Payment Details
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 14.dp
            ) {
                Column(modifier = Modifier.padding(2.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = loc(isEnglish, "৪. পেমেন্ট ও ভাউচার হিসাব", "4. Checkout & Payment Settlement"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Math breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(loc(isEnglish, "মোট বিল", "Total Invoice Amount"), color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("৳${totalAmount}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(loc(isEnglish, "বাকি পরিমাণ", "Outstanding Due"), color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "৳${dueAmount}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (dueAmount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Payment Type selection chip row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentStatusChip(
                            label = loc(isEnglish, "পুরো নগদ", "Full Cash"),
                            isSelected = activePaymentType == "Full Cash",
                            onClick = { viewModel.setPaymentStatus("Full Cash") },
                            modifier = Modifier.weight(1f).testTag("chip_cash")
                        )
                        PaymentStatusChip(
                            label = loc(isEnglish, "পুরো বাকি", "Full Due"),
                            isSelected = activePaymentType == "Full Due",
                            onClick = { viewModel.setPaymentStatus("Full Due") },
                            modifier = Modifier.weight(1f).testTag("chip_due")
                        )
                        PaymentStatusChip(
                            label = loc(isEnglish, "আংশিক নগদ", "Partial Paid"),
                            isSelected = activePaymentType == "Partial Paid",
                            onClick = { viewModel.setPaymentStatus("Partial Paid") },
                            modifier = Modifier.weight(1f).testTag("chip_partial")
                        )
                    }

                    // Numeric Paid Amount input
                    OutlinedTextField(
                        value = paidAmountInput,
                        onValueChange = { viewModel.setPaidAmount(it) },
                        label = { Text(loc(isEnglish, "জমাকৃত টাকা (পরিশোধ)", "Collected Cash (৳)"), fontSize = 13.sp) },
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
                            if (cartItems.isEmpty()) {
                                val emptyMsg = loc(isEnglish, "শপিং কার্ট খালি!", "Shopping cart is empty!")
                                Toast.makeText(context, emptyMsg, Toast.LENGTH_SHORT).show()
                            } else {
                                showReviewDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("complete_sale_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(loc(isEnglish, "বেচাকেনা সম্পন্ন করুন", "Complete Sale Booking"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
            isEnglish = isEnglish,
            onDismiss = { showCustomerSelector = false },
            onSelect = { customer ->
                viewModel.selectCustomer(customer)
                showCustomerSelector = false
            }
        )
    }

    // Order Review & Edit Dialog
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = {
                Text(
                    text = loc(isEnglish, "অর্ডার রিভিউ করুন", "Review Order"),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    // Customer info
                    Text(
                        text = loc(
                            isEnglish,
                            "ক্রেতা: ${selectedCustomer?.name ?: "সাধারণ ক্রেতা"}",
                            "Customer: ${selectedCustomer?.name ?: "Walk-in Customer"}"
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // List of items
                    Text(
                        text = loc(isEnglish, "নির্বাচিত পণ্যসমূহ:", "Selected Products:"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        cartItems.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.product.name} x ${item.quantity}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "৳${item.product.sellingPrice * item.quantity}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Summary metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(loc(isEnglish, "মোট বিল", "Total Bill"), fontSize = 13.sp, color = Color.Gray)
                        Text("৳${totalAmount}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(loc(isEnglish, "জমাকৃত টাকা", "Paid Amount"), fontSize = 13.sp, color = Color.Gray)
                        Text("৳${paidAmount}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(loc(isEnglish, "বাকি পরিমাণ", "Outstanding Due"), fontSize = 13.sp, color = Color.Gray)
                        Text(
                            "৳${dueAmount}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (dueAmount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReviewDialog = false
                        viewModel.checkout(
                            onSuccess = {
                                val msg = loc(isEnglish, "লেনদেন সফলভাবে সম্পন্ন হয়েছে!", "Sale booking completed successfully!")
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text(loc(isEnglish, "কনফার্ম করুন", "Confirm Order"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text(loc(isEnglish, "এডিট করুন", "Edit Details"), color = MaterialTheme.colorScheme.error)
                }
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
            .height(42.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CustomerSelectorDialog(
    customers: List<Customer>,
    isEnglish: Boolean,
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
        title = { Text(loc(isEnglish, "ক্রেতা নির্বাচন করুন", "Select Customer Account"), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(loc(isEnglish, "নাম বা ফোন নাম্বার খুঁজুন...", "Search customer phone or name..."), fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_customer_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Walk-in Option
                    item {
                        Card(
                            onClick = { onSelect(null) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
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
                                    Text(loc(isEnglish, "খুচরা ক্রেতা (Walk-in Customer)", "Walk-in Customer"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(loc(isEnglish, "কোনো নির্দিষ্ট খাতা নেই", "No persistent ledger account"), fontSize = 11.sp, color = Color.Gray)
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
                Text(loc(isEnglish, "বাতিল", "Cancel"), fontWeight = FontWeight.Bold)
            }
        }
    )
}
