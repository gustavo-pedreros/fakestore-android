package cl.gus.labs.fakestore.favorites.domain

import cl.gus.labs.fakestore.shared.kernel.ProductId
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {

    fun observeIds(): Flow<Set<ProductId>>

    suspend fun toggle(id: ProductId)
}
