package cl.gus.labs.fakestore.core.database.di

import android.content.Context
import androidx.room.Room
import cl.gus.labs.fakestore.core.database.FakeStoreDatabase
import cl.gus.labs.fakestore.core.database.dao.FavoriteDao
import cl.gus.labs.fakestore.core.database.dao.ProductDao
import cl.gus.labs.fakestore.core.database.dao.SyncMetadataDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFakeStoreDatabase(@ApplicationContext context: Context): FakeStoreDatabase =
        Room.databaseBuilder(context, FakeStoreDatabase::class.java, "fakestore.db").build()

    @Provides
    @Singleton
    fun provideProductDao(database: FakeStoreDatabase): ProductDao = database.productDao()

    @Provides
    @Singleton
    fun provideSyncMetadataDao(database: FakeStoreDatabase): SyncMetadataDao = database.syncMetadataDao()

    @Provides
    @Singleton
    fun provideFavoriteDao(database: FakeStoreDatabase): FavoriteDao = database.favoriteDao()
}
