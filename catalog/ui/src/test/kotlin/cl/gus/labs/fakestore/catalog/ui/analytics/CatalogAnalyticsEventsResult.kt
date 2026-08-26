package cl.gus.labs.fakestore.catalog.ui.analytics

import cl.gus.labs.fakestore.analytics.Audience
import cl.gus.labs.fakestore.core.testing.EventResult
import cl.gus.labs.fakestore.core.testing.RecordingTracker

/**
 * Assert phase for Catalog: one method per event, each stating that event's whole
 * contract. This is the file to open when someone asks "what exactly does this event send?".
 */
class CatalogAnalyticsEventsResult(
    tracker: RecordingTracker,
    event: CatalogAnalyticsEvents,
) : EventResult<CatalogAnalyticsEvents>(tracker, event) {

    fun isScreenAccessed() {
        assertName("Catalog Accessed")
        assertAudiences(Audience.Product)
        assertNoProperties()
    }

    fun isItemSelected(itemId: String, position: Int) {
        assertName("Catalog Item Selected")
        assertAudiences(Audience.Product)
        assertProperties(mapOf("item_id" to itemId, "position" to position))
    }

    fun isFlowCompleted(method: String, couponCode: String? = null) {
        assertName("Catalog Flow Completed")
        assertAudiences(Audience.Product, Audience.Marketing)
        assertProperty("method", method)
        if (couponCode == null) assertNoProperty("coupon_code")
        else assertProperty("coupon_code", couponCode)
    }
}
