package cl.gus.labs.fakestore.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import cl.gus.labs.fakestore.core.database.dao.ProductDao
import cl.gus.labs.fakestore.core.database.dao.SyncMetadataDao
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import cl.gus.labs.fakestore.core.database.entity.SyncMetadataEntity

@Database(
    entities = [ProductEntity::class, SyncMetadataEntity::class],
    version = 1,
)
abstract class FakeStoreDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun syncMetadataDao(): SyncMetadataDao
}
