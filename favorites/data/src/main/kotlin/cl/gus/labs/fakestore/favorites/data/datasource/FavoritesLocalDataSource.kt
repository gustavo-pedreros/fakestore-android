package cl.gus.labs.fakestore.favorites.data.datasource

import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

internal interface FavoritesLocalDataSource {
    fun observeIds(): Flow<List<Int>>
    suspend fun toggle(productId: Int, at: Instant)
}
