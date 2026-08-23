package cl.gus.labs.fakestore.catalog.data.mapper

import cl.gus.labs.fakestore.catalog.data.dto.ProductDto
import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.catalog.domain.model.Rating
import cl.gus.labs.fakestore.core.database.entity.ProductEntity
import cl.gus.labs.fakestore.shared.kernel.ProductId

internal fun ProductDto.toEntity(): ProductEntity = ProductEntity(
    id = id,
    title = title,
    price = price,
    description = description,
    category = category,
    imageUrl = imageUrl,
    ratingRate = rating.rate,
    ratingCount = rating.count,
)

internal fun ProductEntity.toDomain(): Product = Product(
    id = ProductId(id),
    title = title,
    price = price,
    description = description,
    category = Category(category),
    imageUrl = imageUrl,
    rating = Rating(rate = ratingRate, count = ratingCount),
)
