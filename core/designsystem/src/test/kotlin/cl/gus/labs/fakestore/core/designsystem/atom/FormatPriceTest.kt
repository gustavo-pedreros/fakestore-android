package cl.gus.labs.fakestore.core.designsystem.atom

import java.util.Locale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("formatPrice")
class FormatPriceTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @DisplayName("prefixes the amount with $, two decimals, no grouping")
    @CsvSource(
        "109.95, \$109.95",
        "695.0, \$695.00",
        "1099.95, \$1099.95",
    )
    fun formatsAmount(amount: Double, expected: String) {
        assertEquals(expected, formatPrice(amount))
    }

    @Test
    @DisplayName("rounds half up")
    fun roundsHalfUp() {
        assertEquals("$0.13", formatPrice(0.125))
    }

    @Test
    @DisplayName("stays dot-decimal when the default locale is es-CL")
    fun ignoresDefaultLocale() {
        val original = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("es-CL"))
        try {
            assertEquals("$1099.95", formatPrice(1099.95))
        } finally {
            Locale.setDefault(original)
        }
    }
}
