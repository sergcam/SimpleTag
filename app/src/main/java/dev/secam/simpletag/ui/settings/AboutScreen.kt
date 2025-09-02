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

package dev.secam.simpletag.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.secam.simpletag.R
import dev.secam.simpletag.ui.components.SettingsItem
import dev.secam.simpletag.ui.components.SimpleTopBar
import dev.secam.simpletag.ui.theme.SimpleTagTheme
import dev.secam.simpletag.util.getAppVersion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            SimpleTopBar(
                title = stringResource(R.string.about_title),
                onBack = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { contentPadding ->
        Column(
            modifier = modifier
                .padding(contentPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
        ) {
            val uriHandler = LocalUriHandler.current
            AppInfo(
                uriHandler = uriHandler,
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            AuthorInfo(
                uriHandler = uriHandler,
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            SupportInfo(
                uriHandler = uriHandler,
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun AppInfo(uriHandler: UriHandler, modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_simpletag_circle),
                contentDescription = stringResource(R.string.app_logo),
                modifier = Modifier
                    .size(50.dp)
                    .padding(start = 16.dp)
            )
            Text(
                text = stringResource(R.string.app_name),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
            )
        }

        Column {
            SettingsItem(
                headlineContent = stringResource(R.string.version),
                icon = painterResource(R.drawable.ic_info_24px),
                supportingContent = getAppVersion(LocalContext.current) ?: "null",
            )
            SettingsItem(
                headlineContent = stringResource(R.string.source_code),
                icon = painterResource(R.drawable.ic_code_24px),
            ) { uriHandler.openUri("https://github.com/sergcam/SimpleTag") }
            SettingsItem(
                headlineContent = stringResource(R.string.license),
                icon = painterResource(R.drawable.ic_balance_24px),
                supportingContent = "GPL v3",
            ) { showDialog = true }
        }
    }
    if (showDialog) {
//        LicenseDialog { showDialog = false }
    }
}

@Composable
fun AuthorInfo(uriHandler: UriHandler, modifier: Modifier = Modifier) {
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.author),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Column {
            SettingsItem(
                headlineContent = "Sergio Camacho",
                icon = painterResource(R.drawable.ic_person_24px),
            )
            SettingsItem(
                headlineContent = "Github",
                supportingContent = "sergcam",
                icon = painterResource(R.drawable.ic_github_logo),
            ) { uriHandler.openUri("https://github.com/sergcam/") }
            SettingsItem(
                headlineContent = stringResource(R.string.donate),
                supportingContent = "give me money",
                icon = painterResource(R.drawable.ic_kofi_24px),
            ) { uriHandler.openUri("https://ko-fi.com/sergcam") }
        }
    }
}

@Composable
fun SupportInfo(uriHandler: UriHandler, modifier: Modifier = Modifier) {
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.support),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
// feature
        Column {
            SettingsItem(
                headlineContent = stringResource(R.string.bugs),
                supportingContent = "Open an issue on GitHub",
                icon = painterResource(R.drawable.ic_feedback_24px),
            ) { uriHandler.openUri("https://github.com/sergcam/SimpleTag/issues/new") }
            SettingsItem(
                headlineContent = stringResource(R.string.email),
                supportingContent = "sergio@secam.dev",
                icon = painterResource(R.drawable.ic_mail_24px),
            ) { uriHandler.openUri("mailto:sergio@secam.dev") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutPreview() {
    SimpleTagTheme {
        AboutScreen(
            onNavigateBack = {},
        )
    }
}