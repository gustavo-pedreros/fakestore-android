package cl.gus.labs.fakestore.core.database.dao

import androidx.room.Room
import cl.gus.labs.fakestore.core.database.FakeStoreDatabase
import cl.gus.labs.fakestore.core.database.entity.FavoriteEntity
import cl.gus.labs.fakestore.core.database.entity.SyncState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class FavoriteDaoTest {
    private val databaseName = "favorite-dao-test-${System.nanoTime()}.db"
    private lateinit var database: FakeStoreDatabase
    private lateinit var dao: FavoriteDao

    private val favorite = FavoriteEntity(productId = 1, updatedAt = 1_000L, syncState = SyncState.LOCAL_ONLY)
    private val otherFavorite = FavoriteEntity(productId = 2, updatedAt = 2_000L, syncState = SyncState.LOCAL_ONLY)

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.databaseBuilder(context, FakeStoreDatabase::class.java, databaseName).build()
        dao = database.favoriteDao()
    }

    @After
    fun tearDown() {
        database.close()
        RuntimeEnvironment.getApplication().deleteDatabase(databaseName)
    }

    @Test
    fun `toggle inserts when the product is not a favorite yet`() = runTest {
        dao.toggle(favorite)

        assertEquals(listOf(1), dao.observeIds().first())
    }

    @Test
    fun `toggle deletes when the product is already a favorite`() = runTest {
        dao.toggle(favorite)

        dao.toggle(favorite)

        assertEquals(emptyList<Int>(), dao.observeIds().first())
    }

    @Test
    fun `observeIds orders by updatedAt descending`() = runTest {
        dao.toggle(favorite)
        dao.toggle(otherFavorite)

        assertEquals(listOf(2, 1), dao.observeIds().first())
    }

    @Test
    fun `observeIds re-emits after a toggle`() = runTest {
        dao.toggle(favorite)
        val afterInsert = dao.observeIds().first()

        dao.toggle(favorite)
        val afterDelete = dao.observeIds().first()

        assertEquals(listOf(1), afterInsert)
        assertEquals(emptyList<Int>(), afterDelete)
    }
}
