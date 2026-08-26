package cl.gus.labs.fakestore.analytics.tool.logcat.di

import cl.gus.labs.fakestore.analytics.AnalyticsTracker
import cl.gus.labs.fakestore.analytics.AnalyticsUserSession
import cl.gus.labs.fakestore.analytics.tool.logcat.LogcatClient
import cl.gus.labs.fakestore.analytics.tool.logcat.LogcatClientImpl
import cl.gus.labs.fakestore.analytics.tool.logcat.LogcatTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Registers Logcat into the analytics sets.
 *
 * This module lives in the vendor's own Gradle module. Nothing outside it is edited when
 * Logcat is added or removed — Hilt aggregates `@InstallIn` modules across the whole
 * transitive runtime classpath, so being several modules deep from the app changes nothing.
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface LogcatAnalyticsModule {

    @Binds
    @IntoSet
    fun bindTracker(tracker: LogcatTracker): AnalyticsTracker

    // Delete this binding if Logcat has no identity API.
    @Binds
    @IntoSet
    fun bindUserSession(tracker: LogcatTracker): AnalyticsUserSession

    // The seam that makes the tracker testable with a plain fake.
    @Binds
    fun bindClient(client: LogcatClientImpl): LogcatClient
}
