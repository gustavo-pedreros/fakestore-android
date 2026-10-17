package cl.gus.labs.fakestore.theme

import cl.gus.labs.fakestore.core.storage.keyvalue.Key
import cl.gus.labs.fakestore.core.storage.keyvalue.KeyValueStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@DisplayName("ThemeRepositoryImpl")
internal class ThemeRepositoryImplTest {

    @Nested
    @DisplayName("reading")
    inner class Reading {

        @Test
        @DisplayName("defaults to SYSTEM when nothing has been stored")
        fun defaultsToSystem() = runTest {
            val repository = ThemeRepositoryImpl(FakeKeyValueStore())

            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
        }

        @Test
        @DisplayName("falls back to SYSTEM when the stored value is not a known mode")
        fun unknownStoredValueFallsBack() = runTest {
            val repository = ThemeRepositoryImpl(FakeKeyValueStore("theme_mode" to "SEPIA"))

            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
        }
    }

    @Nested
    @DisplayName("updating")
    inner class Updating {

        @ParameterizedTest(name = "{0}")
        @DisplayName("reads back the mode it stored")
        @EnumSource(ThemeMode::class)
        fun readsBackTheStoredMode(mode: ThemeMode) = runTest {
            val repository = ThemeRepositoryImpl(FakeKeyValueStore())

            repository.updateThemeMode { mode }

            assertEquals(mode, repository.themeMode.first())
        }

        @Test
        @DisplayName("hands the transform the stored mode")
        fun transformReceivesTheStoredMode() = runTest {
            val repository = ThemeRepositoryImpl(FakeKeyValueStore("theme_mode" to "LIGHT"))

            repository.updateThemeMode(ThemeMode::next)

            assertEquals(ThemeMode.DARK, repository.themeMode.first())
        }

        @Test
        @DisplayName("stores the mode under its enum name")
        fun storesTheEnumName() = runTest {
            val store = FakeKeyValueStore()

            ThemeRepositoryImpl(store).updateThemeMode { ThemeMode.DARK }

            assertEquals("DARK", store.stored("theme_mode"))
        }

        @Test
        @DisplayName("keeps the stored mode when the write fails on IO")
        fun ioWriteFailureKeepsTheMode() = runTest {
            val repository = ThemeRepositoryImpl(StubKeyValueStore(writeFailure = IOException("disk full")))

            repository.updateThemeMode { ThemeMode.DARK }

            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
        }

        @Test
        @DisplayName("lets a write failure that is not IO propagate")
        fun nonIoWriteFailurePropagates() = runTest {
            val repository = ThemeRepositoryImpl(StubKeyValueStore(writeFailure = IllegalStateException("boom")))

            val thrown = runCatching { repository.updateThemeMode { ThemeMode.DARK } }.exceptionOrNull()

            assertInstanceOf(IllegalStateException::class.java, thrown)
        }
    }
}

// Keeps values in memory by key name, so a test can seed what an older version stored.
private class FakeKeyValueStore(vararg seed: Pair<String, Any>) : KeyValueStore {

    private val values = MutableStateFlow(mapOf(*seed))
    private val mutex = Mutex()

    fun stored(name: String): Any? = values.value[name]

    override fun <T : Any> observe(key: Key<T>): Flow<T?> =
        values.map { stored -> key.type.cast(stored[key.name]) }.distinctUntilChanged()

    override suspend fun edit(transaction: (KeyValueStore.Editor) -> Unit) {
        mutex.withLock {
            val editor = MapEditor(values.value.toMutableMap())
            transaction(editor)
            values.value = editor.values
        }
    }

    private class MapEditor(val values: MutableMap<String, Any>) : KeyValueStore.Editor {

        override fun <T : Any> get(key: Key<T>): T? = key.type.cast(values[key.name])

        override fun <T : Any> set(key: Key<T>, value: T?) {
            if (value == null) values.remove(key.name) else values[key.name] = value
        }
    }
}

// A saboteur: nothing is stored, and every write fails with the given error.
private class StubKeyValueStore(private val writeFailure: Throwable) : KeyValueStore {

    override fun <T : Any> observe(key: Key<T>): Flow<T?> = flowOf(null)

    override suspend fun edit(transaction: (KeyValueStore.Editor) -> Unit) {
        throw writeFailure
    }
}
