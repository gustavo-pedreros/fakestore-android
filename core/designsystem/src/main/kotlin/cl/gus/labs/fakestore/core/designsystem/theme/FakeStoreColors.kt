package cl.gus.labs.fakestore.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class FakeStoreColors(
    val imageTile: Color,
    val imageTileBorder: Color,
    val priceText: Color,
    val favoriteOnBg: Color,
    val favoriteOnGlyph: Color,
    val favoriteOffGlyph: Color,
    val offlineBg: Color,
    val offlineText: Color,
    val categoryBadge: Color,
    val skeleton: Color,
    val ratingStar: Color,
    val statusError: Color,
)

internal val FakeStoreColorsLight = FakeStoreColors(
    imageTile = PaperBright,
    imageTileBorder = Mist,
    priceText = Ink,
    favoriteOnBg = Lime,
    favoriteOnGlyph = Ink,
    favoriteOffGlyph = Stone,
    offlineBg = Ink,
    offlineText = Paper,
    categoryBadge = Ink,
    skeleton = Fog,
    ratingStar = Ink,
    statusError = Coral,
)

internal val FakeStoreColorsDark = FakeStoreColors(
    imageTile = PaperMuted,
    imageTileBorder = MistDark,
    priceText = Paper,
    favoriteOnBg = Lime,
    favoriteOnGlyph = Ink,
    favoriteOffGlyph = StoneMuted,
    offlineBg = Paper,
    offlineText = Ink,
    categoryBadge = Paper,
    skeleton = Charcoal,
    ratingStar = Paper,
    statusError = CoralLight,
)

internal val LocalFakeStoreColors = staticCompositionLocalOf { FakeStoreColorsLight }

object FakeStoreTheme {
    val colors: FakeStoreColors
        @Composable
        get() = LocalFakeStoreColors.current
}
