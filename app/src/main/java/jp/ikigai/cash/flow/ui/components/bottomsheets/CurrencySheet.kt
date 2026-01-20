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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Search
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.CurrencyInfo
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.MultiSelectCard
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.common.SelectableCard
import jp.ikigai.cash.flow.utils.getHighlightedString

@Composable
fun CurrencySheet(
    index: Int,
    selectedCurrency: String,
    setSelectedCurrency: (String) -> Unit,
    currencies: List<CurrencyInfo>,
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

    val listState = rememberLazyListState()

    var filteredCurrencies by remember {
        mutableStateOf(currencies)
    }

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCurrencies = if (searchText.isBlank()) {
            currencies
        } else {
            currencies
                .filter {
                    it.annotatedString.text.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    it.copy(
                        annotatedString = getHighlightedString(it.annotatedString.text, searchText)
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
            state = listState,
            modifier = Modifier.heightIn(max = 240.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredCurrencies,
                key = { currencyInfo -> "currency-${currencyInfo.currency.currencyCode}" }
            ) { currencyInfo ->
                SelectableCard(
                    checked = { currencyInfo.currency.currencyCode == selectedCurrency },
                    label = currencyInfo.annotatedString,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSelectedCurrency(currencyInfo.currency.currencyCode)
                        dismiss()
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCurrencies.isEmpty()) {
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
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
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
        }
    }
}

@Composable
fun CurrencyLandscapeSheet(
    index: Int,
    selectedCurrency: String,
    setSelectedCurrency: (String) -> Unit,
    currencies: List<CurrencyInfo>,
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

    val listState = rememberLazyListState()

    var filteredCurrencies by remember {
        mutableStateOf(currencies)
    }

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCurrencies = if (searchText.isBlank()) {
            currencies
        } else {
            currencies
                .filter {
                    it.annotatedString.text.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    it.copy(
                        annotatedString = getHighlightedString(it.annotatedString.text, searchText)
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
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = filteredCurrencies,
                key = { currencyInfo -> "currency-${currencyInfo.currency.currencyCode}" }
            ) { currencyInfo ->
                SelectableCard(
                    checked = { currencyInfo.currency.currencyCode == selectedCurrency },
                    label = currencyInfo.annotatedString,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setSelectedCurrency(currencyInfo.currency.currencyCode)
                        dismiss()
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCurrencies.isEmpty()) {
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

@Composable
fun FilterCurrencySheet(
    selectedCurrencyCodes: Set<String>,
    currencies: List<CurrencyInfo>,
    filter: (Set<String>) -> Unit,
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

    var filteredCurrencies by remember {
        mutableStateOf(currencies)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCurrencies = if (searchText.isBlank()) {
            currencies
        } else {
            currencies
                .filter {
                    it.annotatedString.text.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    it.copy(
                        annotatedString = getHighlightedString(it.annotatedString.text, searchText)
                    )
                }
        }
    }

    val selectedCurrencies = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedCurrencies.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredCurrencies
                .map { selectedCurrencies[it.currency.currencyCode] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        currencies.forEach { (_, currency) ->
            selectedCurrencies[currency.currencyCode] =
                selectedCurrencyCodes.contains(currency.currencyCode)
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
                items = filteredCurrencies,
                key = { currencyInfo -> "currency-${currencyInfo.currency.currencyCode}" }
            ) { currencyInfo ->
                MultiSelectCard(
                    checked = {
                        selectedCurrencies.getOrDefault(
                            currencyInfo.currency.currencyCode,
                            true
                        )
                    },
                    label = currencyInfo.annotatedString,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCurrencies[currencyInfo.currency.currencyCode] =
                            !selectedCurrencies[currencyInfo.currency.currencyCode]!!
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCurrencies.isEmpty()) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp)
                    .weight(1f, fill = true),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val allSelected = filteredListSelectedCount == filteredCurrencies.size
                    filteredCurrencies
                        .map { currencyInfo -> currencyInfo.currency.currencyCode }
                        .forEach { currencyCode -> selectedCurrencies[currencyCode] = !allSelected }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                AnimatedToggleSelectIcon(
                    deselectVisible = filteredListSelectedCount == filteredCurrencies.size
                )
            }
            FilledTonalIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isFocused) {
                        keyboardController?.show()
                    } else {
                        focusRequester.requestFocus()
                    }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Icon(
                    imageVector = TablerIcons.Search,
                    contentDescription = "Search"
                )
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    filter(
                        selectedCurrencies.filter { entry -> entry.value }.keys
                    )
                    dismiss()
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp)
                    .weight(1f, fill = true),
                shape = RoundedCornerShape(35),
                enabled = selectedCount > 0,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        id = R.string.filter_button_with_count_label,
                        selectedCount
                    )
                )
            }
        }
    }
}

@Composable
fun FilterCurrencyLandscapeSheet(
    selectedCurrencyCodes: Set<String>,
    currencies: List<CurrencyInfo>,
    filter: (Set<String>) -> Unit,
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

    var filteredCurrencies by remember {
        mutableStateOf(currencies)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCurrencies = if (searchText.isBlank()) {
            currencies
        } else {
            currencies
                .filter {
                    it.annotatedString.text.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    it.copy(
                        annotatedString = getHighlightedString(it.annotatedString.text, searchText)
                    )
                }
        }
    }

    val selectedCurrencies = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedCurrencies.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredCurrencies
                .map { selectedCurrencies[it.currency.currencyCode] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        currencies.forEach { (_, currency) ->
            selectedCurrencies[currency.currencyCode] =
                selectedCurrencyCodes.contains(currency.currencyCode)
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
                    val allSelected = filteredListSelectedCount == filteredCurrencies.size
                    filteredCurrencies
                        .map { currencyInfo -> currencyInfo.currency.currencyCode }
                        .forEach { currencyCode -> selectedCurrencies[currencyCode] = !allSelected }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(
                    text = if (filteredListSelectedCount == filteredCurrencies.size) {
                        stringResource(id = R.string.unselect_all_button_label)
                    } else {
                        stringResource(id = R.string.select_all_button_label)
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    filter(
                        selectedCurrencies.filter { entry -> entry.value }.keys
                    )
                    dismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedCount > 0,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        id = R.string.filter_button_with_count_label,
                        selectedCount
                    )
                )
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
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = filteredCurrencies,
                key = { currencyInfo -> "currency-${currencyInfo.currency.currencyCode}" }
            ) { currencyInfo ->
                MultiSelectCard(
                    checked = {
                        selectedCurrencies.getOrDefault(
                            currencyInfo.currency.currencyCode,
                            true
                        )
                    },
                    label = currencyInfo.annotatedString,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCurrencies[currencyInfo.currency.currencyCode] =
                            !selectedCurrencies[currencyInfo.currency.currencyCode]!!
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCurrencies.isEmpty()) {
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
fun CurrencySheetPreview() {
    CurrencySheet(
        index = 1,
        selectedCurrency = "INR",
        setSelectedCurrency = {},
        currencies = Constants.currencyList,
        dismiss = {}
    )
}