package cl.gus.labs.fakestore.analytics

/**
 * The tracker installed before [Analytics.install] runs, and after [Analytics.reset].
 *
 * Its purpose is that analytics never crashes and never needs a null check: code that tracks
 * during early startup, in a unit test, or in a build with no vendors simply does nothing.
 */
object NoOpTracker : AnalyticsTracker {
    override val audiences: Set<Audience> = emptySet()
    override fun track(event: AnalyticsEvent) = Unit
}
