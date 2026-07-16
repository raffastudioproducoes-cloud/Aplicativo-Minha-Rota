package com.raffastudioproducoes.minharota.domain.model

data class Account(
    val name: String = "",
    val type: String = "", // "Conta Corrente", "Cartão", etc.
    val balance: Double = 0.0
)
