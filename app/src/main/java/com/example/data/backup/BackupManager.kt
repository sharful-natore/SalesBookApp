package com.example.data.backup

import android.content.Context
import com.example.data.model.DatabaseBackup
import com.example.data.repository.SalesBookRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first

class BackupManager(
    private val repository: SalesBookRepository,
    private val context: Context
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(DatabaseBackup::class.java)

    // Export database snapshot to formatted JSON string
    suspend fun exportToJson(): String {
        val products = repository.allProducts.first()
        val customers = repository.allCustomers.first()
        val sales = repository.allSales.first()
        val saleItems = repository.allSaleItems.first()
        val shopProfile = repository.shopProfile.first()

        val backup = DatabaseBackup(
            products = products,
            customers = customers,
            sales = sales,
            saleItems = saleItems,
            shopProfile = shopProfile
        )
        return adapter.indent("  ").toJson(backup)
    }

    // Restore database snapshot from JSON string
    suspend fun restoreFromJson(jsonString: String): Boolean {
        return try {
            val backup = adapter.fromJson(jsonString) ?: return false
            repository.restoreBackup(backup)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
