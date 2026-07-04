package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.database.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class SalesBookRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val customerDao = db.customerDao()
    private val saleDao = db.saleDao()
    private val shopProfileDao = db.shopProfileDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val allSaleItems: Flow<List<SaleItem>> = saleDao.getAllSaleItems()
    val shopProfile: Flow<ShopProfile?> = shopProfileDao.getShopProfile()

    // Products operations
    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    // Customers operations
    suspend fun insertCustomer(customer: Customer) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)
    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    // Shop Profile operations
    suspend fun insertOrUpdateShopProfile(profile: ShopProfile) = shopProfileDao.insertOrUpdateShopProfile(profile)

    // New Sale Transaction with automated stock level adjustment
    suspend fun createSale(sale: Sale, items: List<SaleItem>) {
        db.withTransaction {
            // 1. Insert sale master record
            val saleId = saleDao.insertSale(sale)
            
            // 2. Insert items and adjust corresponding inventory stock
            for (item in items) {
                val itemWithSaleId = item.copy(saleId = saleId)
                saleDao.insertSaleItem(itemWithSaleId)
                
                // Adjust product stock
                val product = productDao.getProductById(item.productId)
                if (product != null) {
                    val updatedStock = (product.stock - item.quantity).coerceAtLeast(0)
                    productDao.updateProduct(product.copy(stock = updatedStock))
                }
            }
        }
    }

    // Delete sale transaction and restore product stock levels
    suspend fun deleteSale(saleId: Long) {
        db.withTransaction {
            // Fetch sale items to restore product stocks
            val items = saleDao.getSaleItemsBySaleId(saleId)
            for (item in items) {
                val product = productDao.getProductById(item.productId)
                if (product != null) {
                    val restoredStock = product.stock + item.quantity
                    productDao.updateProduct(product.copy(stock = restoredStock))
                }
            }
            // Delete related items and sale itself
            saleDao.deleteSaleItemsBySaleId(saleId)
            saleDao.deleteSaleById(saleId)
        }
    }

    // Fetch sale items by Sale ID
    suspend fun getSaleItems(saleId: Long): List<SaleItem> {
        return saleDao.getSaleItemsBySaleId(saleId)
    }

    // Restore entire database from a backup
    suspend fun restoreBackup(backup: DatabaseBackup) {
        db.withTransaction {
            // 1. Wipe all tables
            productDao.clearProducts()
            customerDao.clearCustomers()
            saleDao.clearSales()
            saleDao.clearSaleItems()
            shopProfileDao.clearShopProfile()

            // 2. Insert all products
            for (product in backup.products) {
                productDao.insertProduct(product)
            }

            // 3. Insert all customers
            for (customer in backup.customers) {
                customerDao.insertCustomer(customer)
            }

            // 4. Insert all sales
            for (sale in backup.sales) {
                saleDao.insertSale(sale)
            }

            // 5. Insert all sale items
            for (item in backup.saleItems) {
                saleDao.insertSaleItem(item)
            }

            // 6. Insert shop profile
            if (backup.shopProfile != null) {
                shopProfileDao.insertOrUpdateShopProfile(backup.shopProfile)
            }
        }
    }
}
