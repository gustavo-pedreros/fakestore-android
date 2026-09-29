package cl.gus.labs.fakestore.core.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>

    /** Reads and writes in one transaction, so two quick updates cannot overwrite each other. */
    suspend fun updateThemeMode(transform: (ThemeMode) -> ThemeMode)
}
