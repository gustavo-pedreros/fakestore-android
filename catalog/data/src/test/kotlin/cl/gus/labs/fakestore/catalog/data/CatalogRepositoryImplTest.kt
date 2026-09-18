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

    private fun createProductEntity(
        id: Int = 1,
        title: String = "widget",
        price: Double = 9.99,
        description: String = "a widget",
        category: String = "misc",
        imageUrl: String = "https://example.com/widget.png",
        ratingRate: Double = 4.5,
        ratingCount: Int = 10,
    ) = ProductEntity(
        id = id,
        title = title,
        price = price,
        description = description,
        category = category,
        imageUrl = imageUrl,
        ratingRate = ratingRate,
        ratingCount = ratingCount,
    )

    @Suppress("SameParameterValue")
    private fun createProductDto(
        id: Int = 1,
        title: String = "widget",
        price: Double = 9.99,
        description: String = "a widget",
        category: String = "misc",
        imageUrl: String = "https://example.com/widget.png",
        rate: Double = 4.5,
        count: Int = 10,
    ) = ProductDto(
        id = id,
        title = title,
        price = price,
        description = description,
        category = category,
        imageUrl = imageUrl,
        rating = RatingDto(rate = rate, count = count),
    )

    @Nested
    @DisplayName("refresh")
    inner class Refresh {

        @Test
        @DisplayName("on success, writes the synced rows and the last-synced timestamp")
        fun successWritesRowsAndTimestamp() = runTest {
            val dto = createProductDto(
                id = 99,
                title = "Special Widget",
                price = 123.45,
                description = "A special widget",
                category = "special",
                imageUrl = "https://example.com/special.png",
                rate = 4.9,
                count = 5
            )
            val local = FakeCatalogLocalDataSource()
            val repository = CatalogRepositoryImpl(
                remote = FakeCatalogRemoteDataSource(Either.Success(listOf(dto))),
                local = local,
                clock = fixedClock,
            )

            val result = repository.refresh()

            assertEquals(Either.Success(Unit), result)
            val expectedEntity = ProductEntity(
                id = 99,
                title = "Special Widget",
                price = 123.45,
                description = "A special widget",
                category = "special",
                imageUrl = "https://example.com/special.png",
                ratingRate = 4.9,
                ratingCount = 5,
            )
            assertEquals(listOf(expectedEntity), local.productsFlow.value)
            assertEquals(fixedInstant, local.lastSyncedAtFlow.value)
        }

        @Test
        @DisplayName("on failure, leaves the cached rows and timestamp untouched")
        fun failureLeavesCacheUntouched() = runTest {
            val local = FakeCatalogLocalDataSource(
                initialProducts = listOf(createProductEntity()),
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
            assertEquals(listOf(createProductEntity()), local.productsFlow.value)
            assertEquals(fixedInstant, local.lastSyncedAtFlow.value)
        }
    }

    @Nested
    @DisplayName("observeAll")
    inner class ObserveAll {

        @Test
        @DisplayName("maps rows to domain products and forwards the category as a String")
        fun mapsToDomainAndForwardsCategory() = runTest {
            val local = FakeCatalogLocalDataSource(initialProducts = listOf(createProductEntity()))
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

        @Test
        @DisplayName("returns all products regardless of category when category is null")
        fun returnsAllProductsWhenCategoryIsNull() = runTest {
            val local = FakeCatalogLocalDataSource(
                initialProducts = listOf(
                    createProductEntity(id = 1),
                    createProductEntity(id = 2, category = "electronics")
                )
            )
            val repository = CatalogRepositoryImpl(
                remote = FakeCatalogRemoteDataSource(Either.Success(emptyList())),
                local = local,
                clock = fixedClock,
            )

            val result = repository.observeAll(null).first()

            assertEquals(null, local.lastRequestedCategory)
            assertEquals(2, result.size)
            assertEquals(ProductId(1), result[0].id)
            assertEquals(ProductId(2), result[1].id)
        }
    }

    @Nested
    @DisplayName("observeById")
    inner class ObserveById {

        @Test
        @DisplayName("returns the product when it exists in the local source")
        fun returnsProductWhenItExists() = runTest {
            val productId = ProductId(1)
            val productEntity = createProductEntity(id = productId.value)
            val local = FakeCatalogLocalDataSource(
                initialProducts = listOf(productEntity)
            )
            val repository = CatalogRepositoryImpl(
                remote = FakeCatalogRemoteDataSource(Either.Success(emptyList())),
                local = local,
                clock = fixedClock,
            )

            val result = repository.observeById(productId).first()

            val expectedProduct = Product(
                id = productId,
                title = "widget",
                price = 9.99,
                description = "a widget",
                category = Category("misc"),
                imageUrl = "https://example.com/widget.png",
                rating = Rating(rate = 4.5, count = 10),
            )
            assertEquals(expectedProduct, result)
        }

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

    @Nested
    @DisplayName("observeLastSyncedAt")
    inner class ObserveLastSyncedAt {

        @Test
        @DisplayName("emits the correct timestamp when a sync has occurred")
        fun emitsTimestampWhenSynced() = runTest {
            val local = FakeCatalogLocalDataSource(
                initialLastSyncedAt = fixedInstant,
            )
            val repository = CatalogRepositoryImpl(
                remote = FakeCatalogRemoteDataSource(Either.Success(emptyList())),
                local = local,
                clock = fixedClock,
            )

            val result = repository.observeLastSyncedAt().first()

            assertEquals(fixedInstant, result)
        }

        @Test
        @DisplayName("emits null when no sync has occurred")
        fun emitsNullWhenNoSync() = runTest {
            val local = FakeCatalogLocalDataSource()
            val repository = CatalogRepositoryImpl(
                remote = FakeCatalogRemoteDataSource(Either.Success(emptyList())),
                local = local,
                clock = fixedClock,
            )

            val result = repository.observeLastSyncedAt().first()

            assertNull(result)
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
