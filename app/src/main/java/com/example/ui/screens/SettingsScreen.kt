package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.SalesBookViewModel

@Composable
fun SettingsScreen(viewModel: SalesBookViewModel) {
    val context = LocalContext.current
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var shopName by remember(shopProfile) { mutableStateOf(shopProfile.name) }
    var shopPhone by remember(shopProfile) { mutableStateOf(shopProfile.phone) }
    var shopAddress by remember(shopProfile) { mutableStateOf(shopProfile.address) }

    var pasteInput by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Heading
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "অ্যাপ সেটিংস (Settings & Backup)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Section A: Shop Profile Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "দোকানের তথ্য পরিবর্তন (Shop Settings)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("দোকানের নাম (Shop Name)") },
                    modifier = Modifier.fillMaxWidth().testTag("settings_shop_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = shopPhone,
                    onValueChange = { shopPhone = it },
                    label = { Text("মোবাইল নাম্বার (Phone)") },
                    modifier = Modifier.fillMaxWidth().testTag("settings_shop_phone"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = { Text("ঠিকানা (Address)") },
                    modifier = Modifier.fillMaxWidth().testTag("settings_shop_address"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        if (shopName.trim().isEmpty()) {
                            Toast.makeText(context, "দোকানের নাম খালি রাখা যাবে না।", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.updateShopSettings(shopName.trim(), shopPhone.trim(), shopAddress.trim()) {
                            Toast.makeText(context, "দোকানের তথ্য সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("settings_save_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("তথ্য সংরক্ষণ করুন")
                }
            }
        }

        // Section B: Theme Customization
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ডার্ক মোড (Dark Theme)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "স্ক্রিন হালকা বা গাঢ় করুন",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() },
                    modifier = Modifier.testTag("theme_switch")
                )
            }
        }

        // Section C: Backup & Restore Utilities
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ডাটা ব্যাকআপ ও রিস্টোর (Database Backup)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "আপনার সকল বেচাকেনা, পণ্য ও ক্রেতার তালিকা সুরক্ষিত রাখতে ডাটা ব্যাকআপ করে রাখুন। ব্যাকআপ ফাইলটি কপি করে যেকোনো ডিভাইসে আবার রিস্টোর করতে পারবেন।",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Export Button
                    Button(
                        onClick = {
                            viewModel.exportBackup { json ->
                                exportedJson = json
                                showExportDialog = true
                                // Copy directly to clipboard
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Sales Book Backup", json)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "ব্যাকআপ ফাইল ক্লিপবোর্ডে কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("backup_export_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ব্যাকআপ নিন", fontSize = 13.sp)
                    }

                    // Import Button
                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.weight(1f).testTag("backup_import_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("রিস্টোর করুন", fontSize = 13.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    // Export Details Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("ডাটা ব্যাকআপ সফল হয়েছে", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আপনার ডাটাবেজ ব্যাকআপ সম্পন্ন হয়েছে এবং টেক্সট ফরম্যাটটি নিচের বক্সে রয়েছে। এটি স্বয়ংক্রিয়ভাবে ক্লিপবোর্ডে কপিও করা হয়েছে। নিরাপদে সংরক্ষণ করতে এটি অন্য কোথাও পেস্ট করে রাখুন।")
                    OutlinedTextField(
                        value = exportedJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("exported_json_field"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showExportDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // Import Paste Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("ব্যাকআপ ডাটা রিস্টোর করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("নিচের টেক্সট বক্সে আপনার পূর্বে কপি করে রাখা ব্যাকআপ কোডটি (JSON) পেস্ট করুন। সতর্ক থাকুন: এটি আপনার বর্তমান সকল স্থানীয় ডাটা মুছে নতুনভাবে রিস্টোর করবে।")
                    OutlinedTextField(
                        value = pasteInput,
                        onValueChange = { pasteInput = it },
                        placeholder = { Text("এখানে ব্যাকআপ কোড পেস্ট করুন...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("import_paste_field"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pasteInput.trim().isEmpty()) {
                            Toast.makeText(context, "দয়া করে কোড পেস্ট করুন!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.restoreBackup(pasteInput.trim()) { success ->
                            if (success) {
                                showImportDialog = false
                                pasteInput = ""
                                Toast.makeText(context, "ডাটা সফলভাবে রিস্টোর হয়েছে!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "রিস্টোর ব্যর্থ হয়েছে! কোডটি সঠিক কিনা যাচাই করুন।", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("import_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("নিশ্চিত রিস্টোর")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
