package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val purchasePrice: Double, // ক্রয়মূল্য
    val sellingPrice: Double,  // বিক্রয়মূল্য
    val stock: Int             // বর্তমান স্টক
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,         // মোবাইল নম্বর
    val address: String        // ঠিকানা
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,      // ০ = খুচরা ক্রেতা (Walk-in)
    val customerName: String,  // ক্রেতার নাম
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,   // মোট টাকা
    val paidAmount: Double,    // পরিশোধিত টাকা
    val dueAmount: Double,     // বাকি টাকা
    val paymentType: String    // "Full Cash" (পুরো পরিশোধ), "Full Due" (পুরো বাকি), "Partial Paid" (আংশিক পরিশোধ)
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val purchasePrice: Double, // বিক্রয়ের সময় ক্রয়মূল্য (মুনাফা হিসাবের জন্য)
    val sellingPrice: Double   // বিক্রয়ের সময় বিক্রয়মূল্য
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "shop_profile")
data class ShopProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "আমার ডিজিটাল ব্যবসা খাতা",
    val phone: String = "01700000000",
    val address: String = "ঢাকা, বাংলাদেশ"
)

// UI-specific class representing a Sale with its detailed items
data class SaleWithItems(
    val sale: Sale,
    val items: List<SaleItem>
)
