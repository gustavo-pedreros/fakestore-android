package cl.gus.labs.fakestore.analytics.runtime

import cl.gus.labs.fakestore.analytics.AnalyticsEvent
import cl.gus.labs.fakestore.analytics.AnalyticsTracker
import cl.gus.labs.fakestore.analytics.Audience

/**
 * Fans one event out to every tracker that serves one of its audiences.
 *
 * This is the only place routing happens. Vendors are mappers; they do not filter.
 *
 * It takes a `Set` rather than a `List` because registration order is not meaningful and a
 * set makes double-registration impossible.
 *
 * Note it implements [AnalyticsTracker] but must never be registered *into* [trackers] —
 * see the DI notes in `references/di-hilt-koin.md`.
 */
class AnalyticsDispatcher(
    private val trackers: Set<AnalyticsTracker>,
) : AnalyticsTracker {

    override val audiences: Set<Audience> =
        trackers.flatMapTo(mutableSetOf()) { it.audiences }

    override fun track(event: AnalyticsEvent) {
        trackers.forEach { tracker ->
            if (tracker.audiences.any(event.audiences::contains)) {
                tracker.track(event)
            }
        }
    }
}
