package cl.gus.labs.fakestore.catalog.ui.detail

import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.model.Rating
import cl.gus.labs.fakestore.catalog.ui.FakeNetworkMonitor
import cl.gus.labs.fakestore.catalog.ui.MainDispatcherExtension
import cl.gus.labs.fakestore.catalog.ui.mapper.toDetail
import cl.gus.labs.fakestore.core.common.result.Either
import cl.gus.labs.fakestore.shared.kernel.AppError
import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
@DisplayName("ProductDetailViewModel")
class ProductDetailViewModelTest {

    private val widget = Product(
        id = ProductId(1),
        title = "widget",
        price = 9.99,
        description = "a widget",
        category = Category("misc"),
        imageUrl = "https://example.com/widget.png",
        rating = Rating(rate = 4.5, count = 10),
    )

    private val syncedAt = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    private val networkError = AppError.Network("timeout")

    @Nested
    @DisplayName("cached product")
    inner class CachedProduct {

        @Test
        @DisplayName("shows the product without asking the catalog to refresh")
        fun showsCachedProductWithoutRefreshing() = runTest {
            var refreshes = 0
            val viewModel = viewModel(
                product = widget,
                lastSyncedAt = syncedAt,
                refresh = {
                    refreshes++
                    Either.Success(Unit)
                },
            )

            keepUiStateHot(viewModel)

            assertEquals(
                ProductDetailContent.Ready(widget.toDetail()),
                viewModel.uiState.value.content,
            )
            assertEquals(0, refreshes)
        }

        @Test
        @DisplayName("never lets a failed refresh cover the product the user already had")
        fun failedRefreshKeepsProductAndMarksItStale() = runTest {
            val viewModel = viewModel(
                product = widget,
                lastSyncedAt = syncedAt,
                refresh = { Either.Error(networkError) },
            )

            keepUiStateHot(viewModel)
            viewModel.refresh()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(ProductDetailContent.Ready(widget.toDetail()), state.content)
            assertTrue(state.isStale)
            assertEquals(syncedAt, state.lastSyncedAt)
        }
    }

    @Nested
    @DisplayName("missing product")
    inner class MissingProduct {

        @Test
        @DisplayName("refreshes the catalog and reports the failure as offline")
        fun blockingFailureWhenOffline() = runTest {
            val viewModel = viewModel(
                refresh = { Either.Error(networkError) },
                monitor = FakeNetworkMonitor(MutableStateFlow(false)),
            )

            keepUiStateHot(viewModel)

            assertEquals(
                ProductDetailContent.Failure(networkError, offline = true),
                viewModel.uiState.value.content,
            )
        }

        @Test
        @DisplayName("reports the failure as online when the connection is up")
        fun blockingFailureWhenOnline() = runTest {
            val viewModel = viewModel(refresh = { Either.Error(networkError) })

            keepUiStateHot(viewModel)

            assertEquals(
                ProductDetailContent.Failure(networkError, offline = false),
                viewModel.uiState.value.content,
            )
        }

        @Test
        @DisplayName("is Unavailable once a successful sync did not bring it back")
        fun unavailableAfterSuccessfulSync() = runTest {
            val viewModel = viewModel(lastSyncedAt = syncedAt)

            keepUiStateHot(viewModel)

            assertEquals(ProductDetailContent.Unavailable, viewModel.uiState.value.content)
        }

        @Test
        @DisplayName("is Loading while the first refresh is still in flight")
        fun loadingWhileRefreshIsInFlight() = runTest {
            val inFlight = CompletableDeferred<Either<AppError, Unit>>()
            val viewModel = viewModel(refresh = { inFlight.await() })

            keepUiStateHot(viewModel)

            assertEquals(ProductDetailContent.Loading, viewModel.uiState.value.content)
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

    private fun viewModel(
        product: Product? = null,
        lastSyncedAt: Instant? = null,
        refresh: suspend () -> Either<AppError, Unit> = { Either.Success(Unit) },
        monitor: FakeNetworkMonitor = FakeNetworkMonitor(),
    ) = ProductDetailViewModel(
        productId = widget.id.value,
        observeProductDetail = { flowOf(product) },
        observeLastSyncedAt = { flowOf(lastSyncedAt) },
        refreshCatalog = { refresh() },
        networkMonitor = monitor,
    )

    private fun TestScope.keepUiStateHot(viewModel: ProductDetailViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()
    }
}
