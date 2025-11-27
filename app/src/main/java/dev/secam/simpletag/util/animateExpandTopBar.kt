package dev.secam.simpletag.util

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior

@OptIn(ExperimentalMaterial3Api::class)
suspend fun TopAppBarScrollBehavior.animateExpandTopBar(animationSpec: AnimationSpec<Float> = tween(50)){
    animate(
        initialValue = state.heightOffset,
        targetValue = 0f,
        animationSpec = animationSpec,
    ) { value, /* velocity */ _ ->
        // Update alpha mutable state with the current animation value
        state.heightOffset = value
    }
}