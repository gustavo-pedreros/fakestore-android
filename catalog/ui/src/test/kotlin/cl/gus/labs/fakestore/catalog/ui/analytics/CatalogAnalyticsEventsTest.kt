package cl.gus.labs.fakestore.catalog.ui.analytics

import cl.gus.labs.fakestore.analytics.Audience
import cl.gus.labs.fakestore.core.testing.AnalyticsExtension
import cl.gus.labs.fakestore.core.testing.assertAudiences
import cl.gus.labs.fakestore.core.testing.assertName
import cl.gus.labs.fakestore.core.testing.assertNoProperties
import cl.gus.labs.fakestore.core.testing.assertNoProperty
import cl.gus.labs.fakestore.core.testing.assertProperties
import cl.gus.labs.fakestore.core.testing.assertProperty
import cl.gus.labs.fakestore.core.testing.assertTracked
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * Direct style. Prefer this unless the feature has many events with shared setup — then see
 * the robot templates.
 *
 * There is no mocking and no framework setup: an event is data, and the extension installs a
 * recording tracker for the duration of each test.
 */
@DisplayName("CatalogAnalyticsEvents")
class CatalogAnalyticsEventsTest {

    @JvmField
    @RegisterExtension
    val analytics = AnalyticsExtension()

    @Nested
    @DisplayName("ScreenAccessed")
    inner class ScreenAccessed {

        @Test
        @DisplayName("has the agreed name and no properties")
        fun definition() {
            val event = CatalogAnalyticsEvents.ScreenAccessed

            event.assertName("Catalog Accessed")
            event.assertAudiences(Audience.Product)
            event.assertNoProperties()
        }

        @Test
        @DisplayName("reaches the tracker exactly once")
        fun tracks() {
            CatalogAnalyticsEvents.ScreenAccessed.track()

            analytics.tracker.assertTracked("Catalog Accessed")
        }
    }

    @Nested
    @DisplayName("ItemSelected")
    inner class ItemSelected {

        @Test
        @DisplayName("carries the item id and position")
        fun definition() {
            val event = CatalogAnalyticsEvents.ItemSelected(itemId = "sku-42", position = 3)

            event.assertName("Catalog Item Selected")
            event.assertProperties(mapOf("item_id" to "sku-42", "position" to 3))
        }
    }

    @Nested
    @DisplayName("FlowCompleted")
    inner class FlowCompleted {

        @Test
        @DisplayName("includes the coupon code when there is one")
        fun withCoupon() {
            val event = CatalogAnalyticsEvents.FlowCompleted(
                method = CatalogAnalyticsEvents.Method.Card,
                couponCode = "SUMMER",
            )

            event.assertProperty("method", "card")
            event.assertProperty("coupon_code", "SUMMER")
        }

        @Test
        @DisplayName("omits the coupon key entirely when there is none")
        fun withoutCoupon() {
            val event = CatalogAnalyticsEvents.FlowCompleted(
                method = CatalogAnalyticsEvents.Method.Wallet,
            )

            // Absent, not null: a null would reach most vendors as the string "null".
            event.assertNoProperty("coupon_code")
        }

        @Test
        @DisplayName("is sent to both product and marketing")
        fun audiences() {
            CatalogAnalyticsEvents
                .FlowCompleted(CatalogAnalyticsEvents.Method.Card)
                .assertAudiences(Audience.Product, Audience.Marketing)
        }
    }
}
