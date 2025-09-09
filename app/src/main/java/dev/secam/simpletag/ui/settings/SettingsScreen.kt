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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.secam.simpletag.R
import dev.secam.simpletag.ui.components.SimpleListItem
import dev.secam.simpletag.ui.components.SimpleSectionHeader
import dev.secam.simpletag.ui.components.SimpleTopBar
import dev.secam.simpletag.ui.components.SimpleToggleItem
import dev.secam.simpletag.ui.settings.dialogs.ColorSchemeDialog
import dev.secam.simpletag.ui.settings.dialogs.ThemeDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    //user preferences
    val prefs = viewModel.prefState.collectAsState().value
    val theme = prefs.theme
    val colorScheme = prefs.colorScheme
    val pureBlack = prefs.pureBlack
    val advancedEditor = prefs.advancedEditor
    val roundCovers = prefs.roundCovers
    val systemFont = prefs.systemFont

    // ui state
    val uiState = viewModel.uiState.collectAsState().value
    val showThemeDialog = uiState.showThemeDialog
    val showColorSchemeDialog = uiState.showColorSchemeDialog

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            SimpleTopBar(
                title = stringResource(R.string.settings_title),
                onBack = { onNavigateBack() }
            )
        },
        modifier = modifier
    ) { contentPadding ->

        Column(
            modifier = modifier
                .padding(contentPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
        ) {
            //  Appearance Section
            SimpleSectionHeader("Appearance")
            SimpleListItem(
                headlineContent = stringResource(R.string.theme),
                supportingContent = stringResource(theme.displayNameRes),
                icon = viewModel.getThemeIcon(theme)
            ) {viewModel.setShowThemeDialog(true)}
            SimpleListItem(
                headlineContent = stringResource(R.string.color_scheme),
                supportingContent = stringResource(colorScheme.displayNameRes),
                icon = painterResource(R.drawable.ic_palette_24px),
                iconColor = MaterialTheme.colorScheme.primary
            ) {viewModel.setShowColorSchemeDialog(true)}
            SimpleToggleItem(
                headlineContent = stringResource(R.string.pure_black),
                currentState = pureBlack,
                onToggle = viewModel::setPureBlack
            )
            SimpleToggleItem(
                headlineContent = stringResource(R.string.system_font),
                currentState = systemFont,
                onToggle = viewModel::setSystemFont
            )
            HorizontalDivider(
                modifier = modifier
                    .padding(vertical = 16.dp)
            )

            //  Editor Section
            SimpleSectionHeader("Editor")
            SimpleToggleItem(
                headlineContent = stringResource(R.string.advanced_editor),
                supportingContent = stringResource(R.string.advanced_editor_subtext),
                currentState = advancedEditor,
                onToggle = viewModel::setAdvancedEditor
            )
            SimpleToggleItem(
                headlineContent = stringResource(R.string.round_album_cover),
                currentState = roundCovers,
                onToggle = viewModel::setRoundCovers
            )
            HorizontalDivider(
                modifier = modifier
                    .padding(vertical = 16.dp)
            )

            //  About Section
            SimpleSectionHeader("About")
            SimpleListItem(
                headlineContent = stringResource(R.string.about_title),
                icon = painterResource(R.drawable.ic_info_24px)
            ) { onNavigateToAbout() }
        }

        //  Show Dialogs
        if(showThemeDialog) {
            ThemeDialog(
                theme = theme,
                setTheme = viewModel::setTheme
            ) { viewModel.setShowThemeDialog(false) }
        }
        if(showColorSchemeDialog) {
            ColorSchemeDialog(
                colorScheme = colorScheme,
                setColorScheme = viewModel::setColorScheme
            ) { viewModel.setShowColorSchemeDialog(false) }
        }
    }
}

