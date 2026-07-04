package com.example.ui.screens

import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.ui.components.GlassCard
import com.example.ui.components.loc
import com.example.data.model.Customer
import com.example.viewmodel.SalesBookViewModel

@Composable
fun CustomersScreen(viewModel: SalesBookViewModel) {
    val context = LocalContext.current
    val customersList by viewModel.customers.collectAsStateWithLifecycle()
    val salesList by viewModel.sales.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var customerToCollectDue by remember { mutableStateOf<Customer?>(null) }
    var collectAmountInput by remember { mutableStateOf("") }
    var selectedCustomerProfile by remember { mutableStateOf<Customer?>(null) }

    val customerPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            selectedCustomerProfile?.let { customer ->
                val updatedCustomer = customer.copy(photoUri = uri.toString())
                viewModel.updateCustomer(updatedCustomer) {
                    selectedCustomerProfile = updatedCustomer
                }
            }
        }
    }

    val filteredCustomers = remember(customersList, searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            customersList
        } else {
            customersList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Customer")
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
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = loc(isEnglish, "ক্রেতা তালিকা", "Customer Directory"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(loc(isEnglish, "নাম বা ফোন নাম্বার দিয়ে খুঁজুন...", "Search by name or mobile number..."), fontSize = 13.sp) },
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
                    .testTag("customer_search_input"),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(8.dp)
            )

            // Customers Listing
            if (filteredCustomers.isEmpty()) {
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
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                loc(isEnglish, "কোনো ক্রেতা পাওয়া যায়নি!", "No customers found matching search!")
                            } else {
                                loc(
                                    isEnglish,
                                    "ক্রেতা তালিকা খালি! নতুন ক্রেতা যুক্ত করতে নিচের বোতাম চাপুন।",
                                    "Your customer list is empty! Tap the + button at bottom right to add a customer."
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
                    items(filteredCustomers, key = { it.id }) { customer ->
                        val dueAmount = remember(salesList) {
                            salesList.filter { it.customerId == customer.id }.sumOf { it.dueAmount }
                        }
                        CustomerItemCard(
                            customer = customer,
                            dueAmount = dueAmount,
                            isEnglish = isEnglish,
                            onEdit = { customerToEdit = customer },
                            onDelete = { customerToDelete = customer },
                            onCollectDue = {
                                customerToCollectDue = customer
                                collectAmountInput = dueAmount.toString()
                            },
                            onShowProfile = {
                                selectedCustomerProfile = customer
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddDialog) {
        CustomerFormDialog(
            title = loc(isEnglish, "নতুন ক্রেতা যোগ করুন", "Add New Customer"),
            isEnglish = isEnglish,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, address ->
                viewModel.addCustomer(name, phone, address) {
                    showAddDialog = false
                }
            }
        )
    }

    // Edit Customer Dialog
    customerToEdit?.let { customer ->
        CustomerFormDialog(
            title = loc(isEnglish, "ক্রেতার তথ্য সংশোধন", "Edit Customer Info"),
            customer = customer,
            isEnglish = isEnglish,
            onDismiss = { customerToEdit = null },
            onConfirm = { name, phone, address ->
                viewModel.updateCustomer(customer.copy(name = name, phone = phone, address = address)) {
                    customerToEdit = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    customerToDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text(loc(isEnglish, "ক্রেতা মুছে ফেলার সতর্কতা", "Delete Customer Warning"), fontWeight = FontWeight.Bold) },
            text = { Text(loc(isEnglish, "'${customer.name}' ক্রেতাটিকে কি তালিকা থেকে মুছে ফেলতে চান? এটি তার কোনো অতীত খাতার তথ্য মুছবে না।", "Are you sure you want to delete '${customer.name}'? This will not remove their past sales invoices.")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer(customer) {
                            customerToDelete = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("delete_confirm_btn")
                ) {
                    Text(loc(isEnglish, "হ্যাঁ, ডিলিট করুন", "Yes, Delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text(loc(isEnglish, "বাতিল", "Cancel"))
                }
            }
        )
    }

    // Collect Due Dialog
    customerToCollectDue?.let { customer ->
        val maxDue = remember(salesList) {
            salesList.filter { it.customerId == customer.id }.sumOf { it.dueAmount }
        }
        AlertDialog(
            onDismissRequest = { customerToCollectDue = null },
            title = { Text(loc(isEnglish, "বাকি টাকা আদায় করুন", "Collect Outstanding Dues"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${loc(isEnglish, "ক্রেতার নাম: ", "Customer: ")}${customer.name}", fontWeight = FontWeight.Bold)
                    Text("${loc(isEnglish, "সর্বোচ্চ বাকি পরিমাণ: ৳", "Outstanding Amount: ৳")}${String.format("%.1f", maxDue)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = collectAmountInput,
                        onValueChange = { collectAmountInput = it },
                        label = { Text(loc(isEnglish, "আদায়কৃত টাকা", "Collected Cash Amount (৳)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("collect_due_input_field"),
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
                        viewModel.collectCustomerDue(customer.id, amt)
                        customerToCollectDue = null
                        val msg = loc(isEnglish, "বাকি সফলভাবে আদায় করা হয়েছে!", "Outstanding dues successfully received!")
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("collect_due_confirm_btn")
                ) {
                    Text(loc(isEnglish, "নিশ্চিত করুন", "Confirm"))
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToCollectDue = null }) {
                    Text(loc(isEnglish, "বাতিল", "Cancel"))
                }
            }
        )
    }

    // Customer Profile Dialog State Trigger
    selectedCustomerProfile?.let { customer ->
        val dueAmount = remember(salesList) {
            salesList.filter { it.customerId == customer.id }.sumOf { it.dueAmount }
        }
        val totalPurchased = remember(salesList) {
            salesList.filter { it.customerId == customer.id }.sumOf { it.totalAmount }
        }
        CustomerProfileDialog(
            customer = customer,
            dueAmount = dueAmount,
            totalPurchased = totalPurchased,
            isEnglish = isEnglish,
            onDismiss = { selectedCustomerProfile = null },
            onAddPhoto = {
                customerPhotoPickerLauncher.launch(arrayOf("image/*"))
            }
        )
    }
}
@Composable
fun CustomerItemCard(
    customer: Customer,
    dueAmount: Double,
    isEnglish: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCollectDue: () -> Unit,
    onShowProfile: () -> Unit
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
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowProfile() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Profile Photo or Placeholder Icon
                if (customer.photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = Uri.parse(customer.photoUri)),
                        contentDescription = "Thumbnail",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Placeholder",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = customer.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = customer.phone,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (customer.address.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Address",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = customer.address,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    if (dueAmount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoneyOff,
                                contentDescription = "Dues",
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${loc(isEnglish, "বাকি: ৳", "Dues: ৳")}${String.format("%.1f", dueAmount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE57373)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "No Dues",
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = loc(isEnglish, "পরিশোধিত (No Dues)", "Paid (No Dues)"),
                                fontSize = 13.sp,
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dueAmount > 0) {
                    Button(
                        onClick = onCollectDue,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp).testTag("collect_due_btn_${customer.id}")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(loc(isEnglish, "আদায়", "Collect"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp).testTag("edit_customer_${customer.id}")) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Customer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp).testTag("delete_customer_${customer.id}")) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Customer",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerFormDialog(
    title: String,
    customer: Customer? = null,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }

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
                    label = { Text(loc(isEnglish, "ক্রেতার নাম", "Customer Name")) },
                    modifier = Modifier.fillMaxWidth().testTag("customer_dialog_name"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(loc(isEnglish, "মোবাইল নম্বর", "Phone Number")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().testTag("customer_dialog_phone"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(loc(isEnglish, "ঠিকানা", "Address / Remarks")) },
                    modifier = Modifier.fillMaxWidth().testTag("customer_dialog_address"),
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
                        errorMsg = loc(isEnglish, "ক্রেতার নাম লিখতে হবে।" , "Please enter customer name.")
                        return@Button
                    }
                    if (phone.trim().isEmpty()) {
                        errorMsg = loc(isEnglish, "মোবাইল নম্বর লিখতে হবে।" , "Please enter a valid phone number.")
                        return@Button
                    }
                    onConfirm(name.trim(), phone.trim(), address.trim())
                },
                modifier = Modifier.testTag("customer_dialog_confirm")
            ) {
                Text(loc(isEnglish, "সংরক্ষণ", "Save Details"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(loc(isEnglish, "বাতিল", "Cancel"))
            }
        }
    )
}

@Composable
fun CustomerProfileDialog(
    customer: Customer,
    dueAmount: Double,
    totalPurchased: Double,
    isEnglish: Boolean,
    onDismiss: () -> Unit,
    onAddPhoto: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = loc(isEnglish, "ক্রেতা প্রোফাইল", "Customer Profile"),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Photo Section with Edit Overlay
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {
                    if (customer.photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = Uri.parse(customer.photoUri)),
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Placeholder",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    // Small photo trigger button
                    IconButton(
                        onClick = onAddPhoto,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Change Photo",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Name & Metadata
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = customer.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = customer.phone,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (customer.address.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = customer.address,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Financial Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Sales
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = loc(isEnglish, "মোট বিক্রয়", "Total Bought"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৳${String.format("%.1f", totalPurchased)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // Total Dues
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (dueAmount > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = loc(isEnglish, "মোট বাকি", "Outstanding Dues"),
                                fontSize = 11.sp,
                                color = if (dueAmount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "৳${String.format("%.1f", dueAmount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dueAmount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Call Action Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error making call: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Customer",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = loc(isEnglish, "কল করুন", "Call Customer"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(loc(isEnglish, "বন্ধ করুন", "Close"))
            }
        }
    )
}
