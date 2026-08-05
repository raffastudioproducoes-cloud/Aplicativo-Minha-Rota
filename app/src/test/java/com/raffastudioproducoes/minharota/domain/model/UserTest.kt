package com.raffastudioproducoes.minharota.domain.model

import org.junit.Assert.assertFalse
import org.junit.Test

class UserTest {

    @Test
    fun `profile payload cannot carry subscription entitlement`() {
        val profileFields = User::class.java.declaredFields.map { it.name }.toSet()

        assertFalse("Profile must not contain isPro", "isPro" in profileFields)
        assertFalse("Profile must not contain plan name", "nomePlano" in profileFields)
        assertFalse("Profile must not contain expiration", "dataVencimento" in profileFields)
    }
}
