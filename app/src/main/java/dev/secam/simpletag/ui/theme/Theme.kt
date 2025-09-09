/*
 * Copyright (C) 2025  Sergio Camacho
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.secam.simpletag.ui.theme

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dev.secam.simpletag.data.AppColorScheme
import dev.secam.simpletag.data.AppTheme


@Composable
fun SimpleTagTheme(
    appTheme: AppTheme? = AppTheme.System,
    appColorScheme: AppColorScheme? = AppColorScheme.Dynamic,
    pureBlack: Boolean? = false,
    content: @Composable () -> Unit
) {
    val appColorScheme = appColorScheme ?: AppColorScheme.Dynamic
    val pureBlack = pureBlack ?: false
    val darkTheme = when(appTheme) {
        AppTheme.Dark -> true
        AppTheme.Light -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = getThemeColorScheme(
        appColorScheme = appColorScheme,
        darkTheme = darkTheme,
        pureBlack = pureBlack,
        context = LocalContext.current
    )
    SetEdge(appTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
fun getThemeColorScheme(appColorScheme: AppColorScheme, darkTheme: Boolean, pureBlack: Boolean, context: Context): ColorScheme{
    return when {
        appColorScheme == AppColorScheme.Dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {

            if (darkTheme) {
                if (pureBlack) blackThemeConvert(dynamicDarkColorScheme(context)) else dynamicDarkColorScheme(context)
            } else dynamicLightColorScheme(context)
        }

        darkTheme && pureBlack -> blackThemeConvert(getColorScheme(appColorScheme,true))
        darkTheme -> getColorScheme(appColorScheme,true)
        else -> getColorScheme(appColorScheme,false)
    }
}

@Composable
fun SetEdge(appTheme: AppTheme?) {
    val activity = LocalActivity.current as ComponentActivity?
    if(activity != null){
        when (appTheme) {
            AppTheme.Dark -> activity.enableEdgeToEdge(
                SystemBarStyle.dark(Color.TRANSPARENT)
            )

            AppTheme.Light -> activity.enableEdgeToEdge(
                SystemBarStyle.light(
                    Color.TRANSPARENT,
                    Color.TRANSPARENT
                )
            )

            else -> activity.enableEdgeToEdge()
        }
    }
}