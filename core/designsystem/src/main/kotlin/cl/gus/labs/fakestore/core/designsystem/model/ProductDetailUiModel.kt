package cl.gus.labs.fakestore.core.designsystem.model

import androidx.compose.runtime.Immutable

@Immutable
data class ProductDetailUiModel(
    val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val price: Double,
    val rate: Double,
    val ratingCount: Int,
    val imageUrl: String?,
)
