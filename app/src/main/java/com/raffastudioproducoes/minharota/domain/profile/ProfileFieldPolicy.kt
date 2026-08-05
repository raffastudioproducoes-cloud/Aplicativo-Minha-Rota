package com.raffastudioproducoes.minharota.domain.profile

object ProfileFieldPolicy {
    private val allowedFields = setOf(
        "name",
        "email",
        "cpf",
        "dataAniversario",
        "photoUrl"
    )

    fun sanitize(fields: Map<String, Any>): Map<String, Any> =
        fields.filterKeys { it in allowedFields }
}
