package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DatabaseBackup(
    val products: List<Product> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val saleItems: List<SaleItem> = emptyList(),
    val shopProfile: ShopProfile? = null
)
