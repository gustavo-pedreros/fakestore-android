package cl.gus.labs.fakestore.analytics.runtime.di

import cl.gus.labs.fakestore.analytics.AnalyticsTracker
import cl.gus.labs.fakestore.analytics.AnalyticsUserSession
import cl.gus.labs.fakestore.analytics.runtime.AnalyticsDispatcher
import cl.gus.labs.fakestore.analytics.runtime.CompositeUserSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import javax.inject.Singleton

/**
 * Declares the multibinding sets that vendor modules contribute to.
 *
 * `@Multibinds` is required, not optional: without it, a build that ships zero vendors fails
 * to compile because Dagger has no binding for `Set<AnalyticsTracker>` at all. Declaring the
 * set here means "this set exists and may be empty", which is what makes removing the last
 * vendor a one-line change rather than a compile error.
 */
@Module
@InstallIn(SingletonComponent::class)
interface AnalyticsMultibindingDeclarations {
    @Multibinds fun trackers(): Set<AnalyticsTracker>
    @Multibinds fun userSessions(): Set<AnalyticsUserSession>
}

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsRuntimeModule {

    /**
     * The dispatcher is provided by its concrete type and is deliberately NOT contributed
     * with `@IntoSet`. Adding it to the set it consumes would make it dispatch to itself.
     *
     * `@JvmSuppressWildcards` is mandatory: without it Kotlin generates
     * `Set<? extends AnalyticsTracker>` and Dagger reports the binding as missing.
     */
    @Provides
    @Singleton
    fun provideDispatcher(
        trackers: Set<@JvmSuppressWildcards AnalyticsTracker>,
    ): AnalyticsDispatcher = AnalyticsDispatcher(trackers)

    @Provides
    @Singleton
    fun provideUserSession(
        sessions: Set<@JvmSuppressWildcards AnalyticsUserSession>,
    ): AnalyticsUserSession = CompositeUserSession(sessions)
}
