package cl.gus.labs.fakestore.core.designsystem.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.R
import cl.gus.labs.fakestore.core.designsystem.atom.FsCategoryBadge
import cl.gus.labs.fakestore.core.designsystem.atom.FsFavoriteButton
import cl.gus.labs.fakestore.core.designsystem.atom.FsImageTile
import cl.gus.labs.fakestore.core.designsystem.atom.FsPriceText
import cl.gus.labs.fakestore.core.designsystem.atom.FsRatingStars
import cl.gus.labs.fakestore.core.designsystem.atom.FsSize
import cl.gus.labs.fakestore.core.designsystem.atom.FsSkeleton
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

private val TileInset = 26.dp
private const val TitleLines = 2

@Composable
fun ProductCard(
    product: ProductCardUiModel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    savingFavorite: Boolean = false,
) {
    CardSurface(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
    ) {
        Box {
            FsImageTile(
                url = product.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = TileInset,
                showBorder = false,
            )
            FsFavoriteButton(
                checked = isFavorite,
                onCheckedChange = onFavoriteClick,
                contentDescription = stringResource(
                    if (isFavorite) R.string.fs_favorite_remove else R.string.fs_favorite_add,
                ),
                modifier = Modifier.align(Alignment.TopEnd),
                saving = savingFavorite,
                size = FsSize.Compact,
            )
        }
        TileDivider()
        Column(
            modifier = Modifier.padding(FakeStoreTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
        ) {
            FsCategoryBadge(product.category, size = FsSize.Compact)
            Text(
                text = product.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                minLines = TitleLines,
                maxLines = TitleLines,
                overflow = TextOverflow.Ellipsis,
            )
            FsRatingStars(
                rate = product.rate,
                count = product.ratingCount,
                size = FsSize.Compact,
            )
            FsPriceText(product.price)
        }
    }
}

@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    CardSurface(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(FakeStoreTheme.colors.skeleton),
        )
        TileDivider()
        Column(
            modifier = Modifier.padding(FakeStoreTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.sm),
        ) {
            FsSkeleton(
                Modifier
                    .fillMaxWidth(0.62f)
                    .height(20.dp),
            )
            FsSkeleton(
                Modifier
                    .fillMaxWidth()
                    .height(14.dp),
            )
            FsSkeleton(
                Modifier
                    .fillMaxWidth(0.74f)
                    .height(14.dp),
            )
            FsSkeleton(
                Modifier
                    .fillMaxWidth(0.55f)
                    .height(14.dp),
            )
            FsSkeleton(
                Modifier
                    .fillMaxWidth(0.48f)
                    .height(20.dp),
            )
        }
    }
}

@Composable
private fun CardSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .clip(shape),
    ) {
        content()
    }
}

@Composable
private fun TileDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FakeStoreTheme.colors.imageTileBorder),
    )
}

private val SampleProduct = ProductCardUiModel(
    id = 1,
    title = "Fjallraven - Foldsack No. 1 Backpack, Fits 15 Laptops",
    category = "men's clothing",
    price = 109.95,
    rate = 3.9,
    ratingCount = 120,
    imageUrl = null,
)

private val PreviewCardWidth = 173.dp

@PreviewLightDark
@Composable
private fun ProductCardPreview() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(FakeStoreTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.md),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.md)) {
                    ProductCard(
                        product = SampleProduct,
                        isFavorite = false,
                        onClick = {},
                        onFavoriteClick = {},
                        modifier = Modifier.width(PreviewCardWidth),
                    )
                    ProductCard(
                        product = SampleProduct.copy(
                            id = 2,
                            title = "John Hardy Naga",
                            category = "jewelery",
                            price = 695.0,
                            rate = 4.6,
                            ratingCount = 400,
                        ),
                        isFavorite = true,
                        onClick = {},
                        onFavoriteClick = {},
                        modifier = Modifier.width(PreviewCardWidth),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.md)) {
                    ProductCard(
                        product = SampleProduct.copy(
                            id = 3,
                            title = "Samsung 49-Inch CHG90 144Hz Curved Gaming Monitor",
                            category = "electronics",
                            price = 1099.95,
                            rate = 2.2,
                            ratingCount = 1000,
                        ),
                        isFavorite = true,
                        onClick = {},
                        onFavoriteClick = {},
                        modifier = Modifier.width(PreviewCardWidth),
                        savingFavorite = true,
                    )
                    ProductCardSkeleton(modifier = Modifier.width(PreviewCardWidth))
                }
            }
        }
    }
}
