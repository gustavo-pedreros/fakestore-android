package cl.gus.labs.fakestore.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Shape

private val LightColorScheme = lightColorScheme(
    background = Paper,
    surface = PaperBright,
    surfaceContainer = PaperDim,
    surfaceContainerHigh = PaperDimmer,
    onSurface = Ink,
    onSurfaceVariant = Stone,
    primary = Lime,
    onPrimary = Ink,
    outlineVariant = Mist,
    outline = StoneLight,
    error = Coral,
    onError = Paper,
    inverseSurface = Ink,
    inverseOnSurface = Paper,
)

private val DarkColorScheme = darkColorScheme(
    background = Ink,
    surface = InkDim,
    surfaceContainer = InkDimmer,
    surfaceContainerHigh = InkDimmest,
    onSurface = Paper,
    onSurfaceVariant = StoneMuted,
    primary = Lime,
    onPrimary = Ink,
    outlineVariant = MistDark,
    outline = StoneDark,
    error = CoralLight,
    onError = Ink,
    inverseSurface = Paper,
    inverseOnSurface = Ink,
)

@Composable
fun FakeStoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val fakeStoreColors = if (darkTheme) FakeStoreColorsDark else FakeStoreColorsLight

    CompositionLocalProvider(LocalFakeStoreColors provides fakeStoreColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FakeStoreTypography,
            shapes = FakeStoreShapes,
            content = content,
        )
    }
}

object FakeStoreTheme {
    val colors: FakeStoreColors
        @Composable get() = LocalFakeStoreColors.current

    val spacing: FsSpacing get() = FakeStoreSpacing

    val textStyles: FsTextStyles get() = FakeStoreTextStyles

    val favoriteDiscShape: Shape get() = CircleShape
}
