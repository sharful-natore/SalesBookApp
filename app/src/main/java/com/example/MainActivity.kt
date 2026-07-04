package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import com.example.ui.components.GlassmorphicBackground
import com.example.ui.components.loc
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.SalesBookViewModel

class MainActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: SalesBookViewModel = viewModel()
      val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
      val isEnglish by viewModel.isEnglish.collectAsStateWithLifecycle()
      val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
      val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()

      MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
        GlassmorphicBackground {
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
              CenterAlignedTopAppBar(
                title = {
                  Text(
                    text = when (currentScreen) {
                      AppScreen.Dashboard -> loc(isEnglish, "সেলসবুক ড্যাশবোর্ড", "SalesBook Dashboard")
                      AppScreen.Products -> loc(isEnglish, "পণ্য ও ইনভেন্টরি", "Products & Inventory")
                      AppScreen.Customers -> loc(isEnglish, "ক্রেতা খাতা", "Customer Directory")
                      AppScreen.NewSale -> loc(isEnglish, "নতুন বেচাকেনা হিসাব", "New Sale Booking")
                      AppScreen.Transactions -> loc(isEnglish, "বেচাকেনার লেজার", "Sales Book Ledger")
                      AppScreen.Settings -> loc(isEnglish, "সেটিংস ও ব্যাকআপ", "Settings & Backup")
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                  )
                },
                actions = {
                  // Language Toggle (BN / EN)
                  TextButton(
                    onClick = { viewModel.toggleLanguage() },
                    modifier = Modifier.padding(end = 4.dp)
                  ) {
                    Text(
                      text = if (isEnglish) "বাংলা" else "EN",
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 14.sp,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }

                  // Theme Toggle (Light / Dark)
                  IconButton(
                    onClick = { viewModel.toggleDarkMode() }
                  ) {
                    Icon(
                      imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                      contentDescription = "Toggle Theme",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                  containerColor = if (isDarkMode) Color(0x221C2541) else Color(0xF2FFFFFF),
                  titleContentColor = MaterialTheme.colorScheme.onBackground
                )
              )
            },
            bottomBar = {
              NavigationBar(
                containerColor = if (isDarkMode) Color(0x221C2541) else Color(0xF2FFFFFF),
                modifier = Modifier.testTag("app_navigation_bar")
              ) {
                val navItems = listOf(
                  NavigationItem(loc(isEnglish, "ড্যাশবোর্ড", "Dashboard"), AppScreen.Dashboard, Icons.Default.Dashboard, "nav_dashboard"),
                  NavigationItem(loc(isEnglish, "বেচাকেনা", "New Sale"), AppScreen.NewSale, Icons.Default.AddShoppingCart, "nav_new_sale"),
                  NavigationItem(loc(isEnglish, "লেজার", "Ledger"), AppScreen.Transactions, Icons.Default.HistoryEdu, "nav_transactions"),
                  NavigationItem(loc(isEnglish, "পণ্য", "Products"), AppScreen.Products, Icons.Default.Inventory2, "nav_products"),
                  NavigationItem(loc(isEnglish, "ক্রেতা", "Customers"), AppScreen.Customers, Icons.Default.People, "nav_customers"),
                  NavigationItem(loc(isEnglish, "সেটিংস", "Settings"), AppScreen.Settings, Icons.Default.Settings, "nav_settings")
                )

                navItems.forEach { item ->
                  NavigationBarItem(
                    selected = currentScreen == item.screen,
                    onClick = { viewModel.navigateTo(item.screen) },
                    icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                    label = null,
                    alwaysShowLabel = false,
                    colors = NavigationBarItemDefaults.colors(
                      selectedIconColor = MaterialTheme.colorScheme.primary,
                      selectedTextColor = MaterialTheme.colorScheme.primary,
                      indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag(item.testTag)
                  )
                }
              }
            }
          ) { innerPadding ->
            Surface(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
              color = Color.Transparent
            ) {
              when (currentScreen) {
                AppScreen.Dashboard -> DashboardScreen(viewModel = viewModel)
                AppScreen.Products -> ProductsScreen(viewModel = viewModel)
                AppScreen.Customers -> CustomersScreen(viewModel = viewModel)
                AppScreen.NewSale -> NewSaleScreen(viewModel = viewModel)
                AppScreen.Transactions -> TransactionsScreen(viewModel = viewModel)
                AppScreen.Settings -> SettingsScreen(viewModel = viewModel)
              }
            }
          }
        }
      }
    }
  }
}

data class NavigationItem(
  val label: String,
  val screen: AppScreen,
  val icon: ImageVector,
  val testTag: String
)
