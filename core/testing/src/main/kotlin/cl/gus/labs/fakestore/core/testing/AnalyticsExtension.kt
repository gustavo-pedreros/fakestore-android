package cl.gus.labs.fakestore.core.testing

import cl.gus.labs.fakestore.analytics.Analytics
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Installs a [RecordingTracker] around each test and tears it down afterwards.
 *
 * `Analytics` is a global, so a test that installs a tracker and does not reset it will make
 * some unrelated test fail later, in a way that is miserable to track down. This extension
 * exists so no test has to remember.
 *
 * ```
 * class CheckoutViewModelTest {
 *     @JvmField
 *     @RegisterExtension
 *     val analytics = AnalyticsExtension()
 *
 *     @Test
 *     fun tracksOnPurchase() {
 *         viewModel.purchase()
 *         assertEquals(listOf("Checkout Flow Completed"), analytics.tracker.names)
 *     }
 * }
 * ```
 *
 * Tests using this extension must not run in parallel with each other within a class — the
 * installed tracker is process-wide. JUnit's default is sequential, so this only matters if
 * parallel execution has been switched on.
 */
class AnalyticsExtension(
    val tracker: RecordingTracker = RecordingTracker(),
) : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        tracker.clear()
        Analytics.install(tracker)
    }

    override fun afterEach(context: ExtensionContext) {
        Analytics.reset()
        tracker.clear()
    }
}
