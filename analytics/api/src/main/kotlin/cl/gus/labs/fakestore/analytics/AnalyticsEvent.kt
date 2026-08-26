package cl.gus.labs.fakestore.analytics

/**
 * A single analytics event.
 *
 * Implementations are plain data. They must not hold a `Context`, a DI component, a tracker,
 * or any Android type — an event that needs the framework to describe itself cannot be
 * asserted on in a unit test without static mocking, and can only be created where the
 * framework is available, which in practice means the UI layer.
 */
interface AnalyticsEvent {

    /** Human-readable name, Title Case, verb last. E.g. "Checkout Flow Started". */
    val eventName: String

    /** Which audiences this event is for. Must not be empty. */
    val audiences: Set<Audience>

    /**
     * Event properties. Keys are `snake_case` constants, never inline literals.
     * Values keep their real types; each vendor converts as its SDK requires.
     */
    val properties: Map<String, Any> get() = emptyMap()

    /** Sends this event through the installed tracker. */
    fun track() = Analytics.track(this)
}
