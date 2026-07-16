package com.raffastudioproducoes.minharota.domain.model

data class Category(
    val name: String = "",
    val icon: String = "",
    val isDefault: Boolean = true,
    val userId: String? = null // Nulo se for uma categoria padrão do sistema
)
