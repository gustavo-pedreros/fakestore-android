package cl.gus.labs.fakestore.core.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ThemeMode")
class ThemeModeTest {

    @Test
    @DisplayName("next() walks system to light to dark")
    fun nextWalksForward() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.SYSTEM.next())
        assertEquals(ThemeMode.DARK, ThemeMode.LIGHT.next())
        assertEquals(ThemeMode.SYSTEM, ThemeMode.DARK.next())
    }

    @Test
    @DisplayName("three steps return to the starting mode")
    fun cycleClosesInThreeSteps() {
        ThemeMode.entries.forEach { start ->
            assertEquals(start, start.next().next().next())
        }
    }
}
