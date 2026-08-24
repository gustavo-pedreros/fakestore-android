package cl.gus.labs.fakestore.favorites.data

import cl.gus.labs.fakestore.favorites.data.datasource.FavoritesLocalDataSource
import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("FavoritesRepositoryImpl")
class FavoritesRepositoryImplTest {

    private val fixedInstant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    private val fixedClock = object : Clock {
        override fun now(): Instant = fixedInstant
    }

    @Nested
    @DisplayName("observeIds")
    inner class ObserveIds {

        @Test
        @DisplayName("maps the local ids to a set of ProductId")
        fun mapsIdsToProductIdSet() = runTest {
            val local = FakeFavoritesLocalDataSource(initialIds = listOf(2, 1))
            val repository = FavoritesRepositoryImpl(local = local, clock = fixedClock)

            val result = repository.observeIds().first()

            assertEquals(setOf(ProductId(1), ProductId(2)), result)
        }

        @Test
        @DisplayName("an empty local list maps to an empty set")
        fun emptyListMapsToEmptySet() = runTest {
            val local = FakeFavoritesLocalDataSource()
            val repository = FavoritesRepositoryImpl(local = local, clock = fixedClock)

            val result = repository.observeIds().first()

            assertEquals(emptySet<ProductId>(), result)
        }
    }

    @Nested
    @DisplayName("toggle")
    inner class Toggle {

        @Test
        @DisplayName("delegates to the local datasource with the clock's current instant")
        fun delegatesWithClockInstant() = runTest {
            val local = FakeFavoritesLocalDataSource()
            val repository = FavoritesRepositoryImpl(local = local, clock = fixedClock)

            repository.toggle(ProductId(7))

            assertEquals(7, local.lastToggledProductId)
            assertEquals(fixedInstant, local.lastToggledAt)
        }
    }
}

private class FakeFavoritesLocalDataSource(
    initialIds: List<Int> = emptyList(),
) : FavoritesLocalDataSource {

    val idsFlow = MutableStateFlow(initialIds)
    var lastToggledProductId: Int? = null
        private set
    var lastToggledAt: Instant? = null
        private set

    override fun observeIds(): Flow<List<Int>> = idsFlow

    override suspend fun toggle(productId: Int, at: Instant) {
        lastToggledProductId = productId
        lastToggledAt = at
    }
}
