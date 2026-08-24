package cl.gus.labs.fakestore.catalog.ui.catalog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.gus.labs.fakestore.catalog.ui.R
import cl.gus.labs.fakestore.catalog.ui.component.CenteredBlock
import cl.gus.labs.fakestore.catalog.ui.component.staleMessage
import cl.gus.labs.fakestore.catalog.ui.error.appErrorStrings
import cl.gus.labs.fakestore.core.designsystem.atom.FsButton
import cl.gus.labs.fakestore.core.designsystem.atom.FsButtonVariant
import cl.gus.labs.fakestore.core.designsystem.icon.FsIcons
import cl.gus.labs.fakestore.core.designsystem.model.FsUiState
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.core.designsystem.molecule.CategoryFilterRow
import cl.gus.labs.fakestore.core.designsystem.molecule.FsListTopBar
import cl.gus.labs.fakestore.core.designsystem.molecule.FsStateBlock
import cl.gus.labs.fakestore.core.designsystem.molecule.FsStatusBanner
import cl.gus.labs.fakestore.core.designsystem.organism.FsStateHost
import cl.gus.labs.fakestore.core.designsystem.organism.ProductGrid
import cl.gus.labs.fakestore.core.designsystem.organism.ProductGridSkeleton
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import cl.gus.labs.fakestore.shared.kernel.AppError
import kotlin.time.Instant

@Composable
internal fun CatalogScreen(
    onProductClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentErrorMessage by rememberUpdatedState(catalogErrorMessage())

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.errorEvents.collect { error ->
            snackbarHostState.showSnackbar(currentErrorMessage(error))
        }
    }

    CatalogScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onProductClick = onProductClick,
        onFavoritesClick = onFavoritesClick,
        onCategorySelect = viewModel::onCategorySelect,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogScreen(
    state: CatalogUiState,
    snackbarHostState: SnackbarHostState,
    onProductClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit,
    onCategorySelect: (String?) -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()

    Column(modifier = modifier.fillMaxSize()) {
        FsListTopBar(onFavoritesClick = onFavoritesClick)
        FsStatusBanner(
            visible = state.isStale,
            message = staleMessage(state.lastSyncedAt),
        )
        CategoryFilterRow(
            categories = state.categories,
            selected = state.selectedCategory,
            onSelect = onCategorySelect,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                FsStateHost(
                    state = state.content.toUiState(),
                    loading = { ProductGridSkeleton(modifier = Modifier.fillMaxSize()) },
                    empty = {
                        CatalogEmptyBlock(
                            selectedCategory = state.selectedCategory,
                            onShowAllClick = { onCategorySelect(null) },
                        )
                    },
                    failure = { body ->
                        CatalogFailureBlock(
                            titleRes = failureTitleRes(state.content),
                            body = body,
                            onRetryClick = onRefresh,
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    content = { products ->
                        ProductGrid(
                            products = products,
                            favoriteIds = state.favoriteIds,
                            onProductClick = onProductClick,
                            onFavoriteClick = { id, _ -> onFavoriteToggle(id) },
                            modifier = Modifier.fillMaxSize(),
                            state = gridState,
                        )
                    },
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(FakeStoreTheme.spacing.lg),
            )
        }
    }
}

@Composable
private fun catalogErrorMessage(): (AppError) -> String {
    val networkBody = stringResource(R.string.catalog_error_network_body)
    val serverBody = stringResource(R.string.catalog_error_server_body)
    val unknownBody = stringResource(R.string.catalog_error_unknown_body)
    return { error ->
        when (error) {
            is AppError.Network -> networkBody
            is AppError.Http, AppError.EmptyBody -> serverBody
            is AppError.Unknown -> unknownBody
        }
    }
}

@Composable
private fun CatalogContent.toUiState(): FsUiState<List<ProductCardUiModel>> = when (this) {
    CatalogContent.Loading -> FsUiState.Loading
    CatalogContent.Empty -> FsUiState.Empty
    is CatalogContent.Ready -> FsUiState.Content(products)
    is CatalogContent.Failure -> FsUiState.Failure(
        stringResource(appErrorStrings(error, offline).body),
    )
}

@StringRes
private fun failureTitleRes(content: CatalogContent): Int =
    if (content is CatalogContent.Failure) {
        appErrorStrings(content.error, content.offline).title
    } else {
        R.string.catalog_error_unknown_title
    }

@Composable
private fun CatalogEmptyBlock(
    selectedCategory: String?,
    onShowAllClick: () -> Unit,
) {
    CenteredBlock {
        if (selectedCategory == null) {
            FsStateBlock(
                icon = FsIcons.Box,
                title = stringResource(R.string.catalog_empty_title),
                modifier = Modifier.fillMaxWidth(),
                body = stringResource(R.string.catalog_empty_body),
            )
        } else {
            FsStateBlock(
                icon = FsIcons.Box,
                title = stringResource(R.string.catalog_empty_category_title),
                modifier = Modifier.fillMaxWidth(),
                body = stringResource(R.string.catalog_empty_category_body),
                action = {
                    FsButton(
                        text = stringResource(R.string.catalog_action_show_all),
                        onClick = onShowAllClick,
                        variant = FsButtonVariant.Secondary,
                    )
                },
            )
        }
    }
}

@Composable
private fun CatalogFailureBlock(
    @StringRes titleRes: Int,
    body: String,
    onRetryClick: () -> Unit,
) {
    CenteredBlock {
        FsStateBlock(
            icon = FsIcons.Alert,
            title = stringResource(titleRes),
            modifier = Modifier.fillMaxWidth(),
            body = body,
            iconTint = FakeStoreTheme.colors.statusError,
            action = {
                FsButton(
                    text = stringResource(R.string.catalog_action_retry),
                    onClick = onRetryClick,
                )
            },
        )
    }
}

private val PreviewCategories = listOf(
    "men's clothing",
    "jewelery",
    "electronics",
    "women's clothing",
)

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
        id = 2,
        title = "Mens Casual Premium Slim Fit T-Shirts",
        category = "men's clothing",
        price = 22.3,
        rate = 4.1,
        ratingCount = 259,
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
    ProductCardUiModel(
        id = 9,
        title = "WD 2TB Elements Portable External Hard Drive",
        category = "electronics",
        price = 64.0,
        rate = 3.3,
        ratingCount = 203,
        imageUrl = null,
    ),
)

private val PreviewSyncedAt = Instant.parse("2026-08-23T18:30:00Z")

@Composable
private fun CatalogScreenPreview(state: CatalogUiState) {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CatalogScreen(
                state = state,
                snackbarHostState = remember { SnackbarHostState() },
                onProductClick = {},
                onFavoritesClick = {},
                onCategorySelect = {},
                onFavoriteToggle = {},
                onRefresh = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CatalogScreenLoadingPreview() {
    CatalogScreenPreview(CatalogUiState(content = CatalogContent.Loading))
}

@PreviewLightDark
@Composable
private fun CatalogScreenReadyPreview() {
    CatalogScreenPreview(
        CatalogUiState(
            content = CatalogContent.Ready(PreviewProducts),
            categories = PreviewCategories,
            lastSyncedAt = PreviewSyncedAt,
        ),
    )
}

@PreviewLightDark
@Composable
private fun CatalogScreenStalePreview() {
    CatalogScreenPreview(
        CatalogUiState(
            content = CatalogContent.Ready(PreviewProducts),
            categories = PreviewCategories,
            lastSyncedAt = PreviewSyncedAt,
            isStale = true,
        ),
    )
}

@PreviewLightDark
@Composable
private fun CatalogScreenEmptyPreview() {
    CatalogScreenPreview(
        CatalogUiState(
            content = CatalogContent.Empty,
            categories = PreviewCategories,
            selectedCategory = "jewelery",
            lastSyncedAt = PreviewSyncedAt,
        ),
    )
}

@PreviewLightDark
@Composable
private fun CatalogScreenFailurePreview() {
    CatalogScreenPreview(
        CatalogUiState(
            content = CatalogContent.Failure(AppError.Network(message = null), offline = true),
        ),
    )
}
