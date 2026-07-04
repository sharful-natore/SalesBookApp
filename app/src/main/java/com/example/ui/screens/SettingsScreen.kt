package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.ui.components.GlassCard
import com.example.ui.components.loc
import com.example.viewmodel.SalesBookViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: SalesBookViewModel) {
    val context = LocalContext.current
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()

    var shopName by remember(shopProfile) { mutableStateOf(shopProfile.name) }
    var shopPhone by remember(shopProfile) { mutableStateOf(shopProfile.phone) }
    var shopAddress by remember(shopProfile) { mutableStateOf(shopProfile.address) }
    var logoUri by remember(shopProfile) { mutableStateOf(shopProfile.logoUri) }

    // System Image Picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
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
            logoUri = uri.toString()
        }
    }

    // Launcher to save database backup to selected location
    val backupDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportBackup { json ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                    val msg = loc(isEnglish, "ব্যাকআপ সফলভাবে মেমরিতে সেভ হয়েছে!", "Backup saved successfully to selected location!")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var pasteInput by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // System File Picker for restoring database
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader()?.use { it.readText() }
                if (!json.isNullOrBlank()) {
                    viewModel.restoreBackup(json) { success ->
                        if (success) {
                            val msg = loc(isEnglish, "ডাটা সফলভাবে রিস্টোর হয়েছে!", "Data successfully restored!")
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        } else {
                            val msg = loc(isEnglish, "রিস্টোর ব্যর্থ হয়েছে! সঠিক ব্যাকআপ ফাইল নির্বাচন করুন।", "Restore failed! Please choose a valid backup file.")
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    val msg = loc(isEnglish, "ফাইলটি খালি বা ত্রুটিযুক্ত!", "File is empty or corrupted!")
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val msg = loc(isEnglish, "ফাইল পড়তে সমস্যা হয়েছে!", "Error reading backup file!")
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section A: Shop Settings
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 16.dp
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = loc(isEnglish, "দোকানের তথ্য পরিবর্তন", "Shop Profile Configuration"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Profile Image Selection Center Box
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable {
                            try {
                                imagePickerLauncher.launch(arrayOf("image/*"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Cannot open image picker", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (!logoUri.isNullOrBlank()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = logoUri),
                            contentDescription = "Shop Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "No Logo",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Edit Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = loc(isEnglish, "এডিট করুন", "EDIT"),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text(loc(isEnglish, "দোকানের নাম", "Shop Name"), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("settings_shop_name"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = shopPhone,
                    onValueChange = { shopPhone = it },
                    label = { Text(loc(isEnglish, "মোবাইল নাম্বার", "Mobile Number"), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("settings_shop_phone"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = shopAddress,
                    onValueChange = { shopAddress = it },
                    label = { Text(loc(isEnglish, "ঠিকানা", "Location / Address"), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("settings_shop_address"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = {
                        if (shopName.trim().isEmpty()) {
                            val msg = loc(isEnglish, "দোকানের নাম খালি রাখা যাবে না।", "Shop name cannot be empty.")
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.updateShopSettings(shopName.trim(), shopPhone.trim(), shopAddress.trim(), logoUri) {
                            val msg = loc(isEnglish, "দোকানের তথ্য সফলভাবে সংরক্ষিত হয়েছে!", "Shop profile successfully saved!")
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("settings_save_btn"),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(loc(isEnglish, "তথ্য সংরক্ষণ করুন", "Save Changes"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section B: Theme and Language Options
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Theme row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = loc(isEnglish, "ডার্ক মোড (Dark Theme)", "Dark Theme Color Mode"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = loc(isEnglish, "স্ক্রিন হালকা বা গাঢ় করুন", "Switch between light and dark backgrounds"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_switch")
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Language row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = loc(isEnglish, "অ্যাপ্লিকেশন ভাষা (Language)", "Application Language"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = loc(isEnglish, "ইংরেজি বা বাংলা ভাষা নির্বাচন করুন", "Select between English or Bengali translations"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = { viewModel.toggleLanguage() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text(
                            text = if (isEnglish) "English 🇬🇧" else "বাংলা 🇧🇩",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Section C: Storage Backup & Restore Utilities
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = loc(isEnglish, "মেমরি ব্যাকআপ ও রিস্টোর (Local Backup)", "Data Storage Backup & Restore"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = loc(
                        isEnglish,
                        "নিরাপত্তার স্বার্থে আপনার সকল বেচাকেনা ও ক্রেতার তথ্য আপনার ফোন মেমরিতে ফাইল (.json) আকারে সংরক্ষণ বা রিস্টোর করুন।",
                        "For complete safety of your business records, export or import your ledger database as a highly compatible .json backup file in your device downloads directory."
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    lineHeight = 16.sp
                )

                // Primary File Backup/Restore Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export File Button
                    Button(
                        onClick = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            backupDocumentLauncher.launch("SalesBook_Backup_$timeStamp.json")
                        },
                        modifier = Modifier.weight(1f).height(40.dp).testTag("backup_export_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(loc(isEnglish, "মেমরিতে ব্যাকআপ", "Export Backup"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Import File Button
                    Button(
                        onClick = {
                            filePickerLauncher.launch("application/json")
                        },
                        modifier = Modifier.weight(1f).height(40.dp).testTag("backup_import_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(loc(isEnglish, "মেমরি থেকে রিস্টোর", "Import Backup"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Secondary Code Copy / Paste Links (As Fallbacks)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            viewModel.exportBackup { json ->
                                exportedJson = json
                                showExportDialog = true
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Sales Book Backup", json)
                                clipboard.setPrimaryClip(clip)
                                val msg = loc(isEnglish, "কোড ক্লিপবোর্ডে কপি করা হয়েছে!", "Backup code copied to clipboard!")
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(loc(isEnglish, "ম্যানুয়াল কোড কপি", "Copy Code Manual"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = { showImportDialog = true },
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(loc(isEnglish, "ম্যানুয়াল কোড পেস্ট", "Paste Code Manual"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section D: Developer Info
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 16.dp
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = loc(isEnglish, "ডেভেলপার পরিচিতি", "Developer Information"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.developer_photo),
                        contentDescription = "Developer Profile Photo",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Md. Shariful Islam",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "UX/UI Designer & Developer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = loc(isEnglish, "পারকোল, বড়াইগ্রাম, নাটোর", "Parkol, Baraigram, Natore"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = loc(isEnglish, "অ্যাপের আপডেট ভার্সন পেতে হোয়াটসঅ্যাপ এ যোগাযোগ করুন", "Contact on WhatsApp to get the updated version of the app"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF25D366),
                            lineHeight = 14.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Email
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "connect.shariful@gmail.com",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // WhatsApp
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        data = android.net.Uri.parse("https://wa.me/8801768899599")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "WhatsApp",
                            fontSize = 12.sp,
                            color = Color(0xFF25D366),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Export Details Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(loc(isEnglish, "ডাটা ব্যাকআপ সফল হয়েছে", "Data Backup Successful"), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = loc(
                            isEnglish,
                            "আপনার ডাটাবেজ ব্যাকআপ সম্পন্ন হয়েছে এবং টেক্সট ফরম্যাটটি নিচের বক্সে রয়েছে। এটি স্বয়ংক্রিয়ভাবে ক্লিপবোর্ডে কপিও করা হয়েছে। নিরাপদে সংরক্ষণ করতে এটি অন্য কোথাও পেস্ট করে রাখুন।",
                            "Your database backup is completed and the backup JSON is shown in the box below. It is automatically copied to your clipboard. Paste and store it somewhere safe."
                        ),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = exportedJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("exported_json_field"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showExportDialog = false }, shape = RoundedCornerShape(6.dp)) {
                    Text(loc(isEnglish, "ঠিক আছে", "OK"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Import Paste Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(loc(isEnglish, "ব্যাকআপ ডাটা রিস্টোর করুন", "Restore Database Backup"), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = loc(
                            isEnglish,
                            "নিচের টেক্সট বক্সে আপনার পূর্বে কপি করে রাখা ব্যাকআপ কোডটি (JSON) পেস্ট করুন। সতর্ক থাকুন: এটি আপনার বর্তমান সকল স্থানীয় ডাটা মুছে নতুনভাবে রিস্টোর করবে।",
                            "Paste your previously copied backup code (JSON format) in the text box below. WARNING: This will overwrite and permanently replace your current local store data."
                        ),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = pasteInput,
                        onValueChange = { pasteInput = it },
                        placeholder = { Text(loc(isEnglish, "এখানে ব্যাকআপ কোড পেস্ট করুন...", "Paste your JSON backup code here..."), fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
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
                            val msg = loc(isEnglish, "দয়া করে কোড পেস্ট করুন!", "Please paste your code first!")
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.restoreBackup(pasteInput.trim()) { success ->
                            if (success) {
                                showImportDialog = false
                                pasteInput = ""
                                val msg = loc(isEnglish, "ডাটা সফলভাবে রিস্টোর হয়েছে!", "Database successfully restored!")
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            } else {
                                val msg = loc(isEnglish, "রিস্টোর ব্যর্থ হয়েছে! কোডটি সঠিক কিনা যাচাই করুন।", "Restore failed! Please check if the backup code is correct.")
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("import_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(loc(isEnglish, "নিশ্চিত রিস্টোর", "Confirm Restore"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(loc(isEnglish, "বাতিল", "Cancel"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// MediaStore helper function to save the backup file directly to Downloads
fun saveBackupToPublicDownloads(context: Context, json: String): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "SalesBook_Backup_$timeStamp.json"
    
    val resolver = context.contentResolver
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                uri
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    } else {
        // Legacy fallback
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            file.writeText(json)
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
