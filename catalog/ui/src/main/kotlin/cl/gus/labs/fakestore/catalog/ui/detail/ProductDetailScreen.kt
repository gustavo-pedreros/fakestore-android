package cl.gus.labs.fakestore.catalog.ui.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.gus.labs.fakestore.catalog.ui.R
import cl.gus.labs.fakestore.catalog.ui.component.CenteredBlock
import cl.gus.labs.fakestore.catalog.ui.component.staleMessage
import cl.gus.labs.fakestore.catalog.ui.error.appErrorStrings
import cl.gus.labs.fakestore.core.designsystem.atom.FsButton
import cl.gus.labs.fakestore.core.designsystem.atom.FsSkeleton
import cl.gus.labs.fakestore.core.designsystem.icon.FsIcons
import cl.gus.labs.fakestore.core.designsystem.model.ProductDetailUiModel
import cl.gus.labs.fakestore.core.designsystem.molecule.FsDetailTopBar
import cl.gus.labs.fakestore.core.designsystem.molecule.FsStateBlock
import cl.gus.labs.fakestore.core.designsystem.molecule.FsStatusBanner
import cl.gus.labs.fakestore.core.designsystem.organism.ProductDetailBody
import cl.gus.labs.fakestore.core.designsystem.organism.ProductDetailHeader
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import cl.gus.labs.fakestore.shared.kernel.AppError
import kotlin.time.Instant

private val TitleRevealOffset = 240.dp
private val SkeletonHeaderHeight = 300.dp
private val SkeletonTitleHeight = 24.dp
private val SkeletonLineHeight = 12.dp

@Composable
internal fun ProductDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ProductDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ProductDetailScreen(
        state = state,
        onBackClick = onBackClick,
        onRetryClick = viewModel::refresh,
        onFavoriteToggle = viewModel::onFavoriteToggle,
        modifier = modifier,
    )
}

@Composable
internal fun ProductDetailScreen(
    state: ProductDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val revealPx = with(LocalDensity.current) { TitleRevealOffset.toPx() }
    val titleVisible by remember { derivedStateOf { scrollState.value > revealPx } }
    val content = state.content

    Column(modifier = modifier.fillMaxSize()) {
        FsDetailTopBar(
            title = (content as? ProductDetailContent.Ready)?.product?.title.orEmpty(),
            titleVisible = titleVisible && content is ProductDetailContent.Ready,
            onBackClick = onBackClick,
        )
        FsStatusBanner(
            visible = state.isStale,
            message = staleMessage(state.lastSyncedAt),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when (content) {
                ProductDetailContent.Loading -> ProductDetailSkeleton()

                ProductDetailContent.Unavailable -> CenteredBlock {
                    FsStateBlock(
                        icon = FsIcons.Box,
                        title = stringResource(R.string.catalog_detail_unavailable_title),
                        modifier = Modifier.fillMaxWidth(),
                        body = stringResource(R.string.catalog_detail_unavailable_body),
                    )
                }

                is ProductDetailContent.Failure -> CenteredBlock {
                    FsStateBlock(
                        icon = FsIcons.Alert,
                        title = stringResource(detailErrorTitle(content.offline)),
                        modifier = Modifier.fillMaxWidth(),
                        body = stringResource(detailErrorBody(content.error, content.offline)),
                        iconTint = FakeStoreTheme.colors.statusError,
                        action = {
                            FsButton(
                                text = stringResource(R.string.catalog_action_retry),
                                onClick = onRetryClick,
                            )
                        },
                    )
                }

                is ProductDetailContent.Ready -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                ) {
                    ProductDetailHeader(
                        imageUrl = content.product.imageUrl,
                        isFavorite = state.isFavorite,
                        onFavoriteClick = { onFavoriteToggle() },
                    )
                    ProductDetailBody(product = content.product)
                }
            }
        }
    }
}

@StringRes
private fun detailErrorTitle(offline: Boolean): Int =
    if (offline) R.string.catalog_error_offline_title else R.string.catalog_detail_error_title

@StringRes
private fun detailErrorBody(error: AppError, offline: Boolean): Int =
    if (offline) {
        R.string.catalog_detail_error_offline_body
    } else {
        appErrorStrings(error, offline = false).body
    }

@Composable
private fun ProductDetailSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.md),
    ) {
        FsSkeleton(
            Modifier
                .fillMaxWidth()
                .height(SkeletonHeaderHeight),
        )
        Column(
            modifier = Modifier.padding(
                horizontal = FakeStoreTheme.spacing.lg,
                vertical = FakeStoreTheme.spacing.sm,
            ),
            verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.md),
        ) {
            FsSkeleton(
                Modifier
                    .fillMaxWidth(0.35f)
                    .height(SkeletonLineHeight),
            )
            FsSkeleton(
                Modifier
                    .fillMaxWidth()
                    .height(SkeletonTitleHeight),
            )
            FsSkeleton(
                Modifier
                    .fillMaxWidth(0.28f)
                    .height(SkeletonTitleHeight),
            )
            FsSkeleton(
                Modifier
                    .fillMaxWidth()
                    .height(SkeletonLineHeight),
            )
            FsSkeleton(
                Modifier
                    .fillMaxWidth(0.72f)
                    .height(SkeletonLineHeight),
            )
        }
    }
}

private val PreviewProduct = ProductDetailUiModel(
    id = 1,
    title = "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
    category = "men's clothing",
    description = "Your perfect pack for everyday use and walks in the forest. " +
        "Stash your laptop (up to 15 inches) in the padded sleeve, your everyday.",
    price = 109.95,
    rate = 3.9,
    ratingCount = 120,
    imageUrl = null,
)

private val PreviewSyncedAt = Instant.parse("2026-08-23T18:30:00Z")

@Composable
private fun ProductDetailScreenPreview(state: ProductDetailUiState) {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductDetailScreen(
                state = state,
                onBackClick = {},
                onRetryClick = {},
                onFavoriteToggle = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProductDetailScreenReadyPreview() {
    ProductDetailScreenPreview(
        ProductDetailUiState(
            content = ProductDetailContent.Ready(PreviewProduct),
            lastSyncedAt = PreviewSyncedAt,
        ),
    )
}

@PreviewLightDark
@Composable
private fun ProductDetailScreenStalePreview() {
    ProductDetailScreenPreview(
        ProductDetailUiState(
            content = ProductDetailContent.Ready(PreviewProduct),
            lastSyncedAt = PreviewSyncedAt,
            isStale = true,
        ),
    )
}

@PreviewLightDark
@Composable
private fun ProductDetailScreenLoadingPreview() {
    ProductDetailScreenPreview(ProductDetailUiState(content = ProductDetailContent.Loading))
}

@PreviewLightDark
@Composable
private fun ProductDetailScreenUnavailablePreview() {
    ProductDetailScreenPreview(
        ProductDetailUiState(
            content = ProductDetailContent.Unavailable,
            lastSyncedAt = PreviewSyncedAt,
        ),
    )
}

@PreviewLightDark
@Composable
private fun ProductDetailScreenFailurePreview() {
    ProductDetailScreenPreview(
        ProductDetailUiState(
            content = ProductDetailContent.Failure(AppError.Network(message = null), offline = true),
        ),
    )
}
