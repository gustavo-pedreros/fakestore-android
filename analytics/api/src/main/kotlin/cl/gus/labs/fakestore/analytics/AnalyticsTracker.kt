package cl.gus.labs.fakestore.analytics

/**
 * The contract every analytics vendor implements.
 *
 * A tracker is a mapper: it turns an [AnalyticsEvent] into a call on some SDK. It does not
 * decide whether it should have received the event — [audiences] declares what it serves and
 * the dispatcher does the routing.
 */
interface AnalyticsTracker {

    /** Audiences this tracker serves. Defaults to all of them. */
    val audiences: Set<Audience> get() = Audience.entries.toSet()

    fun track(event: AnalyticsEvent)
}
