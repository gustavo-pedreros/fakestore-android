package cl.gus.labs.fakestore

import cl.gus.labs.fakestore.core.settings.SettingsRepository
import cl.gus.labs.fakestore.core.settings.ThemeMode
import cl.gus.labs.fakestore.core.testing.MainDispatcherExtension
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
@DisplayName("MainActivityViewModel")
class MainActivityViewModelTest {

    @Test
    @DisplayName("stays Loading until the stored mode arrives")
    fun loadingUntilTheModeArrives() {
        val viewModel = MainActivityViewModel(FakeSettingsRepository(initial = null))

        assertEquals(MainActivityUiState.Loading, viewModel.uiState.value)
    }

    @Test
    @DisplayName("keeps the splash screen only while Loading")
    fun splashOnlyWhileLoading() {
        assertTrue(MainActivityUiState.Loading.shouldKeepSplashScreen())
        assertFalse(MainActivityUiState.Ready(ThemeMode.SYSTEM).shouldKeepSplashScreen())
    }

    @Test
    @DisplayName("exposes the stored mode")
    fun exposesTheStoredMode() {
        val viewModel = MainActivityViewModel(FakeSettingsRepository(initial = ThemeMode.DARK))

        assertEquals(MainActivityUiState.Ready(ThemeMode.DARK), viewModel.uiState.value)
    }

    @Test
    @DisplayName("each toggle stores and shows the next mode")
    fun toggleWalksTheCycle() {
        val repository = FakeSettingsRepository(initial = ThemeMode.SYSTEM)
        val viewModel = MainActivityViewModel(repository)

        listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).forEach { expected ->
            viewModel.onThemeToggle()

            assertEquals(expected, repository.stored.value)
            assertEquals(MainActivityUiState.Ready(expected), viewModel.uiState.value)
        }
    }

    @Test
    @DisplayName("two toggles before the store shows the first one still move two steps")
    fun quickTogglesDoNotReadAStaleMode() {
        val repository = FakeSettingsRepository(initial = ThemeMode.SYSTEM, showsWrites = false)
        val viewModel = MainActivityViewModel(repository)

        viewModel.onThemeToggle()
        viewModel.onThemeToggle()

        assertEquals(ThemeMode.DARK, repository.stored.value)
    }
}

// Keeps the mode in memory. With showsWrites = false, a write lands but the flow keeps the old mode,
// like a store that has not emitted yet.
private class FakeSettingsRepository(
    initial: ThemeMode?,
    private val showsWrites: Boolean = true,
) : SettingsRepository {

    val stored = MutableStateFlow(initial)
    private val shown = MutableStateFlow(initial)

    override val themeMode: Flow<ThemeMode> = shown.filterNotNull()

    override suspend fun updateThemeMode(transform: (ThemeMode) -> ThemeMode) {
        stored.value = transform(stored.value ?: ThemeMode.SYSTEM)
        if (showsWrites) shown.value = stored.value
    }
}
