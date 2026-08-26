package cl.gus.labs.fakestore.catalog.ui.analytics

import cl.gus.labs.fakestore.core.testing.AnalyticsExtension
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * Robot style. Worth the three files when a feature has many events, or when the same event
 * is asserted from several tests — the payoff is that a constructor change touches the robot
 * and a contract change touches the result class, never the tests.
 *
 * For a feature with two or three simple events, the direct style is less machinery for the
 * same coverage.
 */
@DisplayName("CatalogAnalyticsEvents")
class CatalogAnalyticsEventsRobotTest {

    @JvmField
    @RegisterExtension
    val analytics = AnalyticsExtension()

    private fun withRobot(block: CatalogAnalyticsEventsRobot.() -> Unit) =
        CatalogAnalyticsEventsRobot(analytics.tracker).apply(block)

    @Test
    @DisplayName("when the screen is accessed, the access event is tracked")
    fun screenAccessed() = withRobot {
        screenIsAccessed()
    } then {
        isScreenAccessed()
        assertTracked()
    }

    @Test
    @DisplayName("when an item is selected, its id and position are tracked")
    fun itemSelected() = withRobot {
        itemIsSelected(itemId = "sku-42", position = 3)
    } then {
        isItemSelected(itemId = "sku-42", position = 3)
        assertTracked()
    }

    @Nested
    @DisplayName("when the flow completes")
    inner class FlowCompleted {

        @Test
        @DisplayName("with a coupon, the coupon code is included")
        fun withCoupon() = withRobot {
            flowIsCompleted(CatalogAnalyticsEvents.Method.Card, couponCode = "SUMMER")
        } then {
            isFlowCompleted(method = "card", couponCode = "SUMMER")
            assertTracked()
        }

        @Test
        @DisplayName("without a coupon, the key is absent rather than null")
        fun withoutCoupon() = withRobot {
            flowIsCompleted(CatalogAnalyticsEvents.Method.Wallet)
        } then {
            isFlowCompleted(method = "wallet")
            assertTracked()
        }
    }
}
