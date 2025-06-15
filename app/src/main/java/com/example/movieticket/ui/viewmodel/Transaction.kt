package com.example.movieticket.ui.viewmodel

data class Transaction(
    val id: String = "",
    val amount: Long = 0,
    val type: String = TransactionType.DEBIT.name,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = "",
    val balanceAfter: Long? = null
)

enum class TransactionType {
    CREDIT,  // Nạp tiền
    DEBIT    // Trừ tiền
} 