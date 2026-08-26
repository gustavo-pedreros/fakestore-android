package cl.gus.labs.fakestore.core.testing

import cl.gus.labs.fakestore.analytics.AnalyticsEvent
import cl.gus.labs.fakestore.analytics.Audience

/**
 * Base for the result half of the robot pattern: the *assert* phase.
 *
 * Subclasses add one `assertXxx()` per event, describing that event's full contract in one
 * place — name, audiences, properties. When an event's shape changes, exactly one assertion
 * method changes.
 */
abstract class EventResult<E : AnalyticsEvent>(
    protected val tracker: RecordingTracker,
    protected val event: E,
) {
    fun assertTracked(times: Int = 1) = tracker.assertTracked(event.eventName, times)

    fun assertName(expected: String) = event.assertName(expected)

    fun assertAudiences(vararg expected: Audience) = event.assertAudiences(*expected)

    fun assertProperty(key: String, expected: Any) = event.assertProperty(key, expected)

    fun assertNoProperty(key: String) = event.assertNoProperty(key)

    fun assertProperties(expected: Map<String, Any>) = event.assertProperties(expected)

    fun assertNoProperties() = event.assertNoProperties()
}
