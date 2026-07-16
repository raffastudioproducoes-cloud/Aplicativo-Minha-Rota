package com.raffastudioproducoes.minharota.domain.model

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val isPro: Boolean = false, // Essencial para liberar funções do menu
    val createdAt: Long = System.currentTimeMillis()
)
