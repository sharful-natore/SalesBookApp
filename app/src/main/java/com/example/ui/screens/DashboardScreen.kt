package com.example.ui.screens

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.Product
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ReportFilter
import com.example.viewmodel.SalesBookViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.loc

@Composable
fun DashboardScreen(viewModel: SalesBookViewModel) {
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val salesList by viewModel.sales.collectAsStateWithLifecycle()
    val saleItemsList by viewModel.allSaleItems.collectAsStateWithLifecycle()
    val productsList by viewModel.products.collectAsStateWithLifecycle()
    val activeFilter by viewModel.reportFilter.collectAsStateWithLifecycle()
    val customersList by viewModel.customers.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    // Filter calculations
    val filteredSales = remember(salesList, activeFilter) {
        salesList.filter { viewModel.isTimestampInFilter(it.timestamp) }
    }
    
    val filteredSaleIds = remember(filteredSales) {
        filteredSales.map { it.id }.toSet()
    }
    
    val filteredItems = remember(saleItemsList, filteredSaleIds) {
        saleItemsList.filter { it.saleId in filteredSaleIds }
    }

    val totalSales = filteredSales.sumOf { it.totalAmount }
    val totalPaid = filteredSales.sumOf { it.paidAmount }
    val totalDue = filteredSales.sumOf { it.dueAmount }
    val netProfit = filteredItems.sumOf { (it.sellingPrice - it.purchasePrice) * it.quantity }
    val totalProductsCount = productsList.size
    val totalCustomersCount = customersList.size

    val lowStockProducts = remember(productsList) {
        productsList.filter { product -> product.stock <= 5 }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Business Profile Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!shopProfile.logoUri.isNullOrBlank()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = shopProfile.logoUri),
                                contentDescription = "Shop Custom Icon",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Shop Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = shopProfile.name.ifBlank { loc(isEnglish, "আমার দোকান", "My Shop") },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (shopProfile.phone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Phone Icon",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = shopProfile.phone,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        
                        if (shopProfile.address.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Location Icon",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = shopProfile.address,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Date Filter Selection Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 14.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportFilterButton(
                            label = loc(isEnglish, "আজ", "Today"),
                            isSelected = activeFilter == ReportFilter.Today,
                            onClick = { viewModel.setReportFilter(ReportFilter.Today) },
                            modifier = Modifier.weight(1f).testTag("filter_today")
                        )
                        ReportFilterButton(
                            label = loc(isEnglish, "এই মাস", "This Month"),
                            isSelected = activeFilter == ReportFilter.CurrentMonth,
                            onClick = { viewModel.setReportFilter(ReportFilter.CurrentMonth) },
                            modifier = Modifier.weight(1f).testTag("filter_month")
                        )
                        ReportFilterButton(
                            label = loc(isEnglish, "নির্দিষ্ট তারিখ", "Custom Range"),
                            isSelected = activeFilter == ReportFilter.Custom,
                            onClick = { viewModel.setReportFilter(ReportFilter.Custom) },
                            modifier = Modifier.weight(1.2f).testTag("filter_custom")
                        )
                    }

                    if (activeFilter == ReportFilter.Custom) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val context = LocalContext.current
                            val customStart by viewModel.customStartDate.collectAsStateWithLifecycle()
                            val customEnd by viewModel.customEndDate.collectAsStateWithLifecycle()

                            Button(
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val selectedCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            viewModel.setCustomStartDate(selectedCal.timeInMillis)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                val text = customStart?.let { formatShortDate(it) } ?: loc(isEnglish, "শুরুর তারিখ", "Start Date")
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    android.app.DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val selectedCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, year)
                                                set(Calendar.MONTH, month)
                                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                                set(Calendar.HOUR_OF_DAY, 23)
                                                set(Calendar.MINUTE, 59)
                                                set(Calendar.SECOND, 59)
                                                set(Calendar.MILLISECOND, 999)
                                            }
                                            viewModel.setCustomEndDate(selectedCal.timeInMillis)
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                val text = customEnd?.let { formatShortDate(it) } ?: loc(isEnglish, "শেষের তারিখ", "End Date")
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Analytics KPIs Grid
        item {
            val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

            val salesIconTint = MaterialTheme.colorScheme.primary
            val paidIconTint = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
            val dueIconTint = if (isDark) Color(0xFFFFB4A7) else Color(0xFFC62828)
            val profitIconTint = if (isDark) Color(0xFF80DEEA) else Color(0xFF00838F)
            val productsIconTint = MaterialTheme.colorScheme.secondary
            val customersIconTint = MaterialTheme.colorScheme.tertiary

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = loc(isEnglish, "মোট বিক্রয়", "Total Sales"),
                        value = "৳${String.format("%.1f", totalSales)}",
                        icon = Icons.Default.TrendingUp,
                        iconTint = salesIconTint,
                        onClick = { viewModel.navigateTo(AppScreen.Transactions) },
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = loc(isEnglish, "মোট নগদ", "Total Received"),
                        value = "৳${String.format("%.1f", totalPaid)}",
                        icon = Icons.Default.Payments,
                        iconTint = paidIconTint,
                        onClick = { viewModel.navigateTo(AppScreen.Transactions) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = loc(isEnglish, "মোট বাকি", "Total Due"),
                        value = "৳${String.format("%.1f", totalDue)}",
                        icon = Icons.Default.HistoryEdu,
                        iconTint = dueIconTint,
                        onClick = { viewModel.navigateTo(AppScreen.Customers) },
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = loc(isEnglish, "নিট লাভ", "Net Profit"),
                        value = "৳${String.format("%.1f", netProfit)}",
                        icon = Icons.Default.MonetizationOn,
                        iconTint = profitIconTint,
                        onClick = { viewModel.navigateTo(AppScreen.Transactions) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = loc(isEnglish, "মোট পণ্য", "Total Products"),
                        value = loc(isEnglish, "$totalProductsCount টি", "$totalProductsCount Items"),
                        icon = Icons.Default.Inventory2,
                        iconTint = productsIconTint,
                        onClick = { viewModel.navigateTo(AppScreen.Products) },
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = loc(isEnglish, "মোট ক্রেতা", "Total Customers"),
                        value = loc(isEnglish, "$totalCustomersCount জন", "$totalCustomersCount Customers"),
                        icon = Icons.Default.People,
                        iconTint = customersIconTint,
                        onClick = { viewModel.navigateTo(AppScreen.Customers) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Navigation Section
        item {
            Text(
                text = loc(isEnglish, "দ্রুত মেনু", "Quick Access Menu"),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickNavCard(
                    label = loc(isEnglish, "নতুন বেচাকেনা", "New Sale booking"),
                    icon = Icons.Default.AddShoppingCart,
                    onClick = { viewModel.navigateTo(AppScreen.NewSale) },
                    modifier = Modifier.weight(1f).testTag("quick_nav_sale")
                )
                QuickNavCard(
                    label = loc(isEnglish, "পণ্য তালিকা", "Product Directory"),
                    icon = Icons.Default.Inventory2,
                    onClick = { viewModel.navigateTo(AppScreen.Products) },
                    modifier = Modifier.weight(1f).testTag("quick_nav_products")
                )
            }
        }

        // Low Stock Notifications
        if (lowStockProducts.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = loc(
                            isEnglish,
                            "স্টক ফুরিয়ে যাচ্ছে এমন পণ্য (${lowStockProducts.size})",
                            "Low Stock Alert (${lowStockProducts.size})"
                        ),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            items(lowStockProducts) { product ->
                LowStockItemRow(product, isEnglish)
            }
        } else {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 12.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Perfect Stock Status",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = loc(isEnglish, "সব পণ্যের স্টক পর্যাপ্ত রয়েছে!", "All items have sufficient stock level!"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportFilterButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier.height(38.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

fun formatShortDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier,
        padding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun QuickNavCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier,
        padding = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LowStockItemRow(product: Product, isEnglish: Boolean) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = loc(isEnglish, "বিক্রয়মূল্য: ৳${product.sellingPrice}", "Price: ৳${product.sellingPrice}"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = loc(isEnglish, "স্টক: ${product.stock} ${product.unit}", "Stock: ${product.stock} ${product.unit}"),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
