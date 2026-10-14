package cl.gus.labs.fakestore.core.storage.keyvalue.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import cl.gus.labs.fakestore.core.storage.keyvalue.booleanKey
import cl.gus.labs.fakestore.core.storage.keyvalue.doubleKey
import cl.gus.labs.fakestore.core.storage.keyvalue.intKey
import cl.gus.labs.fakestore.core.storage.keyvalue.longKey
import cl.gus.labs.fakestore.core.storage.keyvalue.stringKey
import cl.gus.labs.fakestore.core.storage.keyvalue.update
import java.io.File
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private val NameKey = stringKey("name")
private val EnabledKey = booleanKey("enabled")
private val CountKey = intKey("count")
private val SinceKey = longKey("since")
private val RatioKey = doubleKey("ratio")

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DataStoreKeyValueStoreTest {

    @get:Rule
    val tmpDir = TemporaryFolder()

    private val scope = TestScope(UnconfinedTestDispatcher())

    // On SDK 26+ (robolectric.properties) DataStore replaces the file as on a device, Windows included.
    private fun TestScope.fileStore() = DataStoreKeyValueStore(
        PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(tmpDir.root, "test.preferences_pb")
        },
    )

    @Test
    fun `observe reads back a value of each type`() = scope.runTest {
        val store = fileStore()

        store.edit { values ->
            values[NameKey] = "dark"
            values[EnabledKey] = true
            values[CountKey] = 3
            values[SinceKey] = 3L
            values[RatioKey] = 0.5
        }

        assertEquals("dark", store.observe(NameKey).first())
        assertEquals(true, store.observe(EnabledKey).first())
        assertEquals(3, store.observe(CountKey).first())
        assertEquals(3L, store.observe(SinceKey).first())
        assertEquals(0.5, store.observe(RatioKey).first())
    }

    @Test
    fun `observe reads an absent key as null`() = scope.runTest {
        assertNull(fileStore().observe(NameKey).first())
    }

    @Test
    fun `observe reads a value stored with another type as null`() = scope.runTest {
        val store = fileStore()
        store.edit { values -> values[stringKey("count")] = "three" }

        assertNull(store.observe(CountKey).first())
    }

    @Test
    fun `observe re-emits only when its own key changes`() = scope.runTest {
        val store = fileStore()

        store.observe(NameKey).test {
            assertNull(awaitItem())

            store.edit { values -> values[CountKey] = 3 }
            expectNoEvents()

            store.edit { values -> values[NameKey] = "dark" }
            assertEquals("dark", awaitItem())
        }
    }

    @Test
    fun `edit writes every key of one transaction`() = scope.runTest {
        val store = fileStore()

        store.edit { values ->
            values[NameKey] = "dark"
            values[CountKey] = 3
        }

        assertEquals("dark", store.observe(NameKey).first())
        assertEquals(3, store.observe(CountKey).first())
    }

    @Test
    fun `edit writes nothing when the transaction throws`() = scope.runTest {
        val store = fileStore()

        val thrown = runCatching {
            store.edit { values ->
                values[NameKey] = "dark"
                error("boom")
            }
        }.exceptionOrNull()

        assertTrue("$thrown", thrown is IllegalStateException)
        assertNull(store.observe(NameKey).first())
    }

    @Test
    fun `edit removes a key set to null`() = scope.runTest {
        val store = fileStore()
        store.edit { values -> values[NameKey] = "dark" }

        store.edit { values -> values[NameKey] = null }

        assertNull(store.observe(NameKey).first())
    }

    @Test
    fun `update hands the transform the stored value`() = scope.runTest {
        val store = fileStore()
        store.edit { values -> values[CountKey] = 41 }

        store.update(CountKey) { count -> count?.plus(1) }

        assertEquals(42, store.observe(CountKey).first())
    }

    @Test
    fun `update applies two updates in flight one after the other`() = scope.runTest {
        val store = fileStore()

        listOf(
            launch { store.update(CountKey) { count -> (count ?: 0) + 1 } },
            launch { store.update(CountKey) { count -> (count ?: 0) + 1 } },
        ).joinAll()

        assertEquals(2, store.observe(CountKey).first())
    }

    @Test
    fun `observe reads null when the file cannot be read`() = scope.runTest {
        val store = DataStoreKeyValueStore(StubPreferencesDataStore(readFailure = IOException("corrupted")))

        assertNull(store.observe(NameKey).first())
    }

    @Test
    fun `observe lets a read failure that is not IO propagate`() = scope.runTest {
        val store = DataStoreKeyValueStore(StubPreferencesDataStore(readFailure = IllegalStateException("boom")))

        val thrown = runCatching { store.observe(NameKey).first() }.exceptionOrNull()

        assertTrue("$thrown", thrown is IllegalStateException)
    }

    @Test
    fun `edit lets a write failure propagate`() = scope.runTest {
        val store = DataStoreKeyValueStore(StubPreferencesDataStore(writeFailure = IOException("disk full")))

        val thrown = runCatching { store.edit { values -> values[NameKey] = "dark" } }.exceptionOrNull()

        assertTrue("$thrown", thrown is IOException)
    }
}

// A saboteur: reads or writes fail with the given error; a read that does not fail finds nothing stored.
private class StubPreferencesDataStore(
    private val readFailure: Throwable? = null,
    private val writeFailure: Throwable? = null,
) : DataStore<Preferences> {

    override val data: Flow<Preferences> = flow {
        readFailure?.let { throw it }
        emit(emptyPreferences())
    }

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences = throw writeFailure ?: IllegalStateException("No write failure configured")
}
