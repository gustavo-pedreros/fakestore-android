package cl.gus.labs.fakestore.theme

import cl.gus.labs.fakestore.core.designsystem.model.ThemeModeUiModel
import cl.gus.labs.fakestore.core.settings.ThemeMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("ThemeMode mapping")
class ThemeModeMappingTest {

    @ParameterizedTest(name = "{0}: dark on a dark system {1}, on a light one {2}")
    @DisplayName("isDark follows the system only in SYSTEM")
    @CsvSource(
        "SYSTEM, true, false",
        "LIGHT, false, false",
        "DARK, true, true",
    )
    fun isDark(mode: ThemeMode, onDarkSystem: Boolean, onLightSystem: Boolean) {
        assertEquals(onDarkSystem, mode.isDark(systemDark = true))
        assertEquals(onLightSystem, mode.isDark(systemDark = false))
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @DisplayName("toUiModel keeps the mode")
    @CsvSource(
        "SYSTEM, System",
        "LIGHT, Light",
        "DARK, Dark",
    )
    fun toUiModel(mode: ThemeMode, expected: ThemeModeUiModel) {
        assertEquals(expected, mode.toUiModel())
    }
}
