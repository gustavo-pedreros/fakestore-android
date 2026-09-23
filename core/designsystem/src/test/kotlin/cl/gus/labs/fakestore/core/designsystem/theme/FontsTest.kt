package cl.gus.labs.fakestore.core.designsystem.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.ResourceFont
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("The variable font families")
class FontsTest {

    @Nested
    @DisplayName("FsSans")
    inner class FsSansFamily {

        @Test
        @DisplayName("declares Normal, SemiBold and Bold")
        fun declaresThreeWeights() {
            assertEquals(
                listOf(FontWeight.Normal, FontWeight.SemiBold, FontWeight.Bold),
                FsSans.weights(),
            )
        }

        @Test
        @DisplayName("carries every weight on the wght axis, so the weights actually differ")
        fun carriesWghtAxis() {
            FsSans.resourceFonts().forEach { font ->
                assertEquals(expectedSettings(font.weight), font.variationSettings.settings)
            }
        }
    }

    @Nested
    @DisplayName("FsMono")
    inner class FsMonoFamily {

        @Test
        @DisplayName("declares Normal and Bold")
        fun declaresTwoWeights() {
            assertEquals(listOf(FontWeight.Normal, FontWeight.Bold), FsMono.weights())
        }

        @Test
        @DisplayName("carries every weight on the wght axis, so the weights actually differ")
        fun carriesWghtAxis() {
            FsMono.resourceFonts().forEach { font ->
                assertEquals(expectedSettings(font.weight), font.variationSettings.settings)
            }
        }
    }

    // Without these the font stays at its default instance and every weight draws the same, which is
    // invisible in code review and was only caught by looking at a rendered type-scale screenshot.
    private fun expectedSettings(weight: FontWeight) =
        FontVariation.Settings(weight, FontStyle.Normal).settings

    @Suppress("UNCHECKED_CAST")
    private fun FontFamily.resourceFonts() = (this as List<Font>).map { it as ResourceFont }

    private fun FontFamily.weights() = resourceFonts().map { it.weight }
}
