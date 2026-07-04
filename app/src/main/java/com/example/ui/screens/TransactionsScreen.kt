package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlassCard
import com.example.ui.components.loc
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.viewmodel.SalesBookViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(viewModel: SalesBookViewModel) {
    val context = LocalContext.current
    val salesList by viewModel.sales.collectAsStateWithLifecycle()
    val saleItemsList by viewModel.allSaleItems.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }
    var saleToCollectDue by remember { mutableStateOf<Sale?>(null) }
    var collectAmountInput by remember { mutableStateOf("") }
    var expandedSaleId by remember { mutableStateOf<Long?>(null) }
    var showOnlyDues by remember { mutableStateOf(false) }

    val filteredSales = remember(salesList, searchQuery, showOnlyDues) {
        val baseList = if (searchQuery.trim().isEmpty()) {
            salesList
        } else {
            salesList.filter {
                it.customerName.contains(searchQuery, ignoreCase = true)
            }
        }
        if (showOnlyDues) {
            baseList.filter { it.dueAmount > 0 }
        } else {
            baseList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.HistoryEdu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = loc(isEnglish, "বেচাকেনার খাতা ও লেজার", "Sales Ledger & Records"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Search Ledger
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(loc(isEnglish, "ক্রেতার নাম দিয়ে খাতা খুঁজুন...", "Search ledger by customer name..."), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("ledger_search_input"),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
            shape = RoundedCornerShape(8.dp)
        )

        // Totals summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val totalSales = filteredSales.sumOf { it.totalAmount }
            val totalDues = filteredSales.sumOf { it.dueAmount }

            GlassCard(
                modifier = Modifier.weight(1f),
                padding = 12.dp
            ) {
                Column {
                    Text(loc(isEnglish, "মোট বেচাকেনা", "Total Revenue"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("৳${String.format("%.1f", totalSales)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }

            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (showOnlyDues) {
                            Modifier.border(2.dp, Color(0xFFC62828), RoundedCornerShape(12.dp))
                        } else {
                            Modifier
                        }
                    ),
                padding = 12.dp,
                onClick = { showOnlyDues = !showOnlyDues }
            ) {
                Column {
                    Text(
                        text = loc(isEnglish, "মোট বাকি (Dues)", "Outstanding Dues"),
                        fontSize = 11.sp,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("৳${String.format("%.1f", totalDues)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC62828))
                        if (showOnlyDues) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtered",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Dues Filter Indicator Banner
        if (showOnlyDues) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC62828).copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFC62828).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Active",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = loc(isEnglish, "শুধুমাত্র বকেয়া লেনদেনের তালিকা", "Showing only outstanding due sales"),
                        color = Color(0xFFC62828),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = { showOnlyDues = false },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Filter",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Transactions List
        if (filteredSales.isEmpty()) {
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
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            loc(isEnglish, "এই নামের কোনো লেনদেন রেকর্ড নেই!", "No transaction records matching search!")
                        } else if (showOnlyDues) {
                            loc(isEnglish, "বর্তমানে কোনো বকেয়া লেনদেন নেই!", "No outstanding due transactions found!")
                        } else {
                            loc(
                                isEnglish,
                                "লেনদেনের খাতা খালি! কোনো বিক্রি সম্পন্ন হলে তার রেকর্ড এখানে দেখতে পাবেন।",
                                "Ledger is empty! Completed sales invoices will be automatically recorded here."
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
                items(filteredSales, key = { it.id }) { sale ->
                    val isExpanded = expandedSaleId == sale.id
                    val saleItems = remember(saleItemsList) {
                        saleItemsList.filter { it.saleId == sale.id }
                    }

                    TransactionItemCard(
                        sale = sale,
                        items = saleItems,
                        isExpanded = isExpanded,
                        isEnglish = isEnglish,
                        onToggleExpand = {
                            expandedSaleId = if (isExpanded) null else sale.id
                        },
                        onDelete = { saleToDelete = sale },
                        onCollectDue = {
                            saleToCollectDue = sale
                            collectAmountInput = sale.dueAmount.toString()
                        }
                    )
                }
            }
        }
    }

    // Safety delete confirmation
    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text(loc(isEnglish, "লেনদেন বাতিলের সতর্কতা", "Cancel Invoice Transaction"), fontWeight = FontWeight.Bold) },
            text = { Text(loc(isEnglish, "আপনি কি নিশ্চিতভাবে এই লেনদেনটি বাতিল করতে চান? এটি বাতিল করলে খাতা থেকে হিসাব মুছে যাবে এবং বিক্রি করা পণ্যের স্টক সংখ্যা পূর্বাবস্থায় ফিরে আসবে।", "Are you sure you want to cancel this transaction? This will permanently delete the ledger invoice and restore back product stocks.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSaleTransaction(sale.id)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("delete_sale_confirm")
                ) {
                    Text(loc(isEnglish, "হ্যাঁ, ডিলিট করুন", "Yes, Cancel"))
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text(loc(isEnglish, "বাতিল", "Cancel"))
                }
            }
        )
    }

    // Collect Sale Due Dialog
    saleToCollectDue?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToCollectDue = null },
            title = { Text(loc(isEnglish, "বাকি টাকা আদায় করুন", "Collect Invoice Due Payment"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${loc(isEnglish, "ক্রেতার নাম: ", "Customer Name: ")}${sale.customerName}", fontWeight = FontWeight.Bold)
                    Text("${loc(isEnglish, "সর্বোচ্চ বাকি পরিমাণ: ৳", "Outstanding Invoice Due: ৳")}${String.format("%.1f", sale.dueAmount)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = collectAmountInput,
                        onValueChange = { collectAmountInput = it },
                        label = { Text(loc(isEnglish, "আদায়কৃত টাকা (Amount)", "Collected Amount (৳)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("collect_sale_due_input_field"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = collectAmountInput.toDoubleOrNull()
                        if (amt == null || amt <= 0) {
                            val msg = loc(isEnglish, "সদয় হয়ে সঠিক টাকার পরিমাণ লিখুন।", "Please enter a valid amount.")
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (amt > sale.dueAmount) {
                            val msg = loc(isEnglish, "বাকি পরিমাণের চেয়ে বেশি আদায় সম্ভব নয়।", "Cannot collect more than the outstanding invoice due.")
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.collectSaleDue(sale.id, amt)
                        saleToCollectDue = null
                        val msg = loc(isEnglish, "বাকি সফলভাবে আদায় করা হয়েছে!", "Due payment collected successfully!")
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("collect_sale_due_confirm_btn")
                ) {
                    Text(loc(isEnglish, "নিশ্চিত করুন", "Confirm"))
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToCollectDue = null }) {
                    Text(loc(isEnglish, "বাতিল", "Cancel"))
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(
    sale: Sale,
    items: List<SaleItem>,
    isExpanded: Boolean,
    isEnglish: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit,
    onCollectDue: () -> Unit
) {
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
        padding = 14.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main clickable banner row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = sale.customerName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        PaymentStatusBadge(status = sale.paymentType, isEnglish = isEnglish)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTimestamp(sale.timestamp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(loc(isEnglish, "মোট বিল", "Total"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text("৳${sale.totalAmount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Column {
                            Text(loc(isEnglish, "জমা", "Paid"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text("৳${sale.paidAmount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Column {
                            Text(loc(isEnglish, "বাকি", "Due"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text(
                                "৳${sale.dueAmount}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sale.dueAmount > 0) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (sale.dueAmount > 0) {
                        Button(
                            onClick = onCollectDue,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("collect_sale_due_btn_${sale.id}")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(loc(isEnglish, "আদায়", "Collect"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp).testTag("delete_sale_${sale.id}")) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete transaction",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onToggleExpand, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Expand details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.rotate(rotationState).size(20.dp)
                        )
                    }
                }
            }

            // Expanded details: List of sold items
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = loc(isEnglish, "ক্রয়কৃত পণ্যের বিবরণ (Sold Items):", "Sold Items Breakdown:"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (items.isEmpty()) {
                        Text(loc(isEnglish, "পণ্যের বিবরণ লোড হচ্ছে...", "Loading item details..."), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.productName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = loc(isEnglish, "৳${item.sellingPrice} x ${item.quantity} টি", "৳${item.sellingPrice} x ${item.quantity} Pcs"),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "৳${item.sellingPrice * item.quantity}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentStatusBadge(status: String, isEnglish: Boolean) {
    val (label, bgColor, textColor) = when (status) {
        "Full Cash" -> Triple(loc(isEnglish, "পরিশোধ (Cash)", "Paid (Cash)"), Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "Full Due" -> Triple(loc(isEnglish, "বাকি (Due)", "Due (Unpaid)"), Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Triple(loc(isEnglish, "আংশিক (Partial)", "Partial"), Color(0xFFFFF3E0), Color(0xFFE65100))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(date)
    } catch (e: Exception) {
        ""
    }
}
