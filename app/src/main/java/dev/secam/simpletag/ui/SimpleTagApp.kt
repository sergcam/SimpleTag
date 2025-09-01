package dev.secam.simpletag.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.TransformOrigin
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.secam.simpletag.data.MusicData
import dev.secam.simpletag.ui.editor.EditorScreen
import dev.secam.simpletag.ui.selector.SelectorScreen
import kotlin.reflect.typeOf

@Composable
fun SimpleTagApp(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Selector,
        popExitTransition = {
            scaleOut(
                targetScale = 0.9f,
                transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f)
            )
        },
        popEnterTransition = {
            EnterTransition.None
        },
    ) {
        composable<Selector> (enterTransition = {
            return@composable fadeIn(tween(1000))
        }, exitTransition = {
            return@composable slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(700)
            )
        }, popEnterTransition = {
            return@composable slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(700)
            )
        }) {
            SelectorScreen() { musicList: List<MusicData> ->
                navController.navigate(Editor(musicList))
            }
        }

        composable<Editor> (
            typeMap = mapOf(typeOf<List<MusicData>>() to navTypeOf<List<MusicData>>()),
            enterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(700)
                )
            },
            exitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(700)
                )
            },
            popExitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(700)
                )
            },
            popEnterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(700)
                )
            }
        ){ backStackEntry ->
            val editor: Editor = backStackEntry.toRoute()
            EditorScreen(
                musicList = editor.musicList,
                onNavigateBack = {navController.navigateUp()}
            )
        }
    }
}
