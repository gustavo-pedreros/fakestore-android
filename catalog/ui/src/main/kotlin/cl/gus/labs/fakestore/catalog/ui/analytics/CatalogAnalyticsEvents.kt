package cl.gus.labs.fakestore.catalog.ui.analytics

import cl.gus.labs.fakestore.analytics.AnalyticsEvent
import cl.gus.labs.fakestore.analytics.Audience

/**
 * Analytics events for Catalog.
 *
 * One container per feature, colocated with the feature. There is deliberately no central
 * catalogue: a shared events file becomes a merge-conflict magnet and forces every feature
 * module to depend on every other feature's vocabulary.
 *
 * The container is `sealed` so the guardrails can enumerate its events, and so an exhaustive
 * `when` over them is possible where that helps.
 */
sealed class CatalogAnalyticsEvents : AnalyticsEvent {

    /** No properties: the simplest shape. */
    data object ScreenAccessed : CatalogAnalyticsEvents() {
        override val eventName = EventName.SCREEN_ACCESSED
        override val audiences = setOf(Audience.Product)
    }

    /** With properties. Note the constructor takes domain values, never a Context. */
    data class ItemSelected(
        private val itemId: String,
        private val position: Int,
    ) : CatalogAnalyticsEvents() {
        override val eventName = EventName.ITEM_SELECTED
        override val audiences = setOf(Audience.Product)
        override val properties = mapOf(
            EventProperty.ITEM_ID to itemId,
            EventProperty.POSITION to position,
        )
    }

    /**
     * An optional property. Build the map conditionally rather than sending a null: most
     * vendors record `"null"` as a literal string value, which quietly poisons the funnel.
     */
    data class FlowCompleted(
        private val method: Method,
        private val couponCode: String? = null,
    ) : CatalogAnalyticsEvents() {
        override val eventName = EventName.FLOW_COMPLETED
        override val audiences = setOf(Audience.Product, Audience.Marketing)
        override val properties = buildMap {
            put(EventProperty.METHOD, method.value)
            couponCode?.let { put(EventProperty.COUPON_CODE, it) }
        }
    }

    /** Event names: Title Case, verb last, readable by the analysts who query them. */
    object EventName {
        const val SCREEN_ACCESSED = "Catalog Accessed"
        const val ITEM_SELECTED = "Catalog Item Selected"
        const val FLOW_COMPLETED = "Catalog Flow Completed"
    }

    /** Property keys: snake_case constants. Never an inline literal — typos are unfindable. */
    object EventProperty {
        const val ITEM_ID = "item_id"
        const val POSITION = "position"
        const val METHOD = "method"
        const val COUPON_CODE = "coupon_code"
    }

    /** Closed sets of values belong in an enum, so a typo is a compile error. */
    enum class Method(val value: String) {
        Card("card"),
        Wallet("wallet"),
        Transfer("transfer"),
    }
}
