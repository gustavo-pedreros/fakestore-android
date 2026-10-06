package cl.gus.labs.fakestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.gus.labs.fakestore.theme.ThemeMode
import cl.gus.labs.fakestore.theme.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class MainActivityViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = themeRepository.themeMode
        .map<ThemeMode, MainActivityUiState> { themeMode -> MainActivityUiState.Ready(themeMode) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MainActivityUiState.Loading,
        )

    // The repository computes the next mode from the stored one, so quick taps never read a stale mode.
    fun onThemeToggle() {
        viewModelScope.launch { themeRepository.updateThemeMode(ThemeMode::next) }
    }
}
