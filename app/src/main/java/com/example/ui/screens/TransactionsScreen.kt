package com.example.ui.screens

import android.widget.Toast
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import java.util.Calendar
import java.util.Locale
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

enum class LedgerDateFilter {
    ALL, TODAY, CUSTOM_MONTH, CUSTOM_DATE
}

@Composable
fun TransactionsScreen(viewModel: SalesBookViewModel) {
    val context = LocalContext.current
    val salesList by viewModel.sales.collectAsStateWithLifecycle()
    val saleItemsList by viewModel.allSaleItems.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }
    var saleToCollectDue by remember { mutableStateOf<Sale?>(null) }
    var collectAmountInput by remember { mutableStateOf("") }
    var expandedSaleId by remember { mutableStateOf<Long?>(null) }
    var showOnlyDues by remember { mutableStateOf(false) }

    // Date filters
    var dateFilter by remember { mutableStateOf(LedgerDateFilter.ALL) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedMonthIndex by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) } // default to current month
    var showMonthPickerDialog by remember { mutableStateOf(false) }

    val now = Calendar.getInstance()
    val filteredSales = remember(salesList, searchQuery, showOnlyDues, dateFilter, selectedDate, selectedMonthIndex) {
        val baseList = if (searchQuery.trim().isEmpty()) {
            salesList
        } else {
            salesList.filter {
                it.customerName.contains(searchQuery, ignoreCase = true)
            }
        }
        
        val dateFiltered = baseList.filter { sale ->
            val saleCal = Calendar.getInstance().apply { timeInMillis = sale.timestamp }
            when (dateFilter) {
                LedgerDateFilter.ALL -> true
                LedgerDateFilter.TODAY -> {
                    saleCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    saleCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                }
                LedgerDateFilter.CUSTOM_MONTH -> {
                    saleCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    saleCal.get(Calendar.MONTH) == selectedMonthIndex
                }
                LedgerDateFilter.CUSTOM_DATE -> {
                    if (selectedDate == null) {
                        true
                    } else {
                        val selCal = Calendar.getInstance().apply { time = selectedDate!! }
                        saleCal.get(Calendar.YEAR) == selCal.get(Calendar.YEAR) &&
                        saleCal.get(Calendar.DAY_OF_YEAR) == selCal.get(Calendar.DAY_OF_YEAR)
                    }
                }
            }
        }

        if (showOnlyDues) {
            dateFiltered.filter { it.dueAmount > 0 }
        } else {
            dateFiltered
        }
    }

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        if (uri != null) {
            generatePdfReport(
                context = context,
                salesList = filteredSales,
                saleItemsList = saleItemsList,
                isEnglish = isEnglish,
                shopProfile = shopProfile,
                dateFilter = dateFilter,
                selectedDate = selectedDate,
                selectedMonthIndex = selectedMonthIndex,
                targetUri = uri
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title & PDF Export Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            // Export to PDF Button
            OutlinedButton(
                onClick = {
                    if (filteredSales.isEmpty()) {
                        val emptyMsg = loc(isEnglish, "এক্সপোর্ট করার মতো কোনো লেনদেন নেই!", "No transactions to export under current filter!")
                        Toast.makeText(context, emptyMsg, Toast.LENGTH_SHORT).show()
                    } else {
                        val sdfName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val timeStamp = sdfName.format(Date())
                        createPdfLauncher.launch("Ledger_Report_$timeStamp.pdf")
                    }
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = loc(isEnglish, "পিডিএফ", "PDF"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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

        // Date Filters Row (Chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ALL Chip
            FilterChip(
                selected = dateFilter == LedgerDateFilter.ALL,
                onClick = { dateFilter = LedgerDateFilter.ALL },
                label = { Text(loc(isEnglish, "সব", "All"), fontSize = 12.sp) }
            )
            // TODAY Chip
            FilterChip(
                selected = dateFilter == LedgerDateFilter.TODAY,
                onClick = { dateFilter = LedgerDateFilter.TODAY },
                label = { Text(loc(isEnglish, "আজ", "Today"), fontSize = 12.sp) }
            )
            // MONTH Chip
            FilterChip(
                selected = dateFilter == LedgerDateFilter.CUSTOM_MONTH,
                onClick = { showMonthPickerDialog = true },
                label = {
                    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
                    val monthsEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    val monthStr = if (isEnglish) monthsEn[selectedMonthIndex] else monthsBn[selectedMonthIndex]
                    Text(
                        text = if (dateFilter == LedgerDateFilter.CUSTOM_MONTH) {
                            "${loc(isEnglish, "মাস", "Month")}: $monthStr"
                        } else {
                            loc(isEnglish, "নির্দিষ্ট মাস", "Select Month")
                        },
                        fontSize = 12.sp
                    )
                }
            )
            // DATE Chip
            FilterChip(
                selected = dateFilter == LedgerDateFilter.CUSTOM_DATE,
                onClick = {
                    val calendar = Calendar.getInstance()
                    val datePickerDialog = android.app.DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val selectedCal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            }
                            selectedDate = selectedCal.time
                            dateFilter = LedgerDateFilter.CUSTOM_DATE
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePickerDialog.show()
                },
                label = {
                    val dateStr = selectedDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) }
                    Text(
                        text = if (dateFilter == LedgerDateFilter.CUSTOM_DATE && dateStr != null) {
                            dateStr
                        } else {
                            loc(isEnglish, "নির্দিষ্ট তারিখ", "Select Date")
                        },
                        fontSize = 12.sp
                    )
                }
            )
        }

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

    // Month picker Dialog
    if (showMonthPickerDialog) {
        val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        
        AlertDialog(
            onDismissRequest = { showMonthPickerDialog = false },
            title = {
                Text(
                    text = loc(isEnglish, "মাস নির্বাচন করুন", "Select Month"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 0..11) {
                        TextButton(
                            onClick = {
                                selectedMonthIndex = i
                                dateFilter = LedgerDateFilter.CUSTOM_MONTH
                                showMonthPickerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isEnglish) monthsEn[i] else monthsBn[i],
                                fontSize = 14.sp,
                                color = if (selectedMonthIndex == i && dateFilter == LedgerDateFilter.CUSTOM_MONTH) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (selectedMonthIndex == i && dateFilter == LedgerDateFilter.CUSTOM_MONTH) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMonthPickerDialog = false }) {
                    Text(loc(isEnglish, "বন্ধ করুন", "Close"))
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

fun generatePdfReport(
    context: android.content.Context,
    salesList: List<Sale>,
    saleItemsList: List<SaleItem>,
    isEnglish: Boolean,
    shopProfile: com.example.data.model.ShopProfile,
    dateFilter: LedgerDateFilter,
    selectedDate: Date?,
    selectedMonthIndex: Int,
    targetUri: android.net.Uri
) {
    try {
        val pdfDocument = PdfDocument()
        
        // Setup Paint
        val paint = Paint()
        val titlePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subTitlePaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val infoPaint = Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val headerPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val boldTextPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val linePaint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            strokeWidth = 1f
        }
        val thickLinePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            strokeWidth = 1.5f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Header Function
        fun drawHeader(canvas: android.graphics.Canvas) {
            // Draw Shop Name
            val shopName = shopProfile.name.ifBlank { if (isEnglish) "My Shop" else "আমার দোকান" }
            canvas.drawText(shopName, 40f, 50f, titlePaint)
            
            // Draw Shop Info
            var yPos = 70f
            if (shopProfile.phone.isNotBlank()) {
                canvas.drawText("${if (isEnglish) "Phone: " else "ফোন: "}${shopProfile.phone}", 40f, yPos, infoPaint)
                yPos += 16f
            }
            if (shopProfile.address.isNotBlank()) {
                canvas.drawText("${if (isEnglish) "Address: " else "ঠিকানা: "}${shopProfile.address}", 40f, yPos, infoPaint)
                yPos += 16f
            }
            
            // Draw Report Title
            val reportTitle = if (isEnglish) "Sales Ledger Report" else "বেচাকেনার খাতা ও লেজার রিপোর্ট"
            canvas.drawText(reportTitle, 40f, yPos, subTitlePaint)
            yPos += 18f
            
            // Draw Filters & Metadata
            val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
            val dateStr = sdf.format(Date())
            
            val filterText = when (dateFilter) {
                LedgerDateFilter.ALL -> if (isEnglish) "Filter: All Transactions" else "ফিল্টার: সকল লেনদেন"
                LedgerDateFilter.TODAY -> if (isEnglish) "Filter: Today" else "ফিল্টার: আজকের লেনদেন"
                LedgerDateFilter.CUSTOM_MONTH -> {
                    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
                    val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                    val m = if (isEnglish) monthsEn[selectedMonthIndex] else monthsBn[selectedMonthIndex]
                    "${if (isEnglish) "Filter Month: " else "ফিল্টার মাস: "}$m"
                }
                LedgerDateFilter.CUSTOM_DATE -> {
                    val dStr = selectedDate?.let { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) } ?: ""
                    "${if (isEnglish) "Filter Date: " else "ফিল্টার তারিখ: "}$dStr"
                }
            }
            
            canvas.drawText("$filterText | ${if (isEnglish) "Exported: " else "রপ্তানি করা হয়েছে: "}$dateStr", 40f, yPos, infoPaint)
            yPos += 10f
            
            // Draw divider line
            canvas.drawLine(40f, yPos, 555f, yPos, thickLinePaint)
        }

        // Draw header on first page
        drawHeader(canvas)
        var currentY = 160f

        // Draw Table Header
        fun drawTableHeader(canvas: android.graphics.Canvas, y: Float) {
            canvas.drawText(if (isEnglish) "ID" else "আইডি", 40f, y, headerPaint)
            canvas.drawText(if (isEnglish) "Customer Name" else "ক্রেতার নাম", 90f, y, headerPaint)
            canvas.drawText(if (isEnglish) "Date" else "তারিখ", 260f, y, headerPaint)
            canvas.drawText(if (isEnglish) "Total" else "মোট টাকা", 360f, y, headerPaint)
            canvas.drawText(if (isEnglish) "Paid" else "পরিশোধ", 430f, y, headerPaint)
            canvas.drawText(if (isEnglish) "Due" else "বাকি", 500f, y, headerPaint)
            
            canvas.drawLine(40f, y + 6f, 555f, y + 6f, linePaint)
        }

        drawTableHeader(canvas, currentY)
        currentY += 20f

        val sdfDate = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())

        for (sale in salesList) {
            // Check if we need to start a new page
            if (currentY > 780f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                
                // Draw a simple header on secondary pages
                canvas.drawText(shopProfile.name.ifBlank { if (isEnglish) "My Shop" else "আমার দোকান" }, 40f, 40f, infoPaint)
                canvas.drawText("${if (isEnglish) "Page " else "পৃষ্ঠা "}$pageNumber", 500f, 40f, infoPaint)
                canvas.drawLine(40f, 48f, 555f, 48f, linePaint)
                
                currentY = 70f
                drawTableHeader(canvas, currentY)
                currentY += 20f
            }

            val saleDate = sdfDate.format(Date(sale.timestamp))
            canvas.drawText("#${sale.id}", 40f, currentY, textPaint)
            
            val custName = sale.customerName.ifBlank { if (isEnglish) "Walk-in Customer" else "সাধারণ ক্রেতা" }
            val truncatedName = if (custName.length > 25) custName.take(23) + ".." else custName
            canvas.drawText(truncatedName, 90f, currentY, textPaint)
            
            canvas.drawText(saleDate, 260f, currentY, textPaint)
            canvas.drawText("৳${String.format("%.1f", sale.totalAmount)}", 360f, currentY, textPaint)
            canvas.drawText("৳${String.format("%.1f", sale.paidAmount)}", 430f, currentY, textPaint)
            canvas.drawText("৳${String.format("%.1f", sale.dueAmount)}", 500f, currentY, textPaint)

            canvas.drawLine(40f, currentY + 4f, 555f, currentY + 4f, linePaint)
            currentY += 18f
        }

        // Draw Totals Row
        if (currentY > 750f) {
            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
            
            canvas.drawText(shopProfile.name.ifBlank { if (isEnglish) "My Shop" else "আমার দোকান" }, 40f, 40f, infoPaint)
            canvas.drawText("${if (isEnglish) "Page " else "পৃষ্ঠা "}$pageNumber", 500f, 40f, infoPaint)
            canvas.drawLine(40f, 48f, 555f, 48f, linePaint)
            currentY = 70f
        }

        currentY += 10f
        canvas.drawLine(40f, currentY, 555f, currentY, thickLinePaint)
        currentY += 15f

        val totalSales = salesList.sumOf { it.totalAmount }
        val totalPaid = salesList.sumOf { it.paidAmount }
        val totalDue = salesList.sumOf { it.dueAmount }

        canvas.drawText(if (isEnglish) "GRAND TOTALS:" else "সর্বমোট হিসাব:", 40f, currentY, boldTextPaint)
        canvas.drawText("৳${String.format("%.1f", totalSales)}", 360f, currentY, boldTextPaint)
        canvas.drawText("৳${String.format("%.1f", totalPaid)}", 430f, currentY, boldTextPaint)
        canvas.drawText("৳${String.format("%.1f", totalDue)}", 500f, currentY, boldTextPaint)

        pdfDocument.finishPage(page)

        // Write the PDF file to chosen storage Uri
        context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        val successMsg = if (isEnglish) "PDF exported successfully to selected memory location!" else "পিডিএফ রিপোর্টটি সফলভাবে মেমরি লোকেশনে এক্সপোর্ট হয়েছে!"
        Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "PDF Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
