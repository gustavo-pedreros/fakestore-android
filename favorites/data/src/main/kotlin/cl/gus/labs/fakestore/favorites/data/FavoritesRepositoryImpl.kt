package cl.gus.labs.fakestore.favorites.data

import cl.gus.labs.fakestore.favorites.data.datasource.FavoritesLocalDataSource
import cl.gus.labs.fakestore.favorites.domain.FavoritesRepository
import cl.gus.labs.fakestore.shared.kernel.ProductId
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class FavoritesRepositoryImpl @Inject constructor(
    private val local: FavoritesLocalDataSource,
    private val clock: Clock,
) : FavoritesRepository {

    override fun observeIds(): Flow<Set<ProductId>> =
        local.observeIds().map { ids -> ids.mapTo(mutableSetOf(), ::ProductId) }

    override suspend fun toggle(id: ProductId) = local.toggle(id.value, clock.now())
}
