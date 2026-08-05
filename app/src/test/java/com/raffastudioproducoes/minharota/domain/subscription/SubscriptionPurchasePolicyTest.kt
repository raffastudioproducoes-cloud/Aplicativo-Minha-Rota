package com.raffastudioproducoes.minharota.domain.subscription

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SubscriptionPurchasePolicyTest {

    @Test
    fun `paid plans stay visible but purchasing is unavailable until billing is implemented`() {
        val offer = SubscriptionPurchasePolicy.currentOffer

        assertFalse(offer.purchaseEnabled)
        assertEquals("Assinaturas em breve", offer.actionLabel)
    }

    @Test
    fun `client cannot grant an unverified paid entitlement`() {
        assertFalse(SubscriptionPurchasePolicy.hasVerifiedPaidEntitlement())
    }
}
