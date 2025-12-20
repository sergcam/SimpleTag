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

package dev.secam.simpletag.data.preferences

import dev.secam.simpletag.data.enums.AppColorScheme
import dev.secam.simpletag.data.enums.AppTheme
import dev.secam.simpletag.data.enums.FolderSelectMode
import dev.secam.simpletag.data.enums.SortDirection
import dev.secam.simpletag.data.enums.SortOrder
import kotlinx.coroutines.flow.Flow

interface PreferencesRepo {
    val preferencesFlow: Flow<UserPreferences>
    suspend fun saveThemePref(theme: AppTheme)
    suspend fun saveColorSchemePref(colorScheme: AppColorScheme)
    suspend fun savePureBlackPref(pureBlack: Boolean)
    suspend fun saveAdvancedEditorPref(advancedEditor: Boolean)
    suspend fun saveRoundCoversPref(roundCovers: Boolean)
    suspend fun saveSystemFontPref(systemFont: Boolean)
    suspend fun saveOptionalPermissionsSkipped(optionalPermissionsSkipped: Boolean)
    suspend fun saveRememberSort(rememberSort: Boolean)
    suspend fun saveSortOrder(sortOrder: SortOrder)
    suspend fun saveSortDirection(sortDirection: SortDirection)
    suspend fun saveSelectMode(selectMode: FolderSelectMode)
    suspend fun saveSelectedList(selectedList: Set<String>)
}