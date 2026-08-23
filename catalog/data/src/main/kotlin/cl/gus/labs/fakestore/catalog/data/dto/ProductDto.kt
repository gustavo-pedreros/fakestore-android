package cl.gus.labs.fakestore.catalog.data.dto

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
internal data class ProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    @SerialName("image") val imageUrl: String,
    val rating: RatingDto,
)

@OptIn(InternalSerializationApi::class)
@Serializable
internal data class RatingDto(
    val rate: Double,
    val count: Int,
)
