package cl.gus.labs.fakestore.analytics.tool.logcat

import cl.gus.labs.fakestore.analytics.AnalyticsEvent
import cl.gus.labs.fakestore.analytics.Audience
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("LogcatTracker")
class LogcatTrackerTest {

    private class Recorded(val name: String, val properties: Map<String, String>)

    private class FakeLogcatClient : LogcatClient {
        val events = mutableListOf<Recorded>()
        var userId: String? = null
        val userProperties = mutableMapOf<String, String>()
        var resetCount = 0

        override fun logEvent(name: String, properties: Map<String, String>) {
            events += Recorded(name, properties)
        }
        override fun setUserId(userId: String) { this.userId = userId }
        override fun setUserProperty(key: String, value: String) { userProperties[key] = value }
        override fun reset() { resetCount++ }
    }

    private class TestEvent(
        override val eventName: String,
        override val properties: Map<String, Any> = emptyMap(),
    ) : AnalyticsEvent {
        override val audiences = setOf(Audience.Product)
    }

    private val client = FakeLogcatClient()
    private val tracker = LogcatTracker(client)

    @Test
    @DisplayName("normalises the event name for the SDK")
    fun normalisesName() {
        tracker.track(TestEvent("Checkout Flow Started"))

        assertEquals("checkout_flow_started", client.events.single().name)
    }

    @Test
    @DisplayName("stringifies property values without dropping keys")
    fun stringifiesProperties() {
        tracker.track(TestEvent("Cart Updated", mapOf("item_count" to 3, "is_gift" to true)))

        assertEquals(
            mapOf("item_count" to "3", "is_gift" to "true"),
            client.events.single().properties,
        )
    }

    @Test
    @DisplayName("sends an event with no properties as an empty map, not null")
    fun handlesNoProperties() {
        tracker.track(TestEvent("App Opened"))

        assertEquals(emptyMap<String, String>(), client.events.single().properties)
    }
}
