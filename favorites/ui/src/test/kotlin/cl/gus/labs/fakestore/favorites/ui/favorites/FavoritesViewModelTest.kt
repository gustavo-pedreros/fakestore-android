package cl.gus.labs.fakestore.favorites.ui.favorites

import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.model.Rating
import cl.gus.labs.fakestore.core.testing.MainDispatcherExtension
import cl.gus.labs.fakestore.favorites.ui.mapper.toCard
import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
@DisplayName("FavoritesViewModel")
class FavoritesViewModelTest {

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

    @Nested
    @DisplayName("no favorites")
    inner class NoFavorites {

        @Test
        @DisplayName("is Empty when there are no favorite ids")
        fun emptyWhenNoFavoriteIds() = runTest {
            val viewModel = viewModel(products = listOf(widget, gadget))

            keepUiStateHot(viewModel)

            assertEquals(FavoritesContent.Empty, viewModel.uiState.value.content)
        }
    }

    @Nested
    @DisplayName("with favorites")
    inner class WithFavorites {

        @Test
        @DisplayName("is Ready with the favorites in catalog order")
        fun readyInCatalogOrder() = runTest {
            val viewModel = viewModel(
                products = listOf(widget, gadget),
                favoriteIds = setOf(gadget.id, widget.id),
            )

            keepUiStateHot(viewModel)

            assertEquals(
                FavoritesContent.Ready(listOf(widget.toCard(), gadget.toCard())),
                viewModel.uiState.value.content,
            )
        }

        @Test
        @DisplayName("omits a favorite id that has no matching cached product")
        fun omitsOrphanFavoriteId() = runTest {
            val viewModel = viewModel(
                products = listOf(widget),
                favoriteIds = setOf(widget.id, ProductId(99)),
            )

            keepUiStateHot(viewModel)

            assertEquals(FavoritesContent.Ready(listOf(widget.toCard())), viewModel.uiState.value.content)
        }

        @Test
        @DisplayName("goes back to Empty once the last favorite is removed")
        fun backToEmptyAfterRemovingLast() = runTest {
            val favoriteIdsFlow = MutableStateFlow(setOf(widget.id))
            val viewModel = FavoritesViewModel(
                observeCatalog = { flowOf(listOf(widget)) },
                observeFavoriteIds = { favoriteIdsFlow },
                toggleFavorite = {},
            )

            keepUiStateHot(viewModel)
            assertEquals(FavoritesContent.Ready(listOf(widget.toCard())), viewModel.uiState.value.content)

            favoriteIdsFlow.value = emptySet()
            advanceUntilIdle()

            assertEquals(FavoritesContent.Empty, viewModel.uiState.value.content)
        }
    }

    @Nested
    @DisplayName("favorite toggle")
    inner class FavoriteToggle {

        @Test
        @DisplayName("onFavoriteToggle calls ToggleFavorite with the product id once")
        fun onFavoriteToggleDelegates() = runTest {
            val toggledIds = mutableListOf<ProductId>()
            val viewModel = viewModel(
                products = listOf(widget),
                favoriteIds = setOf(widget.id),
                onToggleFavorite = { id -> toggledIds.add(id) },
            )

            viewModel.onFavoriteToggle(1)
            advanceUntilIdle()

            assertEquals(listOf(ProductId(1)), toggledIds)
        }
    }

    private fun viewModel(
        products: List<Product> = emptyList(),
        favoriteIds: Set<ProductId> = emptySet(),
        onToggleFavorite: suspend (ProductId) -> Unit = {},
    ) = FavoritesViewModel(
        observeCatalog = { flowOf(products) },
        observeFavoriteIds = { flowOf(favoriteIds) },
        toggleFavorite = { id -> onToggleFavorite(id) },
    )

    private fun TestScope.keepUiStateHot(viewModel: FavoritesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        advanceUntilIdle()
    }
}
