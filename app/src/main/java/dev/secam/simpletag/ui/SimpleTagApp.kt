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
import dev.secam.simpletag.data.media.MusicData
import dev.secam.simpletag.ui.editor.EditorScreen
import dev.secam.simpletag.ui.selector.SelectorScreen
import dev.secam.simpletag.ui.settings.AboutScreen
import dev.secam.simpletag.ui.settings.SettingsScreen
import dev.secam.simpletag.util.navTypeOf
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
        val duration = 300
        composable<Selector>(
            enterTransition = {
                return@composable fadeIn(tween(1000))
            }, exitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)
                )
            }, popEnterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(duration)
                )
            }
        ) {
            SelectorScreen(
                onNavigateToSettings = { navController.navigate(Settings) },
                onNavigateToEditor = { musicList: List<MusicData> ->
                    navController.navigate(Editor(musicList))
                }
            )
        }

        composable<Editor> (
            typeMap = mapOf(typeOf<List<MusicData>>() to navTypeOf<List<MusicData>>()),
            enterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)
                )
            },
            exitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)
                )
            },
            popExitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(duration)
                )
            },
            popEnterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(duration)
                )
            }
        ){ backStackEntry ->
            val editor: Editor = backStackEntry.toRoute()
            EditorScreen(
                musicList = editor.musicList,
                onNavigateBack = {navController.navigateUp()}
            )
        }

        composable<Settings> (
            enterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)
                )
            },
            exitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)
                )
            },
            popExitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(duration)
                )
            },
            popEnterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(duration)
                )
            }
        ){
            SettingsScreen(
                onNavigateBack = {navController.navigateUp()},
                onNavigateToAbout = {navController.navigate(About)}
            )
        }
        composable<About> (
            enterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)
                )
            },
            exitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)
                )
            },
            popExitTransition = {
                return@composable slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(duration)
                )
            },
            popEnterTransition = {
                return@composable slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(duration)
                )
            }
        ){
            AboutScreen(
                onNavigateBack = {navController.navigateUp()}
            )
        }
    }
}
