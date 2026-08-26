package cl.gus.labs.fakestore.catalog.ui.analytics

import cl.gus.labs.fakestore.core.testing.EventRobot
import cl.gus.labs.fakestore.core.testing.RecordingTracker

/**
 * Act phase for Catalog: one method per thing that happens, each building and tracking
 * its event. No assertions here.
 */
class CatalogAnalyticsEventsRobot(
    tracker: RecordingTracker,
) : EventRobot<CatalogAnalyticsEvents, CatalogAnalyticsEventsResult>(tracker) {

    fun screenIsAccessed() =
        track(CatalogAnalyticsEvents.ScreenAccessed)

    fun itemIsSelected(itemId: String, position: Int) =
        track(CatalogAnalyticsEvents.ItemSelected(itemId, position))

    fun flowIsCompleted(
        method: CatalogAnalyticsEvents.Method,
        couponCode: String? = null,
    ) = track(CatalogAnalyticsEvents.FlowCompleted(method, couponCode))

    override fun results(
        tracker: RecordingTracker,
        event: CatalogAnalyticsEvents,
    ) = CatalogAnalyticsEventsResult(tracker, event)
}
