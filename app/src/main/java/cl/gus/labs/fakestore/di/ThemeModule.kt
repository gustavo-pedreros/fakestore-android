package cl.gus.labs.fakestore.di

import cl.gus.labs.fakestore.core.storage.keyvalue.KeyValueStoreFactory
import cl.gus.labs.fakestore.theme.ThemeRepository
import cl.gus.labs.fakestore.theme.ThemeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object ThemeModule {

    // Singleton, so the app shell's store is opened once.
    @Provides
    @Singleton
    fun provideThemeRepository(stores: KeyValueStoreFactory): ThemeRepository =
        ThemeRepositoryImpl(stores.create("app"))
}
