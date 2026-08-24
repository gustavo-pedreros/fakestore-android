package cl.gus.labs.fakestore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import cl.gus.labs.fakestore.catalog.ui.navigation.CatalogKey
import cl.gus.labs.fakestore.catalog.ui.navigation.ProductDetailKey
import cl.gus.labs.fakestore.catalog.ui.navigation.catalogEntries
import cl.gus.labs.fakestore.catalog.ui.navigation.catalogNavKeys
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val NavKeyConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) { catalogNavKeys() }
    }
}

@Composable
fun FakeStoreNavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(NavKeyConfiguration, CatalogKey)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator<NavKey>(),
        ),
        entryProvider = entryProvider {
            catalogEntries(
                onProductClick = { productId -> backStack.add(ProductDetailKey(productId)) },
                onBackClick = { backStack.removeLastOrNull() },
                onFavoritesClick = { },
            )
        },
    )
}
