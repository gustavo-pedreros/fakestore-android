package cl.gus.labs.fakestore.core.database.dao

import androidx.room.Room
import cl.gus.labs.fakestore.core.database.FakeStoreDatabase
import cl.gus.labs.fakestore.core.database.entity.SyncMetadataEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SyncMetadataDaoTest {
    private val databaseName = "sync-metadata-dao-test-${System.nanoTime()}.db"
    private lateinit var database: FakeStoreDatabase
    private lateinit var dao: SyncMetadataDao

    private val metadata = SyncMetadataEntity(scope = "catalog", lastSyncedAt = 1_700_000_000_000L)

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.databaseBuilder(context, FakeStoreDatabase::class.java, databaseName).build()
        dao = database.syncMetadataDao()
    }

    @After
    fun tearDown() {
        database.close()
        RuntimeEnvironment.getApplication().deleteDatabase(databaseName)
    }

    @Test
    fun `observeByScope returns null when nothing was synced yet`() = runTest {
        assertNull(dao.observeByScope("catalog").first())
    }

    @Test
    fun `upsert then observeByScope emits what was written`() = runTest {
        dao.upsert(metadata)

        assertEquals(metadata, dao.observeByScope("catalog").first())
    }

    @Test
    fun `upsert replaces the row for the same scope`() = runTest {
        dao.upsert(metadata)
        val updated = metadata.copy(lastSyncedAt = 1_800_000_000_000L)

        dao.upsert(updated)

        assertEquals(updated, dao.observeByScope("catalog").first())
    }

    @Test
    fun `observeByScope does not see rows from another scope`() = runTest {
        dao.upsert(metadata)

        assertNull(dao.observeByScope("favorites").first())
    }
}
