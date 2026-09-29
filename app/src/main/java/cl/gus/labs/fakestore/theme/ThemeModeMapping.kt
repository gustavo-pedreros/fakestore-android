package cl.gus.labs.fakestore.theme

import cl.gus.labs.fakestore.core.designsystem.model.ThemeModeUiModel
import cl.gus.labs.fakestore.core.settings.ThemeMode

internal fun ThemeMode.isDark(systemDark: Boolean): Boolean = when (this) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

internal fun ThemeMode.toUiModel(): ThemeModeUiModel = when (this) {
    ThemeMode.SYSTEM -> ThemeModeUiModel.System
    ThemeMode.LIGHT -> ThemeModeUiModel.Light
    ThemeMode.DARK -> ThemeModeUiModel.Dark
}
