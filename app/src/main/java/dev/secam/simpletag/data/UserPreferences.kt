package dev.secam.simpletag.data

data class UserPreferences(
    val theme: SimpleAppTheme = SimpleAppTheme.System,
    val colorScheme: SimpleAppColorScheme = SimpleAppColorScheme.Dynamic,
    val pureBlack: Boolean = false,
    val advancedEditor: Boolean = false,
    val roundCovers: Boolean = true,
)
