package cl.gus.labs.fakestore.catalog.data.datasource

import app.cash.turbine.test
import cl.gus.labs.fakestore.core.database.dao.ProductDao
import cl.gus.labs.fakestore.core.database.dao.SyncMetadataDao
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import cl.gus.labs.fakestore.core.database.entity.SyncMetadataEntity
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("CatalogLocalDataSourceImpl")
class CatalogLocalDataSourceImplTest {

    private val lastSyncedAtMillis = 1_700_000_000_000L
    private val lastSyncedAt = Instant.fromEpochMilliseconds(lastSyncedAtMillis)

    private val misc = createProductEntity(id = 1, category = "misc")
    private val electronics = createProductEntity(id = 2, category = "electronics")

    private fun createProductEntity(id: Int, category: String, price: Double = 9.99) = ProductEntity(
        id = id,
        title = "product $id",
        price = price,
        description = "description $id",
        category = category,
        imageUrl = "https://example.com/$id.png",
        ratingRate = 4.5,
        ratingCount = 10,
    )

    private fun createDataSource(
        productDao: ProductDao = FakeProductDao(),
        syncMetadataDao: SyncMetadataDao = FakeSyncMetadataDao(),
    ) = CatalogLocalDataSourceImpl(productDao, syncMetadataDao)

    @Nested
    @DisplayName("observeAll")
    inner class ObserveAll {

        @Test
        @DisplayName("forwards the category to the DAO")
        fun forwardsCategory() = runTest {
            val dataSource = createDataSource(productDao = FakeProductDao(listOf(misc, electronics)))

            assertEquals(listOf(electronics), dataSource.observeAll("electronics").first())
        }

        @Test
        @DisplayName("forwards a null category to get every row")
        fun forwardsNullCategory() = runTest {
            val dataSource = createDataSource(productDao = FakeProductDao(listOf(misc, electronics)))

            assertEquals(listOf(misc, electronics), dataSource.observeAll(null).first())
        }

        @Test
        @DisplayName("re-emits only when the filtered rows change")
        fun reEmitsOnlyWhenFilteredRowsChange() = runTest {
            val dataSource = createDataSource(productDao = FakeProductDao(listOf(misc, electronics)))

            dataSource.observeAll("electronics").test {
                assertEquals(listOf(electronics), awaitItem())

                dataSource.syncAll(listOf(misc.copy(price = 19.99), electronics))
                expectNoEvents()

                dataSource.syncAll(listOf(misc, electronics.copy(price = 19.99)))
                assertEquals(listOf(electronics.copy(price = 19.99)), awaitItem())
            }
        }
    }

    @Nested
    @DisplayName("observeById")
    inner class ObserveById {

        @Test
        @DisplayName("forwards the id to the DAO")
        fun forwardsId() = runTest {
            val dataSource = createDataSource(productDao = FakeProductDao(listOf(misc, electronics)))

            assertEquals(electronics, dataSource.observeById(2).first())
        }

        @Test
        @DisplayName("re-emits only when the product itself changes")
        fun reEmitsOnlyWhenProductChanges() = runTest {
            val dataSource = createDataSource(productDao = FakeProductDao(listOf(misc, electronics)))

            dataSource.observeById(2).test {
                assertEquals(electronics, awaitItem())

                dataSource.syncAll(listOf(misc.copy(price = 19.99), electronics))
                expectNoEvents()

                dataSource.syncAll(listOf(misc, electronics.copy(price = 19.99)))
                assertEquals(electronics.copy(price = 19.99), awaitItem())
            }
        }
    }

    @Nested
    @DisplayName("syncAll")
    inner class SyncAll {

        @Test
        @DisplayName("writes the snapshot through the DAO's transactional syncAll")
        fun writesThroughTransactionalSyncAll() = runTest {
            val productDao = FakeProductDao()
            val dataSource = createDataSource(productDao = productDao)

            dataSource.syncAll(listOf(misc, electronics))

            assertEquals(listOf(misc, electronics), productDao.observeAll(null).first())
        }
    }

    @Nested
    @DisplayName("observeLastSyncedAt")
    inner class ObserveLastSyncedAt {

        @Test
        @DisplayName("emits null before the first sync")
        fun emitsNullBeforeFirstSync() = runTest {
            assertNull(createDataSource().observeLastSyncedAt().first())
        }

        @Test
        @DisplayName("converts the stored epoch millis into an Instant")
        fun convertsEpochMillis() = runTest {
            val dataSource = createDataSource(
                syncMetadataDao = FakeSyncMetadataDao(listOf(SyncMetadataEntity("catalog", lastSyncedAtMillis))),
            )

            assertEquals(lastSyncedAt, dataSource.observeLastSyncedAt().first())
        }

        @Test
        @DisplayName("ignores timestamps stored under another scope")
        fun ignoresOtherScopes() = runTest {
            val dataSource = createDataSource(
                syncMetadataDao = FakeSyncMetadataDao(listOf(SyncMetadataEntity("favorites", lastSyncedAtMillis))),
            )

            assertNull(dataSource.observeLastSyncedAt().first())
        }

        @Test
        @DisplayName("re-emits only when the catalog timestamp changes")
        fun reEmitsOnlyWhenCatalogTimestampChanges() = runTest {
            val syncMetadataDao = FakeSyncMetadataDao(listOf(SyncMetadataEntity("catalog", lastSyncedAtMillis)))
            val dataSource = createDataSource(syncMetadataDao = syncMetadataDao)

            dataSource.observeLastSyncedAt().test {
                assertEquals(lastSyncedAt, awaitItem())

                syncMetadataDao.upsert(SyncMetadataEntity("favorites", lastSyncedAtMillis))
                expectNoEvents()

                val later = Instant.fromEpochMilliseconds(1_800_000_000_000L)
                dataSource.writeLastSyncedAt(later)
                assertEquals(later, awaitItem())
            }
        }
    }

    @Nested
    @DisplayName("writeLastSyncedAt")
    inner class WriteLastSyncedAt {

        @Test
        @DisplayName("stores the instant as epoch millis under the catalog scope")
        fun storesEpochMillisUnderCatalogScope() = runTest {
            val syncMetadataDao = FakeSyncMetadataDao()
            val dataSource = createDataSource(syncMetadataDao = syncMetadataDao)

            dataSource.writeLastSyncedAt(lastSyncedAt)

            assertEquals(
                SyncMetadataEntity(scope = "catalog", lastSyncedAt = lastSyncedAtMillis),
                syncMetadataDao.observeByScope("catalog").first(),
            )
        }
    }
}

private class FakeProductDao(
    initialRows: List<ProductEntity> = emptyList(),
) : ProductDao {

    private val table = MutableSharedFlow<List<ProductEntity>>(replay = 1).apply { tryEmit(initialRows) }

    override fun observeAll(category: String?): Flow<List<ProductEntity>> =
        table.map { rows -> rows.filter { category == null || it.category == category } }

    override fun observeById(id: Int): Flow<ProductEntity?> =
        table.map { rows -> rows.find { it.id == id } }

    override suspend fun upsertAll(products: List<ProductEntity>) {
        error("CatalogLocalDataSourceImpl must write through the transactional syncAll")
    }

    override suspend fun deleteMissing(ids: List<Int>) {
        error("CatalogLocalDataSourceImpl must write through the transactional syncAll")
    }

    override suspend fun syncAll(products: List<ProductEntity>) {
        table.emit(products)
    }
}

private class FakeSyncMetadataDao(
    initialRows: List<SyncMetadataEntity> = emptyList(),
) : SyncMetadataDao {

    private var rows = initialRows.associateBy(SyncMetadataEntity::scope)
    private val table = MutableSharedFlow<Map<String, SyncMetadataEntity>>(replay = 1).apply { tryEmit(rows) }

    override fun observeByScope(scope: String): Flow<SyncMetadataEntity?> =
        table.map { it[scope] }

    override suspend fun upsert(metadata: SyncMetadataEntity) {
        rows = rows + (metadata.scope to metadata)
        table.emit(rows)
    }
}