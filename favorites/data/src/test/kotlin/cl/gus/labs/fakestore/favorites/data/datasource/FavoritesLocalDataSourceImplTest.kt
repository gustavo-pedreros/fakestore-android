package cl.gus.labs.fakestore.favorites.data.datasource

import app.cash.turbine.test
import cl.gus.labs.fakestore.core.database.dao.FavoriteDao
import cl.gus.labs.fakestore.core.database.entity.FavoriteEntity
import cl.gus.labs.fakestore.core.database.entity.SyncState
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("FavoritesLocalDataSourceImpl")
class FavoritesLocalDataSourceImplTest {

    private val toggledAtMillis = 1_700_000_000_000L
    private val toggledAt = Instant.fromEpochMilliseconds(toggledAtMillis)
    private val later = Instant.fromEpochMilliseconds(1_800_000_000_000L)

    private fun createFavoriteEntity(productId: Int, updatedAt: Long = toggledAtMillis) = FavoriteEntity(
        productId = productId,
        updatedAt = updatedAt,
        syncState = SyncState.LOCAL_ONLY,
    )

    private fun createDataSource(favoriteDao: FavoriteDao = FakeFavoriteDao()) =
        FavoritesLocalDataSourceImpl(favoriteDao)

    @Nested
    @DisplayName("observeIds")
    inner class ObserveIds {

        @Test
        @DisplayName("emits an empty list when there are no favorites")
        fun emitsEmptyListWithoutFavorites() = runTest {
            assertEquals(emptyList<Int>(), createDataSource().observeIds().first())
        }

        @Test
        @DisplayName("forwards the ids in the DAO's order")
        fun forwardsDaoOrder() = runTest {
            val dataSource = createDataSource(
                favoriteDao = FakeFavoriteDao(
                    listOf(
                        createFavoriteEntity(productId = 1, updatedAt = 1_000L),
                        createFavoriteEntity(productId = 2, updatedAt = 2_000L),
                    ),
                ),
            )

            assertEquals(listOf(2, 1), dataSource.observeIds().first())
        }

        @Test
        @DisplayName("re-emits only when the ids change")
        fun reEmitsOnlyWhenIdsChange() = runTest {
            val favoriteDao = FakeFavoriteDao(listOf(createFavoriteEntity(productId = 1)))
            val dataSource = createDataSource(favoriteDao = favoriteDao)

            dataSource.observeIds().test {
                assertEquals(listOf(1), awaitItem())

                favoriteDao.insert(createFavoriteEntity(productId = 1))
                expectNoEvents()

                dataSource.toggle(productId = 2, at = later)
                assertEquals(listOf(2, 1), awaitItem())
            }
        }
    }

    @Nested
    @DisplayName("toggle")
    inner class Toggle {

        @Test
        @DisplayName("inserts a local-only favorite stamped with the instant as epoch millis")
        fun insertsLocalOnlyFavoriteStampedWithInstant() = runTest {
            val favoriteDao = FakeFavoriteDao()
            val dataSource = createDataSource(favoriteDao = favoriteDao)

            dataSource.toggle(productId = 7, at = toggledAt)

            assertEquals(
                listOf(FavoriteEntity(productId = 7, updatedAt = toggledAtMillis, syncState = SyncState.LOCAL_ONLY)),
                favoriteDao.rows.values.toList(),
            )
        }

        @Test
        @DisplayName("removes a product that is already a favorite")
        fun removesExistingFavorite() = runTest {
            val favoriteDao = FakeFavoriteDao(listOf(createFavoriteEntity(productId = 7)))
            val dataSource = createDataSource(favoriteDao = favoriteDao)

            dataSource.toggle(productId = 7, at = later)

            assertEquals(emptyList<Int>(), dataSource.observeIds().first())
        }
    }
}

private class FakeFavoriteDao(
    initialRows: List<FavoriteEntity> = emptyList(),
) : FavoriteDao {

    var rows = initialRows.associateBy(FavoriteEntity::productId)
        private set
    private val table = MutableSharedFlow<Map<Int, FavoriteEntity>>(replay = 1).apply { tryEmit(rows) }

    override fun observeIds(): Flow<List<Int>> =
        table.map { rows -> rows.values.sortedByDescending(FavoriteEntity::updatedAt).map(FavoriteEntity::productId) }

    override suspend fun count(productId: Int): Int =
        error("FavoritesLocalDataSourceImpl must write through the transactional toggle")

    override suspend fun insert(favorite: FavoriteEntity) {
        write(rows + (favorite.productId to favorite))
    }

    override suspend fun deleteById(productId: Int) {
        error("FavoritesLocalDataSourceImpl must write through the transactional toggle")
    }

    override suspend fun toggle(favorite: FavoriteEntity) {
        write(if (favorite.productId in rows) rows - favorite.productId else rows + (favorite.productId to favorite))
    }

    private suspend fun write(newRows: Map<Int, FavoriteEntity>) {
        rows = newRows
        table.emit(rows)
    }
}
