package cl.gus.labs.fakestore.core.settings.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.gus.labs.fakestore.core.settings.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import java.io.File
import java.io.IOException

private val THEME_MODE = stringPreferencesKey("theme_mode")

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("DataStoreSettingsRepository")
class DataStoreSettingsRepositoryTest {

    @TempDir
    lateinit var tmpDir: File

    private val scope = TestScope(UnconfinedTestDispatcher())

    @Nested
    @DisplayName("reading")
    inner class Reading {

        @Test
        @DisplayName("defaults to SYSTEM when nothing has been stored")
        fun defaultsToSystem() = scope.runTest {
            val repository = DataStoreSettingsRepository(FakePreferencesDataStore())

            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
        }

        @Test
        @DisplayName("falls back to SYSTEM when the stored value is not a known mode")
        fun unknownStoredValueFallsBack() = scope.runTest {
            val dataStore = FakePreferencesDataStore()
            dataStore.edit { preferences -> preferences[THEME_MODE] = "SEPIA" }

            assertEquals(ThemeMode.SYSTEM, DataStoreSettingsRepository(dataStore).themeMode.first())
        }

        @Test
        @DisplayName("falls back to SYSTEM when the preferences file cannot be read")
        fun unreadableFileFallsBack() = scope.runTest {
            val dataStore = FakePreferencesDataStore(readFailure = IOException("corrupted"))

            assertEquals(ThemeMode.SYSTEM, DataStoreSettingsRepository(dataStore).themeMode.first())
        }

        @Test
        @DisplayName("lets a failure that is not IO propagate")
        fun nonIoFailurePropagates() = scope.runTest {
            val dataStore = FakePreferencesDataStore(readFailure = IllegalStateException("boom"))
            val repository = DataStoreSettingsRepository(dataStore)

            val thrown = runCatching { repository.themeMode.first() }.exceptionOrNull()

            assertInstanceOf(IllegalStateException::class.java, thrown)
        }
    }

    @Nested
    @DisplayName("writing")
    inner class Writing {

        @Test
        @DisplayName("reads back the mode that was written")
        fun readsBackWhatWasWritten() = scope.runTest {
            val repository = DataStoreSettingsRepository(FakePreferencesDataStore())

            repository.setThemeMode(ThemeMode.DARK)

            assertEquals(ThemeMode.DARK, repository.themeMode.first())
        }

        @Test
        @DisplayName("a later write overwrites the previous one")
        fun laterWriteOverwrites() = scope.runTest {
            val repository = DataStoreSettingsRepository(FakePreferencesDataStore())

            repository.setThemeMode(ThemeMode.DARK)
            repository.setThemeMode(ThemeMode.LIGHT)

            assertEquals(ThemeMode.LIGHT, repository.themeMode.first())
        }

        @Test
        @DisplayName("stores the mode under its enum name")
        fun storesTheEnumName() = scope.runTest {
            val dataStore = FakePreferencesDataStore()

            DataStoreSettingsRepository(dataStore).setThemeMode(ThemeMode.DARK)

            assertEquals("DARK", dataStore.data.first()[THEME_MODE])
        }
    }

    @Nested
    @DisplayName("on a real preferences file")
    inner class OnDisk {

        @Test
        @DisplayName("round-trips the mode through the file")
        fun roundTripsThroughTheFile() = scope.runTest {
            val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) {
                File(tmpDir, "settings.preferences_pb")
            }
            val repository = DataStoreSettingsRepository(dataStore)

            repository.setThemeMode(ThemeMode.DARK)

            assertEquals(ThemeMode.DARK, repository.themeMode.first())
        }
    }
}

private class FakePreferencesDataStore(
    private val readFailure: Throwable? = null,
) : DataStore<Preferences> {

    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> =
        readFailure?.let { cause -> flow<Preferences> { throw cause } } ?: state

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences = transform(state.value).also { state.value = it }
}
