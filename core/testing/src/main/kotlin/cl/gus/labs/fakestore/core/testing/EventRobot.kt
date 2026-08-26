package cl.gus.labs.fakestore.core.testing

import cl.gus.labs.fakestore.analytics.AnalyticsEvent

/**
 * Base for the robot half of the robot pattern: the *act* phase.
 *
 * A robot exposes one method per thing a user can do, builds the corresponding event and
 * tracks it. The test then reads as a sentence and never mentions how an event is constructed
 * — so when a constructor changes, one robot method changes instead of thirty tests.
 *
 * Subclasses add `trackXxx()` methods and nothing else. Assertions belong in [EventResult].
 */
abstract class EventRobot<E : AnalyticsEvent, R : EventResult<E>>(
    protected val tracker: RecordingTracker,
) {

    private var tracked: E? = null

    /** Records the event as the subject of the following assertions, then tracks it. */
    protected fun <T : E> track(event: T): T {
        tracked = event
        event.track()
        return event
    }

    protected val event: E
        get() = tracked ?: error(
            "No event tracked yet — call one of this robot's track... methods before `then`.",
        )

    protected abstract fun results(tracker: RecordingTracker, event: E): R

    /** Bridges act to assert. Returns Unit so a @Test method can use expression body. */
    infix fun then(assertions: R.() -> Unit) {
        results(tracker, event).assertions()
    }
}
