package com.autoledger.app.data

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: String,          // "expense" | "income"
    val merchant: String,
    val category: String,      // category id
    val payMethod: String,
    val time: Long,            // epoch millis
    val note: String,
    val source: String,        // "noti" | "manual"
    val raw: String
)
