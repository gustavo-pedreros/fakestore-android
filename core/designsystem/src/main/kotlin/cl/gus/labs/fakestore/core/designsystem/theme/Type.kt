package cl.gus.labs.fakestore.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val FakeStoreTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FsSans,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.03f).em,
    ),
    headlineMedium = TextStyle(
        fontFamily = FsSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02f).em,
    ),
    titleLarge = TextStyle(
        fontFamily = FsSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01f).em,
    ),
    titleMedium = TextStyle(
        fontFamily = FsSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.005f).em,
    ),
    bodyLarge = TextStyle(
        fontFamily = FsSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FsSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FsSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.06f.em,
    ),
    labelSmall = TextStyle(
        fontFamily = FsSans,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.14f.em,
    ),
)

@Immutable
data class FsTextStyles(
    val priceLarge: TextStyle,
    val priceMedium: TextStyle,
    val ratingValue: TextStyle,
    val ratingCount: TextStyle,
)

val FakeStoreTextStyles = FsTextStyles(
    priceLarge = TextStyle(
        fontFamily = FsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 30.sp,
    ),
    priceMedium = TextStyle(
        fontFamily = FsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    ),
    ratingValue = TextStyle(
        fontFamily = FsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    ratingCount = TextStyle(
        fontFamily = FsMono,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
