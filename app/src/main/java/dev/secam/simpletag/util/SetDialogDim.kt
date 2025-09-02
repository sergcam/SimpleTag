package dev.secam.simpletag.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider

/**
 * When composed within a dialog, sets background dim to [amount]
 */
@Composable
fun SetDialogDim(amount: Float = .25f){
    (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(amount)
}