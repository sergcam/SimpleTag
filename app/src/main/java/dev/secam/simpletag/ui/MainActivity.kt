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

package dev.secam.simpletag.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import dev.secam.simpletag.data.preferences.PreferencesRepo
import dev.secam.simpletag.ui.theme.SimpleTagTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var preferencesRepo: PreferencesRepo

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme = preferencesRepo.preferencesFlow.collectAsState(null).value?.theme
            val colorScheme = preferencesRepo.preferencesFlow.collectAsState(null).value?.colorScheme
            val pureBlack = preferencesRepo.preferencesFlow.collectAsState(null).value?.pureBlack
            SimpleTagTheme(
                appTheme = theme,
                appColorScheme = colorScheme,
                pureBlack = pureBlack
            ) {
                SimpleTagApp()
            }
        }
    }
}