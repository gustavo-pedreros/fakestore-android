package cl.gus.labs.fakestore.core.designsystem.atom

import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("formatRating")
class FormatRatingTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @DisplayName("keeps one decimal")
    @CsvSource(
        "3.9, 3.9",
        "2.1, 2.1",
        "4.0, 4.0",
    )
    fun formatsRate(rate: Double, expected: String) {
        assertEquals(expected, formatRating(rate))
    }

    @Test
    @DisplayName("pins today's 4.95 to 5.0")
    fun pinsTodaysRating() {
        assertEquals("5.0", formatRating(4.95))
    }

    @Test
    @DisplayName("stays dot-decimal when the default locale is es-CL")
    fun ignoresDefaultLocale() {
        val original = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("es-CL"))
        try {
            assertEquals("3.9", formatRating(3.9))
        } finally {
            Locale.setDefault(original)
        }
    }
}
