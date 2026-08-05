package com.raffastudioproducoes.minharota.domain.profile

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileFieldPolicyTest {

    @Test
    fun `profile updates reject subscription and billing fields`() {
        val requested = mapOf<String, Any>(
            "name" to "Rafa",
            "isPro" to true,
            "nomePlano" to "Pro",
            "dataVencimento" to "2099-01-01"
        )

        assertEquals(mapOf<String, Any>("name" to "Rafa"), ProfileFieldPolicy.sanitize(requested))
    }
}
