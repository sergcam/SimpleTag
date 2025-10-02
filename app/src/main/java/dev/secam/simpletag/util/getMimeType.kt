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

package dev.secam.simpletag.util

import java.io.File

fun File.getMimeType(): String{
    return when(extension.lowercase()) {
        "mp3" ->
            "audio/mpeg"
        "mp4", "m4a", "m4p" ->
            "audio/mp4"
        "wav", "wave"->
            "audio/wav"
        "dsf" ->
            "audio/x-dsf"
        "aiff", "aif", "aifc" ->
            "audio/aiff"
        "wma" ->
            "audio/x-ms-wma"
        "ogg" ->
            "audio/ogg"
        "flac" ->
            "audio/flac"
        "aac" ->
            "audio/aac"
        else -> "audio/mpeg"
    }
}