package cl.gus.labs.fakestore.analytics.runtime

import cl.gus.labs.fakestore.analytics.AnalyticsEvent
import cl.gus.labs.fakestore.analytics.AnalyticsTracker
import cl.gus.labs.fakestore.analytics.Audience
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Routing is a single behaviour in a single class, so it gets a single focused test.
 * Before, this logic lived as an `if` at the top of every vendor and could only be tested
 * vendor by vendor.
 */
@DisplayName("AnalyticsDispatcher")
class AnalyticsDispatcherTest {

    private class SpyTracker(
        override val audiences: Set<Audience>,
    ) : AnalyticsTracker {
        val received = mutableListOf<AnalyticsEvent>()
        override fun track(event: AnalyticsEvent) { received += event }
    }

    private class TestEvent(override val audiences: Set<Audience>) : AnalyticsEvent {
        override val eventName = "Test Event"
    }

    @Test
    @DisplayName("delivers an event to trackers whose audiences intersect it")
    fun deliversOnIntersection() {
        val product = SpyTracker(setOf(Audience.Product))
        val marketing = SpyTracker(setOf(Audience.Marketing))
        val event = TestEvent(setOf(Audience.Product))

        AnalyticsDispatcher(setOf(product, marketing)).track(event)

        assertEquals(listOf(event), product.received)
        assertTrue(marketing.received.isEmpty())
    }

    @Test
    @DisplayName("delivers a multi-audience event to every matching tracker, once each")
    fun deliversToEveryMatch() {
        val product = SpyTracker(setOf(Audience.Product))
        val marketing = SpyTracker(setOf(Audience.Marketing))
        val event = TestEvent(setOf(Audience.Product, Audience.Marketing))

        AnalyticsDispatcher(setOf(product, marketing)).track(event)

        assertEquals(1, product.received.size)
        assertEquals(1, marketing.received.size)
    }

    @Test
    @DisplayName("delivers nothing when no audience matches")
    fun deliversNothingWithoutMatch() {
        val engineering = SpyTracker(setOf(Audience.Engineering))
        AnalyticsDispatcher(setOf(engineering)).track(TestEvent(setOf(Audience.Product)))

        assertTrue(engineering.received.isEmpty())
    }

    @Test
    @DisplayName("survives having no trackers at all")
    fun survivesEmptySet() {
        AnalyticsDispatcher(emptySet()).track(TestEvent(setOf(Audience.Product)))
    }
}
