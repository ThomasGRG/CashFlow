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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.dto.SelectTemplateInfoDTO
import jp.ikigai.cash.flow.ui.components.cards.SelectTemplateCard
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.utils.getHighlightedString

@Composable
fun SelectTemplateSheet(
    templates: List<SelectTemplateInfoDTO>,
    addNewTransaction: (Long) -> Unit,
    dismiss: () -> Unit,
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

    var filteredTemplates by remember {
        mutableStateOf(templates)
    }

    LaunchedEffect(key1 = searchText) {
        filteredTemplates = if (searchText.isBlank()) {
            templates
        } else {
            templates
                .filter {
                    it.annotatedName.text.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    it.copy(
                        annotatedName = getHighlightedString(it.annotatedName.text, searchText)
                    )
                }
        }
    }

    Column(
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 14.dp),
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
        LazyColumn(
            modifier = Modifier.heightIn(max = 240.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredTemplates,
                key = { template -> "template-${template.id}" }
            ) { template ->
                SelectTemplateCard(
                    data = template,
                    onClick = { id ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        addNewTransaction(id)
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredTemplates.isEmpty()) {
                item(
                    key = "no_results"
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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
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
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.search_field_label))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    addNewTransaction(0L)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.new_transaction_button_label))
            }
        }
    }
}

@Composable
fun SelectTemplateLandscapeSheet(
    templates: List<SelectTemplateInfoDTO>,
    addNewTransaction: (Long) -> Unit,
    dismiss: () -> Unit,
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

    var filteredTemplates by remember {
        mutableStateOf(templates)
    }

    LaunchedEffect(key1 = searchText) {
        filteredTemplates = if (searchText.isBlank()) {
            templates
        } else {
            templates
                .filter {
                    it.annotatedName.text.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    it.copy(
                        annotatedName = getHighlightedString(it.annotatedName.text, searchText)
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
                    addNewTransaction(0L)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.new_transaction_button_label))
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = filteredTemplates,
                key = { template -> "template-${template.id}" }
            ) { template ->
                SelectTemplateCard(
                    data = template,
                    onClick = { id ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        addNewTransaction(id)
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredTemplates.isEmpty()) {
                item(
                    key = "no_results"
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
fun SelectTemplateSheetPreview() {
    SelectTemplateSheet(
        templates = listOf(
            SelectTemplateInfoDTO(
                id = 0L,
                annotatedName = AnnotatedString("Split expenses"),
                frequency = "1"
            ),
            SelectTemplateInfoDTO(
                id = 1L,
                annotatedName = AnnotatedString("Server"),
                frequency = "145"
            ),
            SelectTemplateInfoDTO(
                id = 2L,
                annotatedName = AnnotatedString("Hotel Al Hayat"),
                frequency = "3,251"
            ),
            SelectTemplateInfoDTO(
                id = 3L,
                annotatedName = AnnotatedString("Groceries"),
                frequency = "10"
            )
        ),
        addNewTransaction = {},
        dismiss = {}
    )
}