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

import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class PreferencesRepoImpl @Inject constructor(private val dataStore: DataStore<Preferences>) : PreferencesRepo {
    private companion object {
        val THEME = stringPreferencesKey("theme")
        val COLOR_SCHEME = stringPreferencesKey("color_scheme")
        val PURE_BLACK = booleanPreferencesKey("pure_black")
        val ADVANCED_EDITOR = booleanPreferencesKey("advanced_editor")
        val ROUND_COVERS = booleanPreferencesKey("round_covers")
        const val TAG = "PreferencesRepo"
    }

    override val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            Log.d(TAG, "prefs read")

            val theme = preferences[THEME] ?: "System"
            val colorScheme = preferences[COLOR_SCHEME] ?: if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) "Dynamic" else "Classic"
            val pureBlack = preferences[PURE_BLACK] ?: false
            val advancedEditor = preferences[ADVANCED_EDITOR] ?: false
            val roundCovers = preferences[ROUND_COVERS] ?: true

            UserPreferences(
                theme = SimpleAppTheme.valueOf(theme),
                colorScheme = SimpleAppColorScheme.valueOf(colorScheme),
                pureBlack = pureBlack,
                advancedEditor = advancedEditor,
                roundCovers = roundCovers,
            )

        }

    override suspend fun saveThemePref(theme: SimpleAppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME] = theme.name
        }
    }

    override suspend fun saveColorSchemePref(colorScheme: SimpleAppColorScheme) {
        dataStore.edit { preferences ->
            preferences[COLOR_SCHEME] = colorScheme.name
        }
    }

    override suspend fun savePureBlackPref(pureBlack: Boolean) {
        dataStore.edit { preferences ->
            preferences[PURE_BLACK] = pureBlack
        }
    }

    override suspend fun saveAdvancedEditorPref(advancedEditor: Boolean) {
        dataStore.edit { preferences ->
            preferences[ADVANCED_EDITOR] = advancedEditor
        }
    }

    override suspend fun saveRoundCoversPref(roundCovers: Boolean) {
        dataStore.edit { preferences ->
            preferences[ROUND_COVERS] = roundCovers
        }
    }
}

