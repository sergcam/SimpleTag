package dev.secam.simpletag.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.ui.editor.EditorScreen
import dev.secam.simpletag.ui.selector.SelectorScreen
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

@Composable
fun SimpleTagApp(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Selector
    ) {
        composable<Selector> {
            SelectorScreen() { musicList: List<MusicData> ->
                navController.navigate(Editor(musicList))
            }
        }

        composable<Editor> (
            typeMap = mapOf(typeOf<List<MusicData>>() to navTypeOf<List<MusicData>>())
        ){ backStackEntry ->
            val editor: Editor = backStackEntry.toRoute()
            EditorScreen(
                musicList = editor.musicList
            )
        }
    }
}
