package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.viewmodel.AppScreen
import com.example.viewmodel.ReportFilter
import com.example.viewmodel.SalesBookViewModel

@Composable
fun DashboardScreen(viewModel: SalesBookViewModel) {
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val salesList by viewModel.sales.collectAsStateWithLifecycle()
    val saleItemsList by viewModel.allSaleItems.collectAsStateWithLifecycle()
    val productsList by viewModel.products.collectAsStateWithLifecycle()
    val activeFilter by viewModel.reportFilter.collectAsStateWithLifecycle()

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

    val lowStockProducts = remember(productsList) {
        productsList.filter { it.stock <= 5 }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Profile Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Shop Icon",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = shopProfile.name,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone Icon",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = shopProfile.phone,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location Icon",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = shopProfile.address,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Date Filter Selection Row
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportFilterButton(
                        label = "আজ (Today)",
                        isSelected = activeFilter == ReportFilter.Today,
                        onClick = { viewModel.setReportFilter(ReportFilter.Today) },
                        modifier = Modifier.weight(1f).testTag("filter_today")
                    )
                    ReportFilterButton(
                        label = "এই মাস (Month)",
                        isSelected = activeFilter == ReportFilter.CurrentMonth,
                        onClick = { viewModel.setReportFilter(ReportFilter.CurrentMonth) },
                        modifier = Modifier.weight(1f).testTag("filter_month")
                    )
                }
            }
        }

        // Analytics KPIs Grid
        item {
            val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

            val salesBgColor = MaterialTheme.colorScheme.primaryContainer
            val salesTextColor = MaterialTheme.colorScheme.onPrimaryContainer
            val salesIconTint = MaterialTheme.colorScheme.primary

            val paidBgColor = if (isDark) Color(0xFF0C2B18) else Color(0xFFE8F5E9)
            val paidTextColor = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
            val paidIconTint = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)

            val dueBgColor = if (isDark) Color(0xFF3B0C11) else Color(0xFFFFEBEE)
            val dueTextColor = if (isDark) Color(0xFFFFB4A7) else Color(0xFFC62828)
            val dueIconTint = if (isDark) Color(0xFFFFB4A7) else Color(0xFFC62828)

            val profitBgColor = if (isDark) Color(0xFF0A2B30) else Color(0xFFE0F7FA)
            val profitTextColor = if (isDark) Color(0xFF80DEEA) else Color(0xFF00838F)
            val profitIconTint = if (isDark) Color(0xFF80DEEA) else Color(0xFF00838F)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "মোট বিক্রয় (Total Sales)",
                        value = "৳${String.format("%.1f", totalSales)}",
                        icon = Icons.Default.TrendingUp,
                        containerColor = salesBgColor,
                        contentColor = salesTextColor,
                        iconTint = salesIconTint,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "মোট নগদ (Received)",
                        value = "৳${String.format("%.1f", totalPaid)}",
                        icon = Icons.Default.Payments,
                        containerColor = paidBgColor,
                        contentColor = paidTextColor,
                        iconTint = paidIconTint,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "মোট বাকি (Total Due)",
                        value = "৳${String.format("%.1f", totalDue)}",
                        icon = Icons.Default.HistoryEdu,
                        containerColor = dueBgColor,
                        contentColor = dueTextColor,
                        iconTint = dueIconTint,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "নিট লাভ (Net Profit)",
                        value = "৳${String.format("%.1f", netProfit)}",
                        icon = Icons.Default.MonetizationOn,
                        containerColor = profitBgColor,
                        contentColor = profitTextColor,
                        iconTint = profitIconTint,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Navigation Section
        item {
            Text(
                text = "দ্রুত মেনু (Quick Menu)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickNavCard(
                    label = "নতুন বেচাকেনা\n(New Sale)",
                    icon = Icons.Default.AddShoppingCart,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = { viewModel.navigateTo(AppScreen.NewSale) },
                    modifier = Modifier.weight(1f).testTag("quick_nav_sale")
                )
                QuickNavCard(
                    label = "পণ্য তালিকা\n(Products)",
                    icon = Icons.Default.Inventory2,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "স্টক ফুরিয়ে যাচ্ছে এমন পণ্য (${lowStockProducts.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            items(lowStockProducts) { product ->
                LowStockItemRow(product)
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Perfect Stock Status",
                            tint = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "সব পণ্যের স্টক পর্যাপ্ত রয়েছে!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
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
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        modifier = modifier
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = contentColor.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )
        }
    }
}

@Composable
fun QuickNavCard(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun LowStockItemRow(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "বিক্রয়মূল্য: ৳${product.sellingPrice}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "স্টক: ${product.stock} টি",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
