package cl.gus.labs.fakestore.core.settings.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.gus.labs.fakestore.core.settings.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("DataStoreSettingsRepository")
class DataStoreSettingsRepositoryTest {

    @TempDir
    lateinit var tmpDir: File

    private val scope = TestScope(UnconfinedTestDispatcher())

    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(scope = scope.backgroundScope) {
            File(tmpDir, "settings.preferences_pb")
        }
    }

    private val repository by lazy { DataStoreSettingsRepository(dataStore) }

    @Test
    @DisplayName("defaults to SYSTEM when nothing has been stored")
    fun defaultsToSystem() = scope.runTest {
        assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
    }

    @Test
    @DisplayName("reads back the mode that was written")
    fun readsBackWhatWasWritten() = scope.runTest {
        repository.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repository.themeMode.first())
    }

    @Test
    @DisplayName("a later write overwrites the previous one")
    fun laterWriteOverwrites() = scope.runTest {
        repository.setThemeMode(ThemeMode.DARK)
        repository.setThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, repository.themeMode.first())
    }

    @Test
    @DisplayName("falls back to SYSTEM when the stored value is not a known mode")
    fun unknownStoredValueFallsBack() = scope.runTest {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("theme_mode")] = "SEPIA"
        }

        assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
    }
}
