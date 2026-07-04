package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SalesBookRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    Dashboard, Products, Customers, NewSale, Transactions, Settings
}

enum class ReportFilter {
    Today, CurrentMonth, Custom
}

data class CartItem(
    val product: Product,
    val quantity: Int
)

class SalesBookViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = SalesBookRepository(db)
    private val backupManager = BackupManager(repository, application)

    private val prefs = application.getSharedPreferences("sales_book_prefs", Context.MODE_PRIVATE)

    // Screen navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.Dashboard)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // App Theme State (Dark Mode togglable in settings and persisted)
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        prefs.edit().putBoolean("is_dark_mode", newValue).apply()
    }

    // Language State (Bengali vs. English)
    private val _isEnglish = MutableStateFlow(prefs.getBoolean("is_english", false))
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    fun toggleLanguage() {
        val newValue = !_isEnglish.value
        _isEnglish.value = newValue
        prefs.edit().putBoolean("is_english", newValue).apply()
    }

    // Products Flow
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Customers Flow
    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sales Flow
    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Sale Items Flow
    val allSaleItems: StateFlow<List<SaleItem>> = repository.allSaleItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shop Profile Flow
    val shopProfile: StateFlow<ShopProfile> = repository.shopProfile
        .map { it ?: ShopProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopProfile())

    // ==========================================
    // NEW SALE & DYNAMIC CART SYSTEM STATE
    // ==========================================
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null) // null represents Walk-in (খুচরা ক্রেতা)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    val paidAmountInput = MutableStateFlow("")
    val paymentType = MutableStateFlow("Full Cash") // "Full Cash", "Full Due", "Partial Paid"

    // Cart Operations
    fun addToCart(product: Product) {
        val currentList = _cartItems.value
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
        if (existingIndex != -1) {
            val existing = currentList[existingIndex]
            if (existing.quantity < product.stock) {
                val updated = currentList.toMutableList()
                updated[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                _cartItems.value = updated
            }
        } else {
            if (product.stock > 0) {
                _cartItems.value = currentList + CartItem(product, 1)
            }
        }
        recalculatePayment()
    }

    fun updateCartQuantity(product: Product, quantity: Int) {
        val currentList = _cartItems.value
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id }
        if (existingIndex != -1) {
            val updated = currentList.toMutableList()
            if (quantity <= 0) {
                updated.removeAt(existingIndex)
            } else {
                val finalQty = quantity.coerceAtMost(product.stock)
                updated[existingIndex] = updated[existingIndex].copy(quantity = finalQty)
            }
            _cartItems.value = updated
        }
        recalculatePayment()
    }

    fun removeFromCart(product: Product) {
        _cartItems.value = _cartItems.value.filter { it.product.id != product.id }
        recalculatePayment()
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _selectedCustomer.value = null
        paidAmountInput.value = ""
        paymentType.value = "Full Cash"
    }

    fun getTotalCartAmount(): Double {
        return _cartItems.value.sumOf { it.product.sellingPrice * it.quantity }
    }

    private fun recalculatePayment() {
        val total = getTotalCartAmount()
        when (paymentType.value) {
            "Full Cash" -> {
                paidAmountInput.value = if (total > 0) total.toString() else ""
            }
            "Full Due" -> {
                paidAmountInput.value = "0"
            }
            "Partial Paid" -> {
                // Keep current input as is
            }
        }
    }

    fun setPaymentStatus(status: String) {
        paymentType.value = status
        recalculatePayment()
    }

    fun setPaidAmount(amountStr: String) {
        paidAmountInput.value = amountStr
        val total = getTotalCartAmount()
        val paid = amountStr.toDoubleOrNull() ?: 0.0
        
        when {
            paid >= total && total > 0 -> {
                paymentType.value = "Full Cash"
            }
            paid <= 0 -> {
                paymentType.value = "Full Due"
            }
            else -> {
                paymentType.value = "Partial Paid"
            }
        }
    }

    // Submit sale to database
    fun checkout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cart = _cartItems.value
        if (cart.isEmpty()) {
            onError("কার্ট খালি! পণ্য যোগ করুন।")
            return
        }

        val total = getTotalCartAmount()
        val paid = paidAmountInput.value.toDoubleOrNull() ?: 0.0
        val due = (total - paid).coerceAtLeast(0.0)

        // Validate stock levels
        for (item in cart) {
            if (item.quantity > item.product.stock) {
                onError("${item.product.name} এর পর্যাপ্ত স্টক নেই!")
                return
            }
        }

        val finalPaymentType = when {
            paid >= total -> "Full Cash"
            paid <= 0 -> "Full Due"
            else -> "Partial Paid"
        }

        val customer = _selectedCustomer.value
        val customerId = customer?.id ?: 0L
        val customerName = customer?.name ?: "খুচরা ক্রেতা"

        viewModelScope.launch {
            try {
                val sale = Sale(
                    customerId = customerId,
                    customerName = customerName,
                    totalAmount = total,
                    paidAmount = paid,
                    dueAmount = due,
                    paymentType = finalPaymentType
                )

                val saleItems = cart.map {
                    SaleItem(
                        saleId = 0L, // dynamic ID handled inside transaction block
                        productId = it.product.id,
                        productName = it.product.name,
                        quantity = it.quantity,
                        purchasePrice = it.product.purchasePrice,
                        sellingPrice = it.product.sellingPrice
                    )
                }

                repository.createSale(sale, saleItems)
                clearCart()
                onSuccess()
            } catch (e: Exception) {
                onError("লেনদেন সংরক্ষণ করতে সমস্যা হয়েছে: ${e.message}")
            }
        }
    }

    // Product CRUD operations
    fun addProduct(name: String, purchasePrice: Double, sellingPrice: Double, stock: Int, unit: String = "Pcs", onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertProduct(Product(name = name, purchasePrice = purchasePrice, sellingPrice = sellingPrice, stock = stock, unit = unit))
            onComplete()
        }
    }

    fun updateProduct(product: Product, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateProduct(product)
            onComplete()
        }
    }

    fun deleteProduct(product: Product, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            onComplete()
        }
    }

    // Customer CRUD operations
    fun addCustomer(name: String, phone: String, address: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(name = name, phone = phone, address = address))
            onComplete()
        }
    }

    fun updateCustomer(customer: Customer, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
            onComplete()
        }
    }

    fun deleteCustomer(customer: Customer, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            onComplete()
        }
    }

    // Shop settings operation
    fun updateShopSettings(name: String, phone: String, address: String, logoUri: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.insertOrUpdateShopProfile(ShopProfile(name = name, phone = phone, address = address, logoUri = logoUri))
            onComplete()
        }
    }

    // Delete sale
    fun deleteSaleTransaction(saleId: Long) {
        viewModelScope.launch {
            repository.deleteSale(saleId)
        }
    }

    // Collect outstanding due for a specific sale (Ledger screen)
    fun collectSaleDue(saleId: Long, amount: Double) {
        viewModelScope.launch {
            val sale = sales.value.find { it.id == saleId } ?: return@launch
            val newDue = (sale.dueAmount - amount).coerceAtLeast(0.0)
            val newPaid = sale.paidAmount + (sale.dueAmount - newDue)
            val newPaymentType = when {
                newDue <= 0 -> "Full Cash"
                else -> "Partial Paid"
            }
            val updatedSale = sale.copy(
                paidAmount = newPaid,
                dueAmount = newDue,
                paymentType = newPaymentType
            )
            repository.updateSale(updatedSale)
        }
    }

    // Collect outstanding dues from a customer (distribute to their oldest sales with dues)
    fun collectCustomerDue(customerId: Long, amount: Double) {
        viewModelScope.launch {
            var remainingAmount = amount
            val salesWithDue = sales.value.filter { it.customerId == customerId && it.dueAmount > 0 }
                .sortedBy { it.timestamp } // oldest first

            for (sale in salesWithDue) {
                if (remainingAmount <= 0) break

                val due = sale.dueAmount
                val payToThisSale = minOf(remainingAmount, due)

                val newDue = due - payToThisSale
                val updatedSale = sale.copy(
                    paidAmount = sale.paidAmount + payToThisSale,
                    dueAmount = newDue,
                    paymentType = if (newDue <= 0) "Full Cash" else "Partial Paid"
                )
                repository.updateSale(updatedSale)
                remainingAmount -= payToThisSale
            }
        }
    }

    // Date Filtering for reports
    val reportFilter = MutableStateFlow(ReportFilter.Today)
    val customStartDate = MutableStateFlow<Long?>(null)
    val customEndDate = MutableStateFlow<Long?>(null)

    fun setReportFilter(filter: ReportFilter) {
        reportFilter.value = filter
    }

    fun setCustomDateRange(start: Long, end: Long) {
        customStartDate.value = start
        customEndDate.value = end
        reportFilter.value = ReportFilter.Custom
    }

    fun setCustomStartDate(start: Long) {
        customStartDate.value = start
    }

    fun setCustomEndDate(end: Long) {
        customEndDate.value = end
    }

    // Filtering logic helper
    fun isTimestampInFilter(timestamp: Long): Boolean {
        val filter = reportFilter.value
        val start = customStartDate.value
        val end = customEndDate.value
        
        val cal = Calendar.getInstance()
        val saleCal = Calendar.getInstance()
        saleCal.timeInMillis = timestamp

        return when (filter) {
            ReportFilter.Today -> {
                cal.get(Calendar.YEAR) == saleCal.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == saleCal.get(Calendar.DAY_OF_YEAR)
            }
            ReportFilter.CurrentMonth -> {
                cal.get(Calendar.YEAR) == saleCal.get(Calendar.YEAR) &&
                cal.get(Calendar.MONTH) == saleCal.get(Calendar.MONTH)
            }
            ReportFilter.Custom -> {
                if (start != null && end != null) {
                    timestamp in start..end
                } else {
                    true
                }
            }
        }
    }

    // BACKUP & RESTORE ACTIONS
    fun exportBackup(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val json = backupManager.exportToJson()
            onComplete(json)
        }
    }

    fun restoreBackup(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupManager.restoreFromJson(jsonString)
            onComplete(success)
        }
    }
}
