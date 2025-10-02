package dev.secam.simpletag.util

fun durationFormatter(duration: Int): String {
    val min = (duration / 60000).toString()
    var sec = ((duration / 1000) % 60).toString()
    if (sec.length == 1) sec = "0$sec"
    return ("$min:$sec")
}