package cl.gus.labs.fakestore.analytics.tool.console.di

import cl.gus.labs.fakestore.analytics.AnalyticsTracker
import cl.gus.labs.fakestore.analytics.AnalyticsUserSession
import cl.gus.labs.fakestore.analytics.tool.console.ConsoleClient
import cl.gus.labs.fakestore.analytics.tool.console.ConsoleClientImpl
import cl.gus.labs.fakestore.analytics.tool.console.ConsoleTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Registers Console into the analytics sets.
 *
 * This module lives in the vendor's own Gradle module. Nothing outside it is edited when
 * Console is added or removed — Hilt aggregates `@InstallIn` modules across the whole
 * transitive runtime classpath, so being several modules deep from the app changes nothing.
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface ConsoleAnalyticsModule {

    @Binds
    @IntoSet
    fun bindTracker(tracker: ConsoleTracker): AnalyticsTracker

    // Delete this binding if Console has no identity API.
    @Binds
    @IntoSet
    fun bindUserSession(tracker: ConsoleTracker): AnalyticsUserSession

    // The seam that makes the tracker testable with a plain fake.
    @Binds
    fun bindClient(client: ConsoleClientImpl): ConsoleClient
}
