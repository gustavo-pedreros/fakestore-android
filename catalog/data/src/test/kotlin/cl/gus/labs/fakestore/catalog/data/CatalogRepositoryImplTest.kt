package cl.gus.labs.fakestore.catalog.data

import cl.gus.labs.fakestore.catalog.data.datasource.CatalogLocalDataSource
import cl.gus.labs.fakestore.catalog.data.datasource.CatalogRemoteDataSource
import cl.gus.labs.fakestore.catalog.data.dto.ProductDto
import cl.gus.labs.fakestore.catalog.data.dto.RatingDto
import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.model.Rating
import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import cl.gus.labs.fakestore.core.testing.FixedClock
import cl.gus.labs.fakestore.shared.kernel.AppError
import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("CatalogRepositoryImpl")
class CatalogRepositoryImplTest {

    private val fixedInstant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    private val fixedClock = FixedClock(fixedInstant)

    private val productEntity = ProductEntity(
        id = 1,
        title = "widget",
        price = 9.99,
        description = "a widget",
        category = "misc",
        imageUrl = "https://example.com/widget.png",
        ratingRate = 4.5,
        ratingCount = 10,
    )

    private val productDto = ProductDto(
        id = 1,
        title = "widget",
        price = 9.99,
        description = "a widget",
        category = "misc",
        imageUrl = "https://example.com/widget.png",
        rating = RatingDto(rate = 4.5, count = 10),
    )

    @Nested
    @DisplayName("refresh")
    inner class Refresh {

        @Test
        @DisplayName("on success, writes the synced rows and the last-synced timestamp")
        fun successWritesRowsAndTimestamp() = runTest {
            val local = FakeCatalogLocalDataSource()
            val repository = CatalogRepositoryImpl(
                remote = FakeCatalogRemoteDataSource(Either.Success(listOf(productDto))),
                local = local,
                clock = fixedClock,
            )

            val result = repository.refresh()

            assertEquals(Either.Success(Unit), result)
            assertEquals(listOf(productEntity), local.productsFlow.value)
            assertEquals(fixedInstant, local.lastSyncedAtFlow.value)
        }

        @Test
        @DisplayName("on failure, leaves the cached rows and timestamp untouched")
        fun failureLeavesCacheUntouched() = runTest {
            val local = FakeCatalogLocalDataSource(
                initialProducts = listOf(productEntity),
                initialLastSyncedAt = fixedInstant,
            )
            val error = AppError.Network(message = "offline")
            val repository = CatalogRepositoryImpl(
                remote = FakeCatalogRemoteDataSource(Either.Error(error)),
                local = local,
                clock = fixedClock,
            )

            val result = repository.refresh()

            assertEquals(Either.Error(error), result)
            assertEquals(listOf(productEntity), local.productsFlow.value)
            assertEquals(fixedInstant, local.lastSyncedAtFlow.value)
        }
    }

    @Nested
    @DisplayName("observeAll")
    inner class ObserveAll {

        @Test
        @DisplayName("maps rows to domain products and forwards the category as a String")
        fun mapsToDomainAndForwardsCategory() = runTest {
            val local = FakeCatalogLocalDataSource(initialProducts = listOf(productEntity))
            val repository = CatalogRepositoryImpl(
                remote = FakeCatalogRemoteDataSource(Either.Success(emptyList())),
                local = local,
                clock = fixedClock,
            )

            val result = repository.observeAll(Category("misc")).first()

            assertEquals("misc", local.lastRequestedCategory)
            assertEquals(
                listOf(
                    Product(
                        id = ProductId(1),
                        title = "widget",
                        price = 9.99,
                        description = "a widget",
                        category = Category("misc"),
                        imageUrl = "https://example.com/widget.png",
                        rating = Rating(rate = 4.5, count = 10),
                    ),
                ),
                result,
            )
        }
    }

    @Nested
    @DisplayName("observeById")
    inner class ObserveById {

        @Test
        @DisplayName("maps a null row to a null product")
        fun mapsNullToNull() = runTest {
            val local = FakeCatalogLocalDataSource()
            val repository = CatalogRepositoryImpl(
                remote = FakeCatalogRemoteDataSource(Either.Success(emptyList())),
                local = local,
                clock = fixedClock,
            )

            assertNull(repository.observeById(ProductId(99)).first())
        }
    }
}

private class FakeCatalogRemoteDataSource(
    private val result: Either<AppError, List<ProductDto>>,
) : CatalogRemoteDataSource {
    override suspend fun fetchCatalog(): Either<AppError, List<ProductDto>> = result
}

private class FakeCatalogLocalDataSource(
    initialProducts: List<ProductEntity> = emptyList(),
    initialLastSyncedAt: Instant? = null,
) : CatalogLocalDataSource {

    val productsFlow = MutableStateFlow(initialProducts)
    val lastSyncedAtFlow = MutableStateFlow(initialLastSyncedAt)
    var lastRequestedCategory: String? = null
        private set

    override fun observeAll(category: String?): Flow<List<ProductEntity>> {
        lastRequestedCategory = category
        return productsFlow.map { rows -> if (category == null) rows else rows.filter { it.category == category } }
    }

    override fun observeById(id: Int): Flow<ProductEntity?> =
        productsFlow.map { rows -> rows.find { it.id == id } }

    override suspend fun syncAll(rows: List<ProductEntity>) {
        productsFlow.value = rows
    }

    override fun observeLastSyncedAt(): Flow<Instant?> = lastSyncedAtFlow

    override suspend fun writeLastSyncedAt(at: Instant) {
        lastSyncedAtFlow.value = at
    }
}
