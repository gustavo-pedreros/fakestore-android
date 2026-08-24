package cl.gus.labs.fakestore.favorites.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.usecase.ObserveCatalog
import cl.gus.labs.fakestore.favorites.domain.usecase.ObserveFavoriteIds
import cl.gus.labs.fakestore.favorites.domain.usecase.ToggleFavorite
import cl.gus.labs.fakestore.favorites.ui.mapper.toCard
import cl.gus.labs.fakestore.shared.kernel.ProductId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val StopTimeoutMillis = 5_000L

@HiltViewModel
internal class FavoritesViewModel @Inject constructor(
    observeCatalog: ObserveCatalog,
    observeFavoriteIds: ObserveFavoriteIds,
    private val toggleFavorite: ToggleFavorite,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = combine(
        observeCatalog(null),
        observeFavoriteIds(),
    ) { products, favoriteIds ->
        FavoritesUiState(content = reduce(products, favoriteIds))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(StopTimeoutMillis),
        initialValue = FavoritesUiState(),
    )

    fun onFavoriteToggle(productId: Int) {
        viewModelScope.launch { toggleFavorite(ProductId(productId)) }
    }
}

private fun reduce(products: List<Product>, favoriteIds: Set<ProductId>): FavoritesContent {
    val favorites = products.filter { it.id in favoriteIds }
    return if (favorites.isEmpty()) {
        FavoritesContent.Empty
    } else {
        FavoritesContent.Ready(favorites.map(Product::toCard))
    }
}
