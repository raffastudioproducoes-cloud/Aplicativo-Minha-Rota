package com.raffastudioproducoes.minharota.domain.model

data class Vehicle(
    val userId: String = "",
    val name: String = "", // Ex: "Carro", "Moto"
    val model: String = "",
    val licensePlate: String = ""
)
