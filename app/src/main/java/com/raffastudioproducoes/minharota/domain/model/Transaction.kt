package com.raffastudioproducoes.minharota.domain.model

data class Transaction(
    val userId: String = "",
    val accountId: String = "",
    val categoryId: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val type: String = "", // "EXPENSE" ou "INCOME"
    val timestamp: Long = System.currentTimeMillis()
)
