package cl.gus.labs.fakestore.catalog.ui.mapper

import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.core.designsystem.model.ProductCardUiModel
import cl.gus.labs.fakestore.core.designsystem.model.ProductDetailUiModel

internal fun Product.toCard(): ProductCardUiModel = ProductCardUiModel(
    id = id.value,
    title = title,
    category = category.value,
    price = price,
    rate = rating.rate,
    ratingCount = rating.count,
    imageUrl = imageUrl,
)

internal fun Product.toDetail(): ProductDetailUiModel = ProductDetailUiModel(
    id = id.value,
    title = title,
    category = category.value,
    description = description,
    price = price,
    rate = rating.rate,
    ratingCount = rating.count,
    imageUrl = imageUrl,
)
