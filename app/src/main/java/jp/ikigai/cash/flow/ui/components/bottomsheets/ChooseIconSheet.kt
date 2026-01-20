/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui.components.bottomsheets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.AllIcons
import compose.icons.TablerIcons
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import kotlinx.coroutines.delay

@Composable
fun ChooseIconSheet(
    dismiss: () -> Unit,
    setIcon: (ImageVector) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember {
        FocusRequester()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isFocused by interactionSource.collectIsFocusedAsState()

    var searchText by remember {
        mutableStateOf("")
    }

    val icons by remember {
        mutableStateOf(TablerIcons.AllIcons)
    }

    var filteredIconList by remember {
        mutableStateOf(icons)
    }

    LaunchedEffect(key1 = searchText) {
        delay(300)
        filteredIconList = if (searchText.isBlank()) {
            icons
        } else {
            icons
                .filter { icon ->
                    icon.name.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
        }
    }

    Column(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchBox(
            searchText = searchText,
            setSearchText = {
                searchText = it
            },
            focusRequester = focusRequester,
            interactionSource = interactionSource
        )
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(58.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
                .animateContentSize()
        ) {
            items(
                items = filteredIconList,
                key = { icon -> "icon-${icon.name}" }
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = icon.name,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(52.dp)
                        .padding(6.dp)
                        .clickable(
                            enabled = true,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                setIcon(icon)
                                dismiss()
                            },
                        )
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredIconList.isEmpty()) {
                item(
                    key = "no_results",
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.no_results_found_search_placeholder_label,
                            searchText
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 50.dp)
                            .animateItem()
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isFocused) {
                        keyboardController?.show()
                    } else {
                        focusRequester.requestFocus()
                    }
                },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.search_field_label))
            }
        }
    }
}

@Composable
fun ChooseIconLandscapeSheet(
    dismiss: () -> Unit,
    setIcon: (ImageVector) -> Unit
) {
    val haptics = LocalHapticFeedback.current

    val focusRequester = remember {
        FocusRequester()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    var searchText by remember {
        mutableStateOf("")
    }

    val icons by remember {
        mutableStateOf(TablerIcons.AllIcons)
    }

    var filteredIconList by remember {
        mutableStateOf(icons)
    }

    LaunchedEffect(key1 = searchText) {
        delay(300)
        filteredIconList = if (searchText.isBlank()) {
            icons
        } else {
            icons
                .filter { icon ->
                    icon.name.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 10.dp, bottom = 10.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start,
        ) {
            SearchBox(
                searchText = searchText,
                setSearchText = {
                    searchText = it
                },
                focusRequester = focusRequester,
                interactionSource = interactionSource,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
        }
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(58.dp),
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
        ) {
            items(
                items = filteredIconList,
                key = { icon -> "icon-${icon.name}" }
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = icon.name,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(52.dp)
                        .padding(6.dp)
                        .clickable(
                            enabled = true,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                setIcon(icon)
                                dismiss()
                            },
                        )
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredIconList.isEmpty()) {
                item(
                    key = "no_results",
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.no_results_found_search_placeholder_label,
                            searchText
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 50.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChooseIconSheetPreview() {
    ChooseIconSheet(
        dismiss = {},
        setIcon = {}
    )
}