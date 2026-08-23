package cl.gus.labs.fakestore.core.designsystem.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import cl.gus.labs.fakestore.core.designsystem.R
import cl.gus.labs.fakestore.core.designsystem.atom.FsCategoryBadge
import cl.gus.labs.fakestore.core.designsystem.atom.FsDivider
import cl.gus.labs.fakestore.core.designsystem.atom.FsDividerWeight
import cl.gus.labs.fakestore.core.designsystem.atom.FsFavoriteButton
import cl.gus.labs.fakestore.core.designsystem.atom.FsImageTile
import cl.gus.labs.fakestore.core.designsystem.atom.FsPriceSize
import cl.gus.labs.fakestore.core.designsystem.atom.FsPriceText
import cl.gus.labs.fakestore.core.designsystem.atom.FsRatingStars
import cl.gus.labs.fakestore.core.designsystem.model.ProductDetailUiModel
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

private val HeaderHeight = 300.dp
private val HeaderInset = 44.dp
private val FavoriteInset = 8.dp
private val BodyTopPadding = 20.dp

@Composable
fun ProductDetailHeader(
    imageUrl: String?,
    isFavorite: Boolean,
    onFavoriteClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    savingFavorite: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box {
            FsImageTile(
                url = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HeaderHeight),
                ratio = null,
                contentPadding = HeaderInset,
                showBorder = false,
            )
            FsFavoriteButton(
                checked = isFavorite,
                onCheckedChange = onFavoriteClick,
                contentDescription = stringResource(
                    if (isFavorite) R.string.fs_favorite_remove else R.string.fs_favorite_add,
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(FavoriteInset),
                saving = savingFavorite,
            )
        }
        TileHairline()
    }
}

@Composable
fun ProductDetailBody(
    product: ProductDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = FakeStoreTheme.spacing.lg,
                end = FakeStoreTheme.spacing.lg,
                top = BodyTopPadding,
                bottom = FakeStoreTheme.spacing.xxl,
            ),
        verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FsCategoryBadge(product.category)
            FsRatingStars(rate = product.rate, count = product.ratingCount)
        }
        Text(
            text = product.title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
        )
        FsPriceText(product.price, size = FsPriceSize.Large)
        FsDivider(weight = FsDividerWeight.Strong)
        Text(
            text = product.description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun TileHairline() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(FakeStoreTheme.colors.imageTileBorder),
    )
}

private val SampleDetail = ProductDetailUiModel(
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

@PreviewLightDark
@Composable
private fun ProductDetailPreview() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                ProductDetailHeader(
                    imageUrl = SampleDetail.imageUrl,
                    isFavorite = true,
                    onFavoriteClick = {},
                )
                ProductDetailBody(product = SampleDetail)
            }
        }
    }
}