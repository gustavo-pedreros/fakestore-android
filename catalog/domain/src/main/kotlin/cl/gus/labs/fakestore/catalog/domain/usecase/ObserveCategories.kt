package cl.gus.labs.fakestore.catalog.domain.usecase

import cl.gus.labs.fakestore.catalog.domain.CatalogRepository
import cl.gus.labs.fakestore.catalog.domain.model.Category
import cl.gus.labs.fakestore.catalog.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveCategories(private val repository: CatalogRepository) {
    operator fun invoke(): Flow<List<Category>> =
        repository.observeAll(category = null).map { products ->
            products.map(Product::category).distinct().sortedBy(Category::value)
        }
}
