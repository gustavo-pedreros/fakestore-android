package cl.gus.labs.fakestore.catalog.ui.detail

import androidx.compose.runtime.Immutable
import cl.gus.labs.fakestore.core.designsystem.model.ProductDetailUiModel
import cl.gus.labs.fakestore.shared.kernel.AppError
import kotlin.time.Instant

@Immutable
internal data class ProductDetailUiState(
    val content: ProductDetailContent = ProductDetailContent.Loading,
    val isFavorite: Boolean = false,
    val lastSyncedAt: Instant? = null,
    val isStale: Boolean = false,
)

@Immutable
internal sealed interface ProductDetailContent {

    data object Loading : ProductDetailContent

    data object Unavailable : ProductDetailContent

    data class Ready(val product: ProductDetailUiModel) : ProductDetailContent

    data class Failure(val error: AppError, val offline: Boolean) : ProductDetailContent
}
