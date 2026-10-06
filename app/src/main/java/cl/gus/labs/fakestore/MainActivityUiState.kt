package cl.gus.labs.fakestore

import androidx.compose.runtime.Immutable
import cl.gus.labs.fakestore.theme.ThemeMode

@Immutable
internal sealed interface MainActivityUiState {

    data object Loading : MainActivityUiState

    data class Ready(val themeMode: ThemeMode) : MainActivityUiState
}
