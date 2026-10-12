package cl.gus.labs.fakestore.theme

import kotlinx.coroutines.flow.Flow

internal interface ThemeRepository {
    val themeMode: Flow<ThemeMode>

    /** Reads and writes in one transaction, so two quick updates cannot overwrite each other. */
    suspend fun updateThemeMode(transform: (ThemeMode) -> ThemeMode)
}
