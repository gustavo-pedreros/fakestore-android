package cl.gus.labs.fakestore.catalog.ui.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import cl.gus.labs.fakestore.catalog.ui.catalog.CatalogScreen
import cl.gus.labs.fakestore.catalog.ui.detail.ProductDetailScreen
import cl.gus.labs.fakestore.catalog.ui.detail.ProductDetailViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

@Serializable
data object CatalogKey : NavKey

@Serializable
data class ProductDetailKey(val productId: Int) : NavKey

fun EntryProviderScope<NavKey>.catalogEntries(
    onProductClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFavoritesClick: () -> Unit,
) {
    entry<CatalogKey> {
        CatalogScreen(
            onProductClick = onProductClick,
            onFavoritesClick = onFavoritesClick,
        )
    }
    entry<ProductDetailKey> { key ->
        ProductDetailScreen(
            onBackClick = onBackClick,
            viewModel = hiltViewModel<ProductDetailViewModel, ProductDetailViewModel.Factory>(
                creationCallback = { factory -> factory.create(key.productId) },
            ),
        )
    }
}

fun PolymorphicModuleBuilder<NavKey>.catalogNavKeys() {
    subclass(CatalogKey::class)
    subclass(ProductDetailKey::class)
}
