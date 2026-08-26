package cl.gus.labs.fakestore.analytics

/**
 * Holds the tracker that [AnalyticsEvent.track] uses.
 *
 * This exists so an event can be tracked from anywhere — a ViewModel, a use case, a
 * repository — without carrying a `Context` or a DI handle around just to reach the graph.
 * The composition root installs the real dispatcher once, at startup:
 *
 * ```
 * // Application.onCreate()
 * Analytics.install(dispatcher)
 * ```
 *
 * It is a mutable global, with the costs that implies: the tracker cannot be scoped per
 * feature or per user, and tests must install and reset around each case. Use the JUnit
 * extension in `:analytics:testing` rather than calling [install] by hand.
 *
 * If you need a scoped or injected tracker in some part of the codebase, depend on
 * [AnalyticsTracker] by constructor there. Both styles coexist: this object is a default,
 * not a mandate.
 */
object Analytics {

    @Volatile
    private var tracker: AnalyticsTracker = NoOpTracker

    /** Installs the tracker all events will use. Call once, from the composition root. */
    fun install(tracker: AnalyticsTracker) {
        this.tracker = tracker
    }

    fun track(event: AnalyticsEvent) {
        tracker.track(event)
    }

    /** Restores [NoOpTracker]. For tests; do not call in production code. */
    fun reset() {
        tracker = NoOpTracker
    }
}
