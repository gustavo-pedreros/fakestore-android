package cl.gus.labs.fakestore.favorites.data.datasource

import cl.gus.labs.fakestore.core.database.dao.FavoriteDao
import cl.gus.labs.fakestore.core.database.entity.FavoriteEntity
import cl.gus.labs.fakestore.core.database.entity.SyncState
import javax.inject.Inject
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

internal class FavoritesLocalDataSourceImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
) : FavoritesLocalDataSource {

    override fun observeIds(): Flow<List<Int>> = favoriteDao.observeIds().distinctUntilChanged()

    override suspend fun toggle(productId: Int, at: Instant) = favoriteDao.toggle(
        FavoriteEntity(productId = productId, updatedAt = at.toEpochMilliseconds(), syncState = SyncState.LOCAL_ONLY),
    )
}
