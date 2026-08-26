package cl.gus.labs.fakestore

import android.app.Application
import cl.gus.labs.fakestore.analytics.runtime.AnalyticsInstaller
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FakeStoreApp : Application() {

    @Inject
    lateinit var analyticsInstaller: AnalyticsInstaller

    override fun onCreate() {
        super.onCreate()
        // Install early: until this runs, events go to NoOpTracker and are dropped.
        analyticsInstaller.install()
    }
}
