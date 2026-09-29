package cl.gus.labs.fakestore

import androidx.compose.runtime.Immutable
import cl.gus.labs.fakestore.core.settings.ThemeMode

@Immutable
internal sealed interface MainActivityUiState {

    data object Loading : MainActivityUiState

    data class Ready(val themeMode: ThemeMode) : MainActivityUiState
}

// Until the stored mode is known, any frame could show the wrong theme.
internal fun MainActivityUiState.shouldKeepSplashScreen(): Boolean = this is MainActivityUiState.Loading
