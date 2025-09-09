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

package dev.secam.simpletag.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.secam.simpletag.R
import dev.secam.simpletag.data.AppColorScheme
import dev.secam.simpletag.data.AppTheme
import dev.secam.simpletag.data.preferences.PreferencesRepo
import dev.secam.simpletag.data.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepo: PreferencesRepo
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val prefState = preferencesRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences()
    )
    val uiState = _uiState.asStateFlow()

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepo.saveThemePref(theme)
        }
    }

    fun setColorScheme(colorScheme: AppColorScheme) {
        viewModelScope.launch {
            preferencesRepo.saveColorSchemePref(colorScheme)
        }
    }

    fun setPureBlack(pureBlack: Boolean) {
        viewModelScope.launch {
            preferencesRepo.savePureBlackPref(pureBlack)
        }
    }

    fun setAdvancedEditor(advancedEditor: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveAdvancedEditorPref(advancedEditor)
        }
    }

    fun setRoundCovers(roundCovers: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveRoundCoversPref(roundCovers)
        }
    }
    fun setSystemFont(systemFont: Boolean) {
        viewModelScope.launch {
            preferencesRepo.saveSystemFontPref(systemFont)
        }
    }

    fun setShowThemeDialog(showThemeDialog: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showThemeDialog = showThemeDialog
            )
        }
    }
    fun setShowColorSchemeDialog(showColorSchemeDialog: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                showColorSchemeDialog = showColorSchemeDialog
            )
        }
    }


    @Composable
    fun getThemeIcon(theme: AppTheme): Painter {
        return when (theme) {
            AppTheme.System -> painterResource(R.drawable.ic_system_theme_24px)
            AppTheme.Dark -> painterResource(R.drawable.ic_dark_mode_24px)
            AppTheme.Light -> painterResource(R.drawable.ic_light_mode_24px)
        }
    }
}

data class SettingsUiState(
    val showThemeDialog: Boolean = false,
    val showColorSchemeDialog: Boolean = false,
)
