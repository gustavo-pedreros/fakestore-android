package cl.gus.labs.fakestore.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import cl.gus.labs.fakestore.core.designsystem.R

// Both files are variable fonts, so the weight only takes effect as the 'wght' axis. The
// Font(resId, weight, style, loadingStrategy) overload hardcodes empty variation settings, which
// leaves a variable font at its default instance and draws every weight identically.
private fun variableFont(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(weight, FontStyle.Normal),
)

val FsSans = FontFamily(
    variableFont(R.font.space_grotesk_variable, FontWeight.Normal),
    variableFont(R.font.space_grotesk_variable, FontWeight.SemiBold),
    variableFont(R.font.space_grotesk_variable, FontWeight.Bold),
)

val FsMono = FontFamily(
    variableFont(R.font.jetbrains_mono_variable, FontWeight.Normal),
    variableFont(R.font.jetbrains_mono_variable, FontWeight.Bold),
)
