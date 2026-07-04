package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
      val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
      val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()

      MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            CenterAlignedTopAppBar(
              title = {
                Text(
                  text = when (currentScreen) {
                    AppScreen.Dashboard -> shopProfile.name
                    AppScreen.Products -> "পণ্য ও ইনভেন্টরি"
                    AppScreen.Customers -> "ক্রেতা খাতা"
                    AppScreen.NewSale -> "নতুন বেচাকেনা হিসাব"
                    AppScreen.Transactions -> "বেচাকেনার লেজার"
                    AppScreen.Settings -> "সেটিংস ও ব্যাকআপ"
                  },
                  style = MaterialTheme.typography.titleMedium
                )
              },
              colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
              )
            )
          },
          bottomBar = {
            NavigationBar(
              modifier = Modifier.testTag("app_navigation_bar")
            ) {
              val navItems = listOf(
                NavigationItem("ড্যাশবোর্ড", AppScreen.Dashboard, Icons.Default.Dashboard, "nav_dashboard"),
                NavigationItem("বেচাকেনা", AppScreen.NewSale, Icons.Default.AddShoppingCart, "nav_new_sale"),
                NavigationItem("লেজার", AppScreen.Transactions, Icons.Default.HistoryEdu, "nav_transactions"),
                NavigationItem("পণ্য", AppScreen.Products, Icons.Default.Inventory2, "nav_products"),
                NavigationItem("ক্রেতা", AppScreen.Customers, Icons.Default.People, "nav_customers"),
                NavigationItem("সেটিংস", AppScreen.Settings, Icons.Default.Settings, "nav_settings")
              )

              navItems.forEach { item ->
                NavigationBarItem(
                  selected = currentScreen == item.screen,
                  onClick = { viewModel.navigateTo(item.screen) },
                  icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                  label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
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
            color = MaterialTheme.colorScheme.background
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

data class NavigationItem(
  val label: String,
  val screen: AppScreen,
  val icon: ImageVector,
  val testTag: String
)
