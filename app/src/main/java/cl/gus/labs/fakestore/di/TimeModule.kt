package cl.gus.labs.fakestore.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal object TimeModule {

    @Provides
    fun provideClock(): Clock = Clock.System
}