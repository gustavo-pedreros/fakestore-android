package cl.gus.labs.fakestore.favorites.ui.mapper

import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel

internal fun Product.toCard(): ProductCardUiModel = ProductCardUiModel(
    id = id.value,
    title = title,
    category = category.value,
    price = price,
    rate = rating.rate,
    ratingCount = rating.count,
    imageUrl = imageUrl,
)
