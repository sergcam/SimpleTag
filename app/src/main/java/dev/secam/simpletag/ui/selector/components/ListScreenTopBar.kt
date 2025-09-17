package dev.secam.simpletag.ui.selector.components

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
import androidx.compose.ui.unit.dp
import dev.secam.simpletag.R
import dev.secam.simpletag.util.BackPressHandler

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
                BackPressHandler {
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
                                        contentDescription = "back",
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
                                            contentDescription = "close"
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
//                                if (showSearch && !showingIme) {
                            focusRequester.requestFocus()
//                                    onShowSearchChange(false)

                        },
                    shape = RectangleShape,
                    colors = SearchBarDefaults.colors(
                        containerColor = Color.Transparent
                    ),
//                        tonalElevation = TODO(),
//                        shadowElevation = TODO()
                )
            } else {
                IconButton(
                    onClick = { setSearchEnabled(true) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search_24px),
                        contentDescription = "search"
                    )

                }
            }
        },
//                modifier = TODO(),
//                navigationIcon = TODO(),
        actions = {
            if (searchEnabled) { Unit
            } else {
                IconButton(
                    onClick = {onFilter()}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_filter_24px),
                        contentDescription = "filter"
                    )
                }
                IconButton(
                    onClick = {onSort()}
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sort_24px),
                        contentDescription = "sort"
                    )
                }
            }
        },
        windowInsets = WindowInsets(0),
//                expandedHeight = 1.dp,
//                colors = TODO(),
        scrollBehavior = scrollBehavior
    )
}