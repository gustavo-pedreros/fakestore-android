package cl.gus.labs.fakestore.favorites.ui.favorites

import androidx.compose.runtime.Immutable
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel

@Immutable
internal data class FavoritesUiState(
    val content: FavoritesContent = FavoritesContent.Loading,
)

@Immutable
internal sealed interface FavoritesContent {

    data object Loading : FavoritesContent

    data object Empty : FavoritesContent

    data class Ready(val products: List<ProductCardUiModel>) : FavoritesContent
}
