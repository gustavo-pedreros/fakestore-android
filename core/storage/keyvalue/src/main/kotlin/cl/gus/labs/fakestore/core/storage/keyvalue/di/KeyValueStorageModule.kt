package cl.gus.labs.fakestore.core.storage.keyvalue.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import cl.gus.labs.fakestore.core.common.di.IoDispatcher
import cl.gus.labs.fakestore.core.storage.keyvalue.KeyValueStoreFactory
import cl.gus.labs.fakestore.core.storage.keyvalue.internal.DataStoreKeyValueStoreFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

// Binds only the factory: DataStore stays out of the graph, so nothing can skip the port.
@Module
@InstallIn(SingletonComponent::class)
internal object KeyValueStorageModule {

    @Provides
    @Singleton
    fun provideKeyValueStoreFactory(
        @ApplicationContext context: Context,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): KeyValueStoreFactory = DataStoreKeyValueStoreFactory(
        scope = CoroutineScope(ioDispatcher + SupervisorJob()),
        produceFile = context::preferencesDataStoreFile,
    )
}
