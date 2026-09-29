package cl.gus.labs.fakestore

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.gus.labs.fakestore.core.designsystem.theme.FakeStoreTheme
import cl.gus.labs.fakestore.core.settings.ThemeMode
import cl.gus.labs.fakestore.navigation.FakeStoreNavHost
import cl.gus.labs.fakestore.theme.isDark
import cl.gus.labs.fakestore.theme.toUiModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    // androidx.activity's default scrims, for three-button navigation.
    private val lightScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
    private val darkScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { viewModel.uiState.value.shouldKeepSplashScreen() }
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val themeMode = (uiState as? MainActivityUiState.Ready)?.themeMode ?: ThemeMode.SYSTEM
            val darkTheme = themeMode.isDark(systemDark = isSystemInDarkTheme())

            // The default reads dark mode from the system; the stored mode may override it.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(lightScrim, darkScrim) { darkTheme },
                )
                onDispose {}
            }

            FakeStoreTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    FakeStoreNavHost(
                        themeMode = themeMode.toUiModel(),
                        onThemeToggleClick = viewModel::onThemeToggle,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
                    )
                }
            }
        }
    }
}
