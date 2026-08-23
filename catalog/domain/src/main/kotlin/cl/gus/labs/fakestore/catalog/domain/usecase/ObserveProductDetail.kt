package cl.gus.labs.fakestore.catalog.domain.usecase

import cl.gus.labs.fakestore.catalog.domain.model.Product
import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlinx.coroutines.flow.Flow

fun interface ObserveProductDetail {
    operator fun invoke(id: ProductId): Flow<Product?>
}
