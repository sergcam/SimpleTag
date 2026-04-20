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

import android.net.Uri

fun uriToPath(uri: Uri): String {
    val path = uri.path!!
    val location = path.substringBefore(":").substringAfterLast("/")
    return when(location){
        "primary" ->
            path.replace("/tree/primary:", "/storage/emulated/0/")
        else ->
            path.replaceFirst(":", "/").replaceFirst("tree", "storage")
    }
}