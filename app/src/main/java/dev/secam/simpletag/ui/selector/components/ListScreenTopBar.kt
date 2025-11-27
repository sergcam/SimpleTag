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

package dev.secam.simpletag.ui.selector.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreenTopBar(
    searchEnabled: Boolean,
    textFieldState: TextFieldState,
    scrollBehavior: TopAppBarScrollBehavior,
    setSearchEnabled: (Boolean) -> Unit,
    onFilter: () -> Unit,
    onSort: () -> Unit
){
    val searchBarState = rememberSearchBarState()
    TopAppBar(
        title = {
            if (searchEnabled) {
                val focusRequester = remember { FocusRequester() }
                BackHandler {
                    textFieldState.clearText()
                    setSearchEnabled(false)
                }
                SearchBar(
                    state = searchBarState,
                    inputField = {
                        SearchBarDefaults.InputField(
                            searchBarState = searchBarState,
                            textFieldState = textFieldState,
                            leadingIcon = {
                                IconButton(
                                    onClick = {
                                        textFieldState.clearText()
                                        setSearchEnabled(false)
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_arrow_back_24px),
                                        contentDescription = stringResource(R.string.cd_back),
                                        modifier = Modifier
                                            .offset(x = (-5).dp)
                                    )
                                }
                            },
                            trailingIcon = if (textFieldState.text.isEmpty()) null else {
                                {
                                    IconButton(
                                        onClick = { textFieldState.clearText() }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_close_24px),
                                            contentDescription = stringResource(R.string.cd_clear_button)
                                        )
                                    }
                                }
                            },
                            onSearch = { },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                            ),
                            modifier = Modifier
                                .background(Color.Transparent)
                                .fillMaxWidth()
                        )
                    },

                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onGloballyPositioned {
                            focusRequester.requestFocus()
                        },
                    shape = RectangleShape,
                    colors = SearchBarDefaults.colors(
                        containerColor = Color.Transparent
                    ),
                )
            } else {
                IconButton(
                    onClick = { setSearchEnabled(true) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search_24px),
                        contentDescription = stringResource(R.string.cd_search_button)
                    )

                }
            }
        },
        actions = {
            if (searchEnabled) { Unit
            } else {
                IconButton(
                    onClick = {onFilter()}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_filter_24px),
                        contentDescription = stringResource(R.string.cd_filter_button)
                    )
                }
                IconButton(
                    onClick = {onSort()}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sort_24px),
                        contentDescription = stringResource(R.string.sort_button)
                    )
                }
            }
        },
        windowInsets = WindowInsets(0),
        scrollBehavior = scrollBehavior
    )
}