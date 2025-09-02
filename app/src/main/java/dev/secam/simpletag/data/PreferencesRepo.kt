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

package dev.secam.simpletag.data

import kotlinx.coroutines.flow.Flow

interface PreferencesRepo {
    val preferencesFlow: Flow<UserPreferences>
    suspend fun saveThemePref(theme: SimpleAppTheme)
    suspend fun saveColorSchemePref(colorScheme: SimpleAppColorScheme)
    suspend fun savePureBlackPref(pureBlack: Boolean)
    suspend fun saveAdvancedEditorPref(advancedEditor: Boolean)
    suspend fun saveRoundCoversPref(roundCovers: Boolean)
}