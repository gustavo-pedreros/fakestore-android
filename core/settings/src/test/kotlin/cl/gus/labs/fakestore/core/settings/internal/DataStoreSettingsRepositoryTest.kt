package cl.gus.labs.fakestore.core.settings.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.gus.labs.fakestore.core.settings.ThemeMode
import java.io.File
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

private val THEME_MODE = stringPreferencesKey("theme_mode")

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("DataStoreSettingsRepository")
class DataStoreSettingsRepositoryTest {

    @TempDir
    lateinit var tmpDir: File

    private val scope = TestScope(UnconfinedTestDispatcher())

    // DataStore runs on the JVM, so behavior is tested against the real one on a temporary file.
    private fun TestScope.fileDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(tmpDir, "settings.preferences_pb")
        }

    @Nested
    @DisplayName("reading")
    inner class Reading {

        @Test
        @DisplayName("defaults to SYSTEM when nothing has been stored")
        fun defaultsToSystem() = scope.runTest {
            val repository = DataStoreSettingsRepository(fileDataStore())

            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
        }

        @Test
        @DisplayName("falls back to SYSTEM when the stored value is not a known mode")
        fun unknownStoredValueFallsBack() = scope.runTest {
            val dataStore = fileDataStore()
            dataStore.edit { preferences -> preferences[THEME_MODE] = "SEPIA" }

            assertEquals(ThemeMode.SYSTEM, DataStoreSettingsRepository(dataStore).themeMode.first())
        }

        @Test
        @DisplayName("falls back to SYSTEM when the preferences file cannot be read")
        fun unreadableFileFallsBack() = scope.runTest {
            val repository = DataStoreSettingsRepository(StubPreferencesDataStore(IOException("corrupted")))

            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
        }

        @Test
        @DisplayName("lets a read failure that is not IO propagate")
        fun nonIoReadFailurePropagates() = scope.runTest {
            val repository = DataStoreSettingsRepository(StubPreferencesDataStore(IllegalStateException("boom")))

            val thrown = runCatching { repository.themeMode.first() }.exceptionOrNull()

            assertInstanceOf(IllegalStateException::class.java, thrown)
        }
    }

    @Nested
    @DisplayName("writing")
    inner class Writing {

        @ParameterizedTest(name = "{0}")
        @DisplayName("reads back the mode it stored")
        @EnumSource(ThemeMode::class)
        fun readsBackTheStoredMode(mode: ThemeMode) = scope.runTest {
            val repository = DataStoreSettingsRepository(fileDataStore())

            repository.setThemeMode(mode)

            assertEquals(mode, repository.themeMode.first())
        }

        @Test
        @DisplayName("a later write overwrites the previous one")
        fun laterWriteOverwrites() = scope.runTest {
            val repository = DataStoreSettingsRepository(fileDataStore())

            repository.setThemeMode(ThemeMode.DARK)
            repository.setThemeMode(ThemeMode.LIGHT)

            assertEquals(ThemeMode.LIGHT, repository.themeMode.first())
        }

        @Test
        @DisplayName("stores the mode under its enum name")
        fun storesTheEnumName() = scope.runTest {
            val dataStore = fileDataStore()

            DataStoreSettingsRepository(dataStore).setThemeMode(ThemeMode.DARK)

            assertEquals("DARK", dataStore.data.first()[THEME_MODE])
        }
    }
}

// A saboteur: every read fails with the given error.
private class StubPreferencesDataStore(
    private val readFailure: Throwable,
) : DataStore<Preferences> {

    override val data: Flow<Preferences> = flow { throw readFailure }

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences = error("Reads fail before any write")
}
