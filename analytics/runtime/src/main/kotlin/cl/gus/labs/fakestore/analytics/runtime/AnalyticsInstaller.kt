package cl.gus.labs.fakestore.analytics.runtime

import cl.gus.labs.fakestore.analytics.Analytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Installs the dispatcher into the [Analytics] holder.
 *
 * Call [install] once from the composition root:
 *
 * ```
 * @HiltAndroidApp
 * class MyApplication : Application() {
 *     @Inject lateinit var analyticsInstaller: AnalyticsInstaller
 *
 *     override fun onCreate() {
 *         super.onCreate()
 *         analyticsInstaller.install()
 *     }
 * }
 * ```
 *
 * Until this runs, events are tracked into `NoOpTracker` and are silently dropped. Install
 * early — before the first screen — or the first events of a cold start go missing.
 */
@Singleton
class AnalyticsInstaller @Inject constructor(
    private val dispatcher: AnalyticsDispatcher,
) {
    fun install() {
        Analytics.install(dispatcher)
    }
}
