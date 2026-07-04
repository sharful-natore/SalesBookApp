package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.viewmodel.SalesBookViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsScreen(viewModel: SalesBookViewModel) {
    val salesList by viewModel.sales.collectAsStateWithLifecycle()
    val saleItemsList by viewModel.allSaleItems.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }
    var expandedSaleId by remember { mutableStateOf<Long?>(null) }

    val filteredSales = remember(salesList, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            salesList
        } else {
            salesList.filter {
                it.customerName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "বেচাকেনার খাতা ও লেজার (Sales Ledger & Dues)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Search Ledger
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ক্রেতার নাম দিয়ে খাতা খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ledger_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Totals summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val totalSales = filteredSales.sumOf { it.totalAmount }
            val totalDues = filteredSales.sumOf { it.dueAmount }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("মোট বেচাকেনা", fontSize = 11.sp, color = Color.Gray)
                    Text("৳${String.format("%.1f", totalSales)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)) // Soft Red
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("মোট বাকি (Dues)", fontSize = 11.sp, color = Color(0xFFC62828))
                    Text("৳${String.format("%.1f", totalDues)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Empty",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "এই নামের কোনো লেনদেন রেকর্ড নেই!" else "লেনদেনের খাতা খালি! কোনো বিক্রি সম্পন্ন হলে তার রেকর্ড এখানে দেখতে পাবেন।",
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
                items(filteredSales, key = { it.id }) { sale ->
                    val isExpanded = expandedSaleId == sale.id
                    val saleItems = remember(saleItemsList) {
                        saleItemsList.filter { it.saleId == sale.id }
                    }

                    TransactionItemCard(
                        sale = sale,
                        items = saleItems,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedSaleId = if (isExpanded) null else sale.id
                        },
                        onDelete = { saleToDelete = sale }
                    )
                }
            }
        }
    }

    // Safety delete confirmation
    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("লেনদেন বাতিলের সতর্কতা", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিতভাবে এই লেনদেনটি বাতিল করতে চান? এটি বাতিল করলে খাতা থেকে হিসাব মুছে যাবে এবং বিক্রি করা পণ্যের স্টক সংখ্যা পূর্বাবস্থায় ফিরে আসবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSaleTransaction(sale.id)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("delete_sale_confirm")
                ) {
                    Text("হ্যাঁ, ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text("বাতিল")
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
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit
) {
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main clickable banner row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(16.dp),
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
                        PaymentStatusBadge(status = sale.paymentType)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTimestamp(sale.timestamp),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("মোট বিল", fontSize = 10.sp, color = Color.Gray)
                            Text("৳${sale.totalAmount}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("জমা", fontSize = 10.sp, color = Color.Gray)
                            Text("৳${sale.paidAmount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Column {
                            Text("বাকি", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "৳${sale.dueAmount}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sale.dueAmount > 0) Color(0xFFC62828) else Color.Black
                            )
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_sale_${sale.id}")) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete transaction",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Expand details",
                            tint = Color.Gray,
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                }
            }

            // Expanded details: List of sold items
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ক্রয়কৃত পণ্যের বিবরণ (Sold Items):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (items.isEmpty()) {
                        Text("পণ্যের বিবরণ লোড হচ্ছে...", fontSize = 12.sp, color = Color.Gray)
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
                                    Text(item.productName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "৳${item.sellingPrice} x ${item.quantity} টি",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = "৳${item.sellingPrice * item.quantity}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentStatusBadge(status: String) {
    val (label, bgColor, textColor) = when (status) {
        "Full Cash" -> Triple("পরিশোধ (Cash)", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "Full Due" -> Triple("বাকি (Due)", Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Triple("আংশিক (Partial)", Color(0xFFFFF3E0), Color(0xFFE65100))
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
            fontWeight = FontWeight.Bold,
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
