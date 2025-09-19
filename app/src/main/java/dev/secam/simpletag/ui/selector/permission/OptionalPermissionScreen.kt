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

package dev.secam.simpletag.ui.selector.permission

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import dev.secam.simpletag.R
import dev.secam.simpletag.ui.theme.SimpleTagTheme

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OptionalPermissionScreen(
    modifier: Modifier = Modifier,
    setSkipped: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var manageMedia by remember {
        mutableStateOf(MediaStore.canManageMedia(context))
    }
    val optionalPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_MEDIA_LOCATION,
    )
    var mediaLocation by remember {
        mutableStateOf(
            optionalPermissionState.status.isGranted
        )
    }
    mediaLocation = optionalPermissionState.status.isGranted
    var skip by remember { mutableStateOf(false) }


    // update manage media permission on resume from settings activity
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        manageMedia = MediaStore.canManageMedia(context)
    }
    if(manageMedia && mediaLocation) {
        skip = true
    }
    if (skip) {
        setSkipped(true)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 80.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_info_24px),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(48.dp)

        )
        Text(
            text = stringResource(R.string.optional_permissions),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier
                .padding(bottom = 6.dp)
        )
        Text(
            text = stringResource(R.string.optional_permissions_long),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )

        Row() {
            val color =
                if (manageMedia) Color(0xff4c996e) else MaterialTheme.colorScheme.error
            Text(
                text = stringResource(R.string.manage_media),
                color = color,
                fontWeight = FontWeight.Medium
            )
            Icon(
                painter =
                    if (manageMedia) painterResource(R.drawable.ic_check_24px)
                    else painterResource(R.drawable.ic_close_24px),
                contentDescription = stringResource(R.string.cd_permission_not_granted),
                tint = color,

                )
        }
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            val color =
                if (mediaLocation) Color(0xff4c996e) else MaterialTheme.colorScheme.error
            Text(
                text = stringResource(R.string.access_media_location),
                color = color,
                fontWeight = FontWeight.Medium
            )
            Icon(
                painter =
                    if (mediaLocation) painterResource(R.drawable.ic_check_24px)
                    else painterResource(R.drawable.ic_close_24px),
                contentDescription = stringResource(R.string.cd_permission_not_granted),
                tint = color,
                modifier = Modifier
            )
        }
        Text(
            text = stringResource(R.string.optional_permission_note),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Button(
            onClick = {
                if (!mediaLocation) {
                    optionalPermissionState.launchPermissionRequest()
                }
                if (!manageMedia) {
                    context.startActivity(Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA))
                }
            }
        ) {
            Text(
                text = stringResource(R.string.grant_permission),
                fontWeight = FontWeight.Bold
            )
        }
        TextButton({skip = true}) {
            Text(
                text = stringResource(R.string.skip)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalPermissionsApi::class)
@Preview
@Composable
fun OptPermissionPrev() {
    SimpleTagTheme {
        OptionalPermissionScreen(){}
    }
}