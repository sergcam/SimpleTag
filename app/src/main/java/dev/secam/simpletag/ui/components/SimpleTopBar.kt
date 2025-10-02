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

package dev.secam.simpletag.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import dev.secam.simpletag.R
import dev.secam.simpletag.ui.theme.SimpleTagTheme


// top bar with action icon
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    actionIcon: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    action: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        actions = {
            IconButton(onClick = action) {
                Icon(
                    painter = actionIcon,
                    contentDescription = contentDescription
                )
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

// top bar with back button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24px),
                    contentDescription = stringResource(R.string.cd_back_button)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

// top bar with action icon and back button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    actionIcon: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    action: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24px),
                    contentDescription = stringResource(R.string.cd_back_button)
                )
            }
        },
        actions = {
            IconButton(onClick = action) {
                Icon(
                    painter = actionIcon,
                    contentDescription = contentDescription
                )
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24px),
                    contentDescription = stringResource(R.string.cd_back_button)
                )
            }
        },
        actions = { actions() },
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TopBarPreview(){
    SimpleTagTheme {
        Scaffold (
            topBar = {
                SimpleTopBar(
                    title = "Simple Tag",
                    actionIcon = painterResource(R.drawable.ic_settings_24px),
                    contentDescription = stringResource(R.string.cd_settings_button),
                    action = {}
                )
            }
        ){ contentPadding ->
            Column(
                modifier = Modifier.padding(contentPadding),
            ){  }
        }
    }
}