package cl.gus.labs.fakestore.core.settings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import cl.gus.labs.fakestore.core.settings.SettingsRepository
import cl.gus.labs.fakestore.core.settings.internal.DataStoreSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Binds only the repository: the DataStore stays out of the graph, so nothing can skip the port.
@Module
@InstallIn(SingletonComponent::class)
internal object SettingsModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context,
    ): SettingsRepository = DataStoreSettingsRepository(context.settingsDataStore)
}
