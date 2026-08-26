package cl.gus.labs.fakestore.core.testing

import cl.gus.labs.fakestore.analytics.AnalyticsEvent
import cl.gus.labs.fakestore.analytics.AnalyticsTracker
import cl.gus.labs.fakestore.analytics.Audience

/**
 * A real tracker that remembers what it was given.
 *
 * This is a fake, not a mock, and that is the point: it needs no mocking library, works when
 * the class under test is final, and reads as ordinary Kotlin. It also lets a test assert on
 * *what was tracked* rather than on *which method was called*, which is the thing that
 * actually matters to analytics.
 */
class RecordingTracker(
    override val audiences: Set<Audience> = Audience.entries.toSet(),
) : AnalyticsTracker {

    private val recorded = mutableListOf<AnalyticsEvent>()

    val events: List<AnalyticsEvent> get() = recorded.toList()

    val last: AnalyticsEvent? get() = recorded.lastOrNull()

    val names: List<String> get() = recorded.map { it.eventName }

    override fun track(event: AnalyticsEvent) {
        recorded += event
    }

    fun clear() = recorded.clear()

    fun countOf(eventName: String): Int = recorded.count { it.eventName == eventName }
}
