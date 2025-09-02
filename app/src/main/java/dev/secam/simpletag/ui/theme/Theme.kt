package dev.secam.simpletag.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.secam.simpletag.data.SimpleAppColorScheme
import dev.secam.simpletag.data.SimpleAppTheme


@Composable
fun SimpleTagTheme(
    appTheme: SimpleAppTheme? = SimpleAppTheme.System,
    appColorScheme: SimpleAppColorScheme? = SimpleAppColorScheme.Dynamic,
    pureBlack: Boolean? = false,
    content: @Composable () -> Unit
) {
    val appColorScheme = appColorScheme ?: SimpleAppColorScheme.Dynamic
    val pureBlack = pureBlack ?: false
    val darkTheme = when(appTheme) {
        SimpleAppTheme.Dark -> true
        SimpleAppTheme.Light -> false
        else -> isSystemInDarkTheme()
    }
    val dynamicColor = when(appColorScheme) {
        SimpleAppColorScheme.Dynamic -> true
        else -> false
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                if (pureBlack) blackThemeConvert(dynamicDarkColorScheme(context)) else dynamicDarkColorScheme(context)
            } else dynamicLightColorScheme(context)
        }

        darkTheme && pureBlack -> blackThemeConvert(getColorScheme(appColorScheme,true))
        darkTheme -> getColorScheme(appColorScheme,true)
        else -> getColorScheme(appColorScheme,false)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}