package cl.gus.labs.fakestore.core.settings

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    fun next(): ThemeMode = when (this) {
        SYSTEM -> LIGHT
        LIGHT -> DARK
        DARK -> SYSTEM
    }
}
