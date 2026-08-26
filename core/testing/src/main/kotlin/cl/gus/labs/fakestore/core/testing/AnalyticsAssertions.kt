package cl.gus.labs.fakestore.core.testing

import cl.gus.labs.fakestore.analytics.AnalyticsEvent
import cl.gus.labs.fakestore.analytics.Audience
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail

/**
 * Assertions phrased in the language of analytics, so failures read as what went wrong
 * rather than as a map comparison.
 */

fun RecordingTracker.assertTracked(eventName: String, times: Int = 1) {
    val actual = countOf(eventName)
    if (actual != times) {
        fail<Unit>(
            "Expected '$eventName' to be tracked $times time(s) but it was tracked $actual. " +
                "Tracked events: $names",
        )
    }
}

fun RecordingTracker.assertNothingTracked() {
    assertTrue(events.isEmpty()) { "Expected no events but tracked: $names" }
}

fun AnalyticsEvent.assertName(expected: String) = assertEquals(expected, eventName)

fun AnalyticsEvent.assertAudiences(vararg expected: Audience) =
    assertEquals(expected.toSet(), audiences)

fun AnalyticsEvent.assertProperty(key: String, expected: Any) {
    if (!properties.containsKey(key)) {
        fail<Unit>("Event '$eventName' has no property '$key'. Present: ${properties.keys}")
    }
    assertEquals(expected, properties[key])
}

fun AnalyticsEvent.assertNoProperty(key: String) =
    assertTrue(!properties.containsKey(key)) {
        "Event '$eventName' should not carry '$key' but it does: ${properties[key]}"
    }

fun AnalyticsEvent.assertProperties(expected: Map<String, Any>) =
    assertEquals(expected, properties)

fun AnalyticsEvent.assertNoProperties() =
    assertTrue(properties.isEmpty()) { "Event '$eventName' should carry no properties: $properties" }
