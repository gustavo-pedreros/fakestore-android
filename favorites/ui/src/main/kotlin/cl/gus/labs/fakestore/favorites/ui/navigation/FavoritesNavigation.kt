package cl.gus.labs.fakestore.favorites.ui.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import cl.gus.labs.fakestore.favorites.ui.favorites.FavoritesScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

@Serializable
data object FavoritesKey : NavKey

fun EntryProviderScope<NavKey>.favoritesEntries(
    onProductClick: (Int) -> Unit,
    onBackClick: () -> Unit,
) {
    entry<FavoritesKey> {
        FavoritesScreen(
            onProductClick = onProductClick,
            onBackClick = onBackClick,
        )
    }
}

fun PolymorphicModuleBuilder<NavKey>.favoritesNavKeys() {
    subclass(FavoritesKey::class)
}
