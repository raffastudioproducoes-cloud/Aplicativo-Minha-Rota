package com.raffastudioproducoes.minharota.domain.subscription

data class SubscriptionOfferState(
    val purchaseEnabled: Boolean,
    val actionLabel: String
)

/**
 * Containment policy used until store billing and server-side entitlement
 * verification are available. No client-side code may grant a paid plan.
 */
object SubscriptionPurchasePolicy {
    val currentOffer = SubscriptionOfferState(
        purchaseEnabled = false,
        actionLabel = "Assinaturas em breve"
    )

    const val FREE_PLAN = "free"

    fun hasVerifiedPaidEntitlement(): Boolean = false
}
