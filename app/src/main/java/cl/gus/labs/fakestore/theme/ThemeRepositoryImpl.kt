package cl.gus.labs.fakestore.theme

import cl.gus.labs.fakestore.core.storage.keyvalue.KeyValueStore
import cl.gus.labs.fakestore.core.storage.keyvalue.stringKey
import cl.gus.labs.fakestore.core.storage.keyvalue.update
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val ThemeModeKey = stringKey("theme_mode")

internal class ThemeRepositoryImpl(
    private val store: KeyValueStore,
) : ThemeRepository {

    override val themeMode: Flow<ThemeMode> = store.observe(ThemeModeKey).map { stored -> stored.toThemeMode() }

    override suspend fun updateThemeMode(transform: (ThemeMode) -> ThemeMode) {
        try {
            store.update(ThemeModeKey) { stored -> transform(stored.toThemeMode()).name }
        } catch (e: IOException) {
            // Same as a failed read: the stored mode stays, and the flow keeps showing it.
        }
    }

    private fun String?.toThemeMode(): ThemeMode =
        this?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
}
