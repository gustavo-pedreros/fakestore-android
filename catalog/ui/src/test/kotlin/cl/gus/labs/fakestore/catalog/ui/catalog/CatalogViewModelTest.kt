package cl.gus.labs.fakestore.catalog.ui.catalog

import app.cash.turbine.test
import cl.gus.labs.fakestore.catalog.domain.CatalogRepository
import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.model.Rating
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveCatalog
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveCategories
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveLastSyncedAt
import cl.gus.labs.fakestore.catalog.domain.usecase.RefreshCatalog
import cl.gus.labs.fakestore.catalog.ui.FakeNetworkMonitor
import cl.gus.labs.fakestore.catalog.ui.mapper.toCard
import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.core.testing.MainDispatcherExtension
import cl.gus.labs.fakestore.shared.kernel.AppError
import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
@DisplayName("CatalogViewModel")
class CatalogViewModelTest {

    private val widget = Product(
        id = ProductId(1),
        title = "widget",
        price = 9.99,
        description = "a widget",
        category = Category("misc"),
        imageUrl = "https://example.com/widget.png",
        rating = Rating(rate = 4.5, count = 10),
    )

    private val gadget = Product(
        id = ProductId(2),
        title = "gadget",
        price = 19.99,
        description = "a gadget",
        category = Category("tools"),
        imageUrl = "https://example.com/gadget.png",
        rating = Rating(rate = 3.5, count = 20),
    )

    private val syncedAt = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    private val networkError = AppError.Network("timeout")

    @Nested
    @DisplayName("empty cache")
    inner class EmptyCache {

        @Test
        @DisplayName("stays on Loading while the refresh is still in flight")
        fun loadingWhileRefreshIsInFlight() = runTest {
            val inFlight = CompletableDeferred<Either<AppError, Unit>>()
            val viewModel = viewModel(refresh = { inFlight.await() })

            keepUiStateHot(viewModel)

            assertEquals(CatalogContent.Loading, viewModel.uiState.value.content)
        }

        @Test
        @DisplayName("reports the failure as offline when there is no connection")
        fun blockingFailureWhenOffline() = runTest {
            val viewModel = viewModel(
                refresh = { Either.Error(networkError) },
                monitor = FakeNetworkMonitor(MutableStateFlow(false)),
            )

            keepUiStateHot(viewModel)

            assertEquals(
                CatalogContent.Failure(networkError, offline = true),
                viewModel.uiState.value.content,
            )
        }

        @Test
        @DisplayName("reports the failure as online when the connection is up")
        fun blockingFailureWhenOnline() = runTest {
            val viewModel = viewModel(refresh = { Either.Error(networkError) })

            keepUiStateHot(viewModel)

            assertEquals(
                CatalogContent.Failure(networkError, offline = false),
                viewModel.uiState.value.content,
            )
        }

        @Test
        @DisplayName("is Empty once a successful sync returned no products")
        fun emptyAfterSuccessfulSync() = runTest {
            val viewModel = viewModel(lastSyncedAt = syncedAt)

            keepUiStateHot(viewModel)

            assertEquals(CatalogContent.Empty, viewModel.uiState.value.content)
        }
    }

    @Nested
    @DisplayName("cached data")
    inner class CachedData {

        @Test
        @DisplayName("keeps the content and flags the refresh in flight")
        fun contentWhileRefreshing() = runTest {
            val inFlight = CompletableDeferred<Either<AppError, Unit>>()
            val viewModel = viewModel(
                products = { listOf(widget) },
                lastSyncedAt = syncedAt,
                refresh = { inFlight.await() },
            )

            keepUiStateHot(viewModel)

            val state = viewModel.uiState.value
            assertEquals(CatalogContent.Ready(listOf(widget.toCard())), state.content)
            assertTrue(state.isRefreshing)
            assertFalse(state.isStale)
        }

        @Test
        @DisplayName("never lets a failed refresh cover data the user already had")
        fun failedRefreshKeepsContentAndMarksItStale() = runTest {
            val viewModel = viewModel(
                products = { listOf(widget) },
                lastSyncedAt = syncedAt,
                refresh = { Either.Error(networkError) },
            )

            keepUiStateHot(viewModel)

            val state = viewModel.uiState.value
            assertEquals(CatalogContent.Ready(listOf(widget.toCard())), state.content)
            assertTrue(state.isStale)
            assertEquals(syncedAt, state.lastSyncedAt)
        }

        @Test
        @DisplayName("drops the stale flag after a successful refresh")
        fun successfulRefreshClearsStale() = runTest {
            val viewModel = viewModel(products = { listOf(widget) }, lastSyncedAt = syncedAt)

            keepUiStateHot(viewModel)

            val state = viewModel.uiState.value
            assertEquals(CatalogContent.Ready(listOf(widget.toCard())), state.content)
            assertFalse(state.isStale)
            assertFalse(state.isRefreshing)
        }
    }

    @Nested
    @DisplayName("automatic retry")
    inner class AutomaticRetry {

        @Test
        @DisplayName("refreshes again when the connection comes back after a failure")
        fun retriesWhenConnectionComesBack() = runTest {
            var refreshes = 0
            val monitor = FakeNetworkMonitor(MutableStateFlow(false))
            val viewModel = viewModel(
                refresh = {
                    refreshes++
                    Either.Error(networkError)
                },
                monitor = monitor,
            )

            keepUiStateHot(viewModel)
            assertEquals(1, refreshes)

            monitor.isOnline.value = true
            advanceUntilIdle()

            assertEquals(2, refreshes)
        }
    }

    @Nested
    @DisplayName("category filter")
    inner class CategoryFilter {

        @Test
        @DisplayName("re-emits the catalog filtered by the selected category")
        fun reemitsFilteredCatalog() = runTest {
            val viewModel = viewModel(
                products = { category -> if (category == null) listOf(widget, gadget) else listOf(gadget) },
                categories = listOf(widget, gadget),
                lastSyncedAt = syncedAt,
            )

            keepUiStateHot(viewModel)
            assertEquals(
                CatalogContent.Ready(listOf(widget.toCard(), gadget.toCard())),
                viewModel.uiState.value.content,
            )

            viewModel.onCategorySelect("tools")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(CatalogContent.Ready(listOf(gadget.toCard())), state.content)
            assertEquals("tools", state.selectedCategory)
            assertEquals(listOf("misc", "tools"), state.categories)
        }
    }

    @Nested
    @DisplayName("favorites")
    inner class Favorites {

        @Test
        @DisplayName("favorite ids reach the state as raw ints")
        fun favoriteIdsReachState() = runTest {
            val viewModel = viewModel(
                products = { listOf(widget, gadget) },
                lastSyncedAt = syncedAt,
                favoriteIds = setOf(ProductId(2)),
            )

            keepUiStateHot(viewModel)

            assertEquals(setOf(2), viewModel.uiState.value.favoriteIds)
        }

        @Test
        @DisplayName("onFavoriteToggle calls ToggleFavorite with the product id once")
        fun onFavoriteToggleDelegates() = runTest {
            val toggledIds = mutableListOf<ProductId>()
            val viewModel = viewModel(onToggleFavorite = { id -> toggledIds.add(id) })

            viewModel.onFavoriteToggle(2)
            advanceUntilIdle()

            assertEquals(listOf(ProductId(2)), toggledIds)
        }

        @Test
        @DisplayName("changing favorite ids does not alter the catalog content")
        fun favoriteChangeDoesNotAlterContent() = runTest {
            val favoriteIdsFlow = MutableStateFlow(emptySet<ProductId>())
            val viewModel = CatalogViewModel(
                observeCatalog = { flowOf(listOf(widget)) },
                observeCategories = ObserveCategories(repositoryOf(listOf(widget))),
                observeLastSyncedAt = { flowOf(syncedAt) },
                observeFavoriteIds = { favoriteIdsFlow },
                refreshCatalog = { Either.Success(Unit) },
                toggleFavorite = {},
                networkMonitor = FakeNetworkMonitor(),
            )

            keepUiStateHot(viewModel)
            val contentBefore = viewModel.uiState.value.content

            favoriteIdsFlow.value = setOf(ProductId(1))
            advanceUntilIdle()

            assertEquals(contentBefore, viewModel.uiState.value.content)
            assertEquals(setOf(1), viewModel.uiState.value.favoriteIds)
        }
    }

    @Nested
    @DisplayName("error events")
    inner class ErrorEvents {

        @Test
        @DisplayName("only a refresh the user asked for reaches the snackbar")
        fun onlyUserInitiatedFailuresEmit() = runTest {
            val viewModel = viewModel(refresh = { Either.Error(networkError) })

            viewModel.errorEvents.test {
                expectNoEvents()

                viewModel.refresh()

                assertEquals(networkError, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    private fun viewModel(
        products: (Category?) -> List<Product> = { emptyList() },
        categories: List<Product> = products(null),
        lastSyncedAt: Instant? = null,
        favoriteIds: Set<ProductId> = emptySet(),
        refresh: suspend () -> Either<AppError, Unit> = { Either.Success(Unit) },
        onToggleFavorite: suspend (ProductId) -> Unit = {},
        monitor: FakeNetworkMonitor = FakeNetworkMonitor(),
    ) = CatalogViewModel(
        observeCatalog = { category -> flowOf(products(category)) },
        observeCategories = ObserveCategories(repositoryOf(categories)),
        observeLastSyncedAt = { flowOf(lastSyncedAt) },
        observeFavoriteIds = { flowOf(favoriteIds) },
        refreshCatalog = { refresh() },
        toggleFavorite = { id -> onToggleFavorite(id) },
        networkMonitor = monitor,
    )

    private fun repositoryOf(products: List<Product>) = object : CatalogRepository {
        override fun observeAll(category: Category?): Flow<List<Product>> = flowOf(products)

        override fun observeById(id: ProductId): Flow<Product?> = flowOf(null)

        override fun observeLastSyncedAt(): Flow<Instant?> = flowOf(null)

        override suspend fun refresh(): Either<AppError, Unit> = Either.Success(Unit)
    }

    private fun TestScope.keepUiStateHot(viewModel: CatalogViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()
    }
}
