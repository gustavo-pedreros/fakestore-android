package cl.gus.labs.fakestore.core.settings.internal

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import cl.gus.labs.fakestore.core.settings.SettingsRepository
import cl.gus.labs.fakestore.core.settings.ThemeMode
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

internal class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode> = dataStore.data
        .catch { cause ->
            if (cause is IOException) emit(emptyPreferences()) else throw cause
        }
        .map { preferences -> preferences[THEME_MODE].toThemeMode() }

    override suspend fun updateThemeMode(transform: (ThemeMode) -> ThemeMode) {
        try {
            dataStore.edit { preferences ->
                preferences[THEME_MODE] = transform(preferences[THEME_MODE].toThemeMode()).name
            }
        } catch (e: IOException) {
            // Same as a failed read: the stored mode stays, and the flow keeps showing it.
        }
    }

    private fun String?.toThemeMode(): ThemeMode =
        this?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM

    private companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
