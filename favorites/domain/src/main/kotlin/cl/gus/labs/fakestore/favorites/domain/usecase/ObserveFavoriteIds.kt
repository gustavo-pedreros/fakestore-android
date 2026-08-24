package cl.gus.labs.fakestore.favorites.domain.usecase

import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlinx.coroutines.flow.Flow

fun interface ObserveFavoriteIds {
    operator fun invoke(): Flow<Set<ProductId>>
}
