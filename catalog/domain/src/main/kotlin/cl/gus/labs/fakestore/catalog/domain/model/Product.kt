package cl.gus.labs.fakestore.catalog.domain.model

import cl.gus.labs.fakestore.shared.kernel.ProductId

data class Product(
    val id: ProductId,
    val title: String,
    val price: Double,
    val description: String,
    val category: Category,
    val imageUrl: String,
    val rating: Rating,
)
