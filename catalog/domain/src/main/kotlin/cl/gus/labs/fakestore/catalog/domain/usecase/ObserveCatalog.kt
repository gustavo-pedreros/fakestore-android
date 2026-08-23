package cl.gus.labs.fakestore.catalog.domain.usecase

import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import kotlinx.coroutines.flow.Flow

fun interface ObserveCatalog {
    operator fun invoke(category: Category?): Flow<List<Product>>
}
