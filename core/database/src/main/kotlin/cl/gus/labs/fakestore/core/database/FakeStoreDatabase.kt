package cl.gus.labs.fakestore.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import cl.gus.labs.fakestore.core.database.dao.FavoriteDao
import cl.gus.labs.fakestore.core.database.dao.ProductDao
import cl.gus.labs.fakestore.core.database.dao.SyncMetadataDao
import cl.gus.labs.fakestore.core.database.entity.FavoriteEntity
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import cl.gus.labs.fakestore.core.database.entity.SyncMetadataEntity

@Database(
    entities = [ProductEntity::class, SyncMetadataEntity::class, FavoriteEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class FakeStoreDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun favoriteDao(): FavoriteDao
}
