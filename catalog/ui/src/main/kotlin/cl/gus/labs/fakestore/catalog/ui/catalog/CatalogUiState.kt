package cl.gus.labs.fakestore.catalog.ui.catalog

import androidx.compose.runtime.Immutable
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.shared.kernel.AppError
import kotlin.time.Instant

@Immutable
internal data class CatalogUiState(
    val content: CatalogContent = CatalogContent.Loading,
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val favoriteIds: Set<Int> = emptySet(),
    val lastSyncedAt: Instant? = null,
    val isStale: Boolean = false,
    val isRefreshing: Boolean = false,
)

@Immutable
internal sealed interface CatalogContent {

    data object Loading : CatalogContent

    data object Empty : CatalogContent

    data class Ready(val products: List<ProductCardUiModel>) : CatalogContent

    data class Failure(val error: AppError, val offline: Boolean) : CatalogContent
}
