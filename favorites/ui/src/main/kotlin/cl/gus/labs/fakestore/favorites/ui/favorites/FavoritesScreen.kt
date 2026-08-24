package cl.gus.labs.fakestore.favorites.ui.favorites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.gus.labs.fakestore.core.designsystem.R as DesignSystemR
import cl.gus.labs.fakestore.core.designsystem.icon.FsIcons
import cl.gus.labs.fakestore.core.designsystem.model.FsUiState
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.core.designsystem.molecule.FsDetailTopBar
import cl.gus.labs.fakestore.core.designsystem.molecule.FsStateBlock
import cl.gus.labs.fakestore.core.designsystem.organism.FsStateHost
import cl.gus.labs.fakestore.core.designsystem.organism.ProductGrid
import cl.gus.labs.fakestore.core.designsystem.organism.ProductGridSkeleton
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import cl.gus.labs.fakestore.favorites.ui.R

@Composable
internal fun FavoritesScreen(
    onProductClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesScreen(
        state = state,
        onProductClick = onProductClick,
        onBackClick = onBackClick,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        modifier = modifier,
    )
}

@Composable
internal fun FavoritesScreen(
    state: FavoritesUiState,
    onProductClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()

    Column(modifier = modifier.fillMaxSize()) {
        FsDetailTopBar(
            title = stringResource(DesignSystemR.string.fs_action_favorites),
            titleVisible = true,
            onBackClick = onBackClick,
        )
        FsStateHost(
            state = state.content.toUiState(),
            loading = { ProductGridSkeleton(modifier = Modifier.fillMaxSize()) },
            empty = {
                CenteredBlock {
                    FsStateBlock(
                        icon = FsIcons.HeartOutline,
                        title = stringResource(R.string.fav_empty_title),
                        modifier = Modifier.fillMaxWidth(),
                        body = stringResource(R.string.fav_empty_body),
                    )
                }
            },
            failure = { },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            content = { products ->
                ProductGrid(
                    products = products,
                    favoriteIds = products.mapTo(mutableSetOf(), ProductCardUiModel::id),
                    onProductClick = onProductClick,
                    onFavoriteClick = { id, _ -> onFavoriteToggle(id) },
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                )
            },
        )
    }
}

@Composable
private fun FavoritesContent.toUiState(): FsUiState<List<ProductCardUiModel>> = when (this) {
    FavoritesContent.Loading -> FsUiState.Loading
    FavoritesContent.Empty -> FsUiState.Empty
    is FavoritesContent.Ready -> FsUiState.Content(products)
}

@Composable
private fun CenteredBlock(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(FakeStoreTheme.spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private val PreviewProducts = listOf(
    ProductCardUiModel(
        id = 1,
        title = "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
        category = "men's clothing",
        price = 109.95,
        rate = 3.9,
        ratingCount = 120,
        imageUrl = null,
    ),
    ProductCardUiModel(
        id = 5,
        title = "John Hardy Women's Legends Naga Gold & Silver Bracelet",
        category = "jewelery",
        price = 695.0,
        rate = 4.6,
        ratingCount = 400,
        imageUrl = null,
    ),
)

@Composable
private fun FavoritesScreenPreview(state: FavoritesUiState) {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            FavoritesScreen(
                state = state,
                onProductClick = {},
                onBackClick = {},
                onFavoriteToggle = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun FavoritesScreenLoadingPreview() {
    FavoritesScreenPreview(FavoritesUiState(content = FavoritesContent.Loading))
}

@PreviewLightDark
@Composable
private fun FavoritesScreenReadyPreview() {
    FavoritesScreenPreview(FavoritesUiState(content = FavoritesContent.Ready(PreviewProducts)))
}

@PreviewLightDark
@Composable
private fun FavoritesScreenEmptyPreview() {
    FavoritesScreenPreview(FavoritesUiState(content = FavoritesContent.Empty))
}
