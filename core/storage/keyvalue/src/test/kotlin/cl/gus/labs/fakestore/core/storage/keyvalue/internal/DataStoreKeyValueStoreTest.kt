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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

private val NameKey = stringKey("name")
private val EnabledKey = booleanKey("enabled")
private val CountKey = intKey("count")
private val SinceKey = longKey("since")
private val RatioKey = doubleKey("ratio")

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("DataStoreKeyValueStore")
class DataStoreKeyValueStoreTest {

    @TempDir
    lateinit var tmpDir: File

    private val scope = TestScope(UnconfinedTestDispatcher())

    // DataStore runs on the JVM, so behavior is tested against the real one on a temporary file.
    private fun TestScope.fileStore() = DataStoreKeyValueStore(
        PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(tmpDir, "test.preferences_pb")
        },
    )

    @Nested
    @DisplayName("values")
    inner class Values {

        @Test
        @DisplayName("reads back a value of each type")
        fun readsBackEachType() = scope.runTest {
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
        @DisplayName("reads an absent key as null")
        fun readsAbsentKeyAsNull() = scope.runTest {
            assertNull(fileStore().observe(NameKey).first())
        }

        @Test
        @DisplayName("reads a value stored with another type as null")
        fun readsOtherTypeAsNull() = scope.runTest {
            val store = fileStore()
            store.edit { values -> values[stringKey("count")] = "three" }

            assertNull(store.observe(CountKey).first())
        }
    }

    @Nested
    @DisplayName("edit")
    inner class Edit {

        @Test
        @DisplayName("writes every key of one transaction")
        fun writesEveryKey() = scope.runTest {
            val store = fileStore()

            store.edit { values ->
                values[NameKey] = "dark"
                values[CountKey] = 3
            }

            assertEquals("dark", store.observe(NameKey).first())
            assertEquals(3, store.observe(CountKey).first())
        }

        @Test
        @DisplayName("writes nothing when the transaction throws")
        fun failingTransactionWritesNothing() = scope.runTest {
            val store = fileStore()

            val thrown = runCatching {
                store.edit { values ->
                    values[NameKey] = "dark"
                    error("boom")
                }
            }.exceptionOrNull()

            assertInstanceOf(IllegalStateException::class.java, thrown)
            assertNull(store.observe(NameKey).first())
        }

        @Test
        @DisplayName("removes a key set to null")
        fun nullRemovesTheKey() = scope.runTest {
            val store = fileStore()
            store.edit { values -> values[NameKey] = "dark" }

            store.edit { values -> values[NameKey] = null }

            assertNull(store.observe(NameKey).first())
        }
    }

    @Nested
    @DisplayName("update")
    inner class Update {

        @Test
        @DisplayName("hands the transform the stored value")
        fun transformReceivesTheStoredValue() = scope.runTest {
            val store = fileStore()
            store.edit { values -> values[CountKey] = 41 }

            store.update(CountKey) { count -> count?.plus(1) }

            assertEquals(42, store.observe(CountKey).first())
        }

        @Test
        @DisplayName("applies two updates in flight one after the other")
        fun concurrentUpdatesBothApply() = scope.runTest {
            val store = fileStore()

            listOf(
                launch { store.update(CountKey) { count -> (count ?: 0) + 1 } },
                launch { store.update(CountKey) { count -> (count ?: 0) + 1 } },
            ).joinAll()

            assertEquals(2, store.observe(CountKey).first())
        }
    }

    @Nested
    @DisplayName("observe")
    inner class Observe {

        @Test
        @DisplayName("re-emits only when its own key changes")
        fun reEmitsOnlyForItsKey() = scope.runTest {
            val store = fileStore()

            store.observe(NameKey).test {
                assertNull(awaitItem())

                store.edit { values -> values[CountKey] = 3 }
                expectNoEvents()

                store.edit { values -> values[NameKey] = "dark" }
                assertEquals("dark", awaitItem())
            }
        }
    }

    @Nested
    @DisplayName("failures")
    inner class Failures {

        @Test
        @DisplayName("reads null when the file cannot be read")
        fun ioReadFailureReadsNull() = scope.runTest {
            val store = DataStoreKeyValueStore(StubPreferencesDataStore(readFailure = IOException("corrupted")))

            assertNull(store.observe(NameKey).first())
        }

        @Test
        @DisplayName("lets a read failure that is not IO propagate")
        fun nonIoReadFailurePropagates() = scope.runTest {
            val store = DataStoreKeyValueStore(StubPreferencesDataStore(readFailure = IllegalStateException("boom")))

            val thrown = runCatching { store.observe(NameKey).first() }.exceptionOrNull()

            assertInstanceOf(IllegalStateException::class.java, thrown)
        }

        @Test
        @DisplayName("lets a write failure propagate")
        fun writeFailurePropagates() = scope.runTest {
            val store = DataStoreKeyValueStore(StubPreferencesDataStore(writeFailure = IOException("disk full")))

            val thrown = runCatching { store.edit { values -> values[NameKey] = "dark" } }.exceptionOrNull()

            assertInstanceOf(IOException::class.java, thrown)
        }
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
