package cl.gus.labs.fakestore.core.designsystem.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.core.designsystem.molecule.ProductCard
import cl.gus.labs.fakestore.core.designsystem.molecule.ProductCardSkeleton
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme

private const val GridColumns = 2
private const val DefaultSkeletonCount = 6

@Composable
fun ProductGrid(
    products: List<ProductCardUiModel>,
    favoriteIds: Set<Int>,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    savingFavoriteIds: Set<Int> = emptySet(),
) {
    ProductGridLayout(modifier = modifier, state = state) {
        items(products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                isFavorite = product.id in favoriteIds,
                onClick = { onProductClick(product.id) },
                onFavoriteClick = { checked -> onFavoriteClick(product.id, checked) },
                modifier = Modifier.fillMaxWidth(),
                savingFavorite = product.id in savingFavoriteIds,
            )
        }
    }
}

@Composable
fun ProductGridSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = DefaultSkeletonCount,
) {
    ProductGridLayout(modifier = modifier, userScrollEnabled = false) {
        items(count = itemCount) {
            ProductCardSkeleton(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ProductGridLayout(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    userScrollEnabled: Boolean = true,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(GridColumns),
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(FakeStoreTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(FakeStoreTheme.spacing.md),
        userScrollEnabled = userScrollEnabled,
        content = content,
    )
}

private val SampleProducts = listOf(
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
        id = 3,
        title = "John Hardy Women's Legends Naga Gold & Silver Bracelet",
        category = "jewelery",
        price = 695.0,
        rate = 4.6,
        ratingCount = 400,
        imageUrl = null,
    ),
    ProductCardUiModel(
        id = 4,
        title = "WD 2TB Elements Portable External Hard Drive",
        category = "electronics",
        price = 64.0,
        rate = 3.3,
        ratingCount = 203,
        imageUrl = null,
    ),
)

@PreviewLightDark
@Composable
private fun ProductGridPreview() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductGrid(
                products = SampleProducts,
                favoriteIds = setOf(2),
                onProductClick = {},
                onFavoriteClick = { _, _ -> },
                modifier = Modifier.fillMaxSize(),
                savingFavoriteIds = setOf(3),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProductGridSkeletonPreview() {
    FakeStoreTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductGridSkeleton(modifier = Modifier.fillMaxSize())
        }
    }
}