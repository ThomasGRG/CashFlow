package jp.ikigai.cash.flow.ui.components.bottomsheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.MultiSelectCard
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.common.SelectableCard
import jp.ikigai.cash.flow.utils.getHighlightedString

@Composable
fun SelectCounterPartySheet(
    index: Int,
    selectedCounterPartyId: Long,
    setSelectedCounterParty: (CounterPartyWithTransactionMetadata) -> Unit,
    counterParties: List<CounterPartyWithTransactionMetadata>,
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

    val counterPartyList by remember {
        mutableStateOf(
            counterParties.map { counterParty ->
                Pair(counterParty, getHighlightedString(counterParty.counterPartyName, ""))
            }
        )
    }

    var filteredCounterPartyList by remember {
        mutableStateOf(counterPartyList)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCounterPartyList = if (searchText.isBlank()) {
            counterPartyList
        } else {
            counterPartyList
                .filter {
                    it.first.counterPartyName.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    Pair(it.first, getHighlightedString(it.first.counterPartyName, searchText))
                }
        }
    }

    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 14.dp),
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
            modifier = Modifier.height(230.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredCounterPartyList,
                key = { (counterParty, _) -> "counterParty-${counterParty.counterPartyId}" }
            ) { (counterParty, annotatedName) ->
                SelectableCard(
                    checked = { counterParty.counterPartyId == selectedCounterPartyId },
                    label = annotatedName,
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedCounterParty(counterParty)
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCounterPartyList.isEmpty()) {
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
                            .padding(10.dp)
                            .animateItem()
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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
        }
    }
}

@Composable
fun SelectCounterPartyLandscapeSheet(
    index: Int,
    selectedCounterPartyId: Long,
    setSelectedCounterParty: (CounterPartyWithTransactionMetadata) -> Unit,
    counterParties: List<CounterPartyWithTransactionMetadata>,
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

    val counterPartyList by remember {
        mutableStateOf(
            counterParties.map { counterParty ->
                Pair(counterParty, getHighlightedString(counterParty.counterPartyName, ""))
            }
        )
    }

    var filteredCounterPartyList by remember {
        mutableStateOf(counterPartyList)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCounterPartyList = if (searchText.isBlank()) {
            counterPartyList
        } else {
            counterPartyList
                .filter {
                    it.first.counterPartyName.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    Pair(it.first, getHighlightedString(it.first.counterPartyName, searchText))
                }
        }
    }

    Row(
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
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
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = filteredCounterPartyList,
                key = { (counterParty, _) -> "counterParty-${counterParty.counterPartyId}" }
            ) { (counterParty, annotatedName) ->
                SelectableCard(
                    checked = { counterParty.counterPartyId == selectedCounterPartyId },
                    label = annotatedName,
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedCounterParty(counterParty)
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCounterPartyList.isEmpty()) {
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
                            .padding(10.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Composable
fun MigrateCounterPartySheet(
    migrateCount: Int,
    selectCounterParty: (CounterPartyWithTransactionMetadata) -> Unit,
    counterParties: List<CounterPartyWithTransactionMetadata>,
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

    val counterPartyList by remember {
        mutableStateOf(
            counterParties.map { counterParty ->
                Pair(counterParty, getHighlightedString(counterParty.counterPartyName, ""))
            }
        )
    }

    var filteredCounterPartyList by remember {
        mutableStateOf(counterPartyList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCounterPartyList = if (searchText.isBlank()) {
            counterPartyList
        } else {
            counterPartyList
                .filter { (counterParty, _) ->
                    counterParty.counterPartyName.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map { (counterParty, _) ->
                    Pair(
                        counterParty,
                        getHighlightedString(counterParty.counterPartyName, searchText)
                    )
                }
        }
    }

    var selectedCounterParty by remember {
        mutableStateOf(
            CounterPartyWithTransactionMetadata(
                counterPartyId = 0,
                counterPartyName = "",
                transactionCount = 0,
                lastUsed = null
            )
        )
    }

    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 14.dp),
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
            modifier = Modifier.height(230.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredCounterPartyList,
                key = { (counterParty, _) -> "counterParty-${counterParty.counterPartyId}" }
            ) { (counterParty, annotatedName) ->
                SelectableCard(
                    checked = { counterParty.counterPartyId == selectedCounterParty.counterPartyId },
                    label = annotatedName,
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCounterParty = counterParty
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCounterPartyList.isEmpty()) {
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
                            .padding(10.dp)
                            .animateItem()
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
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
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.search_field_label))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    selectCounterParty(selectedCounterParty)
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedCounterParty.counterPartyId > 0,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        id = R.string.migrate_with_count_button_label,
                        migrateCount
                    )
                )
            }
        }
    }
}

@Composable
fun MigrateCounterPartyLandscapeSheet(
    migrateCount: Int,
    selectCounterParty: (CounterPartyWithTransactionMetadata) -> Unit,
    counterParties: List<CounterPartyWithTransactionMetadata>,
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

    val counterPartyList by remember {
        mutableStateOf(
            counterParties.map { counterParty ->
                Pair(counterParty, getHighlightedString(counterParty.counterPartyName, ""))
            }
        )
    }

    var filteredCounterPartyList by remember {
        mutableStateOf(counterPartyList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCounterPartyList = if (searchText.isBlank()) {
            counterPartyList
        } else {
            counterPartyList
                .filter { (counterParty, _) ->
                    counterParty.counterPartyName.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map { (counterParty, _) ->
                    Pair(
                        counterParty,
                        getHighlightedString(counterParty.counterPartyName, searchText)
                    )
                }
        }
    }

    var selectedCounterParty by remember {
        mutableStateOf(
            CounterPartyWithTransactionMetadata(
                counterPartyId = 0,
                counterPartyName = "",
                transactionCount = 0,
                lastUsed = null
            )
        )
    }

    Row(
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
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
                    selectCounterParty(selectedCounterParty)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedCounterParty.counterPartyId > 0,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        id = R.string.migrate_with_count_button_label,
                        migrateCount
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(2f)
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = filteredCounterPartyList,
                key = { (counterParty, _) -> "counterParty-${counterParty.counterPartyId}" }
            ) { (counterParty, annotatedName) ->
                SelectableCard(
                    checked = { counterParty.counterPartyId == selectedCounterParty.counterPartyId },
                    label = annotatedName,
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCounterParty = counterParty
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCounterPartyList.isEmpty()) {
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
                            .padding(10.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Composable
fun FilterCounterPartySheet(
    selectedCounterPartyMap: Map<Long, Boolean>,
    includeNoCounterPartyTransactions: Boolean,
    counterParties: List<CounterPartyWithTransactionMetadata>,
    filter: (Map<Long, Boolean>, Boolean) -> Unit,
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

    var includeTransactionsWithNoCounterParty by remember(includeNoCounterPartyTransactions) {
        mutableStateOf(includeNoCounterPartyTransactions)
    }

    val counterPartyList by remember {
        mutableStateOf(
            counterParties.map { counterParty ->
                Pair(counterParty, getHighlightedString(counterParty.counterPartyName, ""))
            }
        )
    }

    var filteredCounterPartyList by remember {
        mutableStateOf(counterPartyList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCounterPartyList = if (searchText.isBlank()) {
            counterPartyList
        } else {
            counterPartyList
                .filter { (counterParty, _) ->
                    counterParty.counterPartyName.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (counterParty, _) ->
                    Pair(
                        counterParty,
                        getHighlightedString(counterParty.counterPartyName, searchText)
                    )
                }
        }
    }

    val selectedCounterParties = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedCounterParties.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredCounterPartyList
                .map { (counterParty, _) -> selectedCounterParties[counterParty.counterPartyId] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedCounterParties.putAll(selectedCounterPartyMap)
    }

    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 14.dp),
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
            modifier = Modifier.height(230.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredCounterPartyList,
                key = { (counterParty, _) -> "counterParty-${counterParty.counterPartyId}" }
            ) { (counterParty, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedCounterParties.getOrDefault(counterParty.counterPartyId, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCounterParties[counterParty.counterPartyId] =
                            !selectedCounterParties[counterParty.counterPartyId]!!
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCounterPartyList.isEmpty()) {
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
                            .padding(10.dp)
                            .animateItem()
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    includeTransactionsWithNoCounterParty = !includeTransactionsWithNoCounterParty
                }
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.no_counter_party_label),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .weight(1f, fill = false)
            )
            Switch(
                checked = includeTransactionsWithNoCounterParty,
                onCheckedChange = null,
                thumbContent = {
                    Icon(
                        imageVector = if (includeTransactionsWithNoCounterParty) Icons.Filled.Check else Icons.Filled.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val allSelected = filteredListSelectedCount == filteredCounterPartyList.size
                    filteredCounterPartyList
                        .map { (counterParty, _) -> counterParty.counterPartyId }
                        .forEach { id -> selectedCounterParties[id] = !allSelected }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                AnimatedToggleSelectIcon(
                    deselectVisible = filteredListSelectedCount == filteredCounterPartyList.size
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
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    filter(selectedCounterParties, includeTransactionsWithNoCounterParty)
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedCount > 0 || includeTransactionsWithNoCounterParty,
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
fun FilterCounterPartyLandscapeSheet(
    selectedCounterPartyMap: Map<Long, Boolean>,
    includeNoCounterPartyTransactions: Boolean,
    counterParties: List<CounterPartyWithTransactionMetadata>,
    filter: (Map<Long, Boolean>, Boolean) -> Unit,
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

    var includeTransactionsWithNoCounterParty by remember(includeNoCounterPartyTransactions) {
        mutableStateOf(includeNoCounterPartyTransactions)
    }

    val counterPartyList by remember {
        mutableStateOf(
            counterParties.map { counterParty ->
                Pair(counterParty, getHighlightedString(counterParty.counterPartyName, ""))
            }
        )
    }

    var filteredCounterPartyList by remember {
        mutableStateOf(counterPartyList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCounterPartyList = if (searchText.isBlank()) {
            counterPartyList
        } else {
            counterPartyList
                .filter { (counterParty, _) ->
                    counterParty.counterPartyName.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (counterParty, _) ->
                    Pair(
                        counterParty,
                        getHighlightedString(counterParty.counterPartyName, searchText)
                    )
                }
        }
    }

    val selectedCounterParties = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedCounterParties.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredCounterPartyList
                .map { (counterParty, _) -> selectedCounterParties[counterParty.counterPartyId] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedCounterParties.putAll(selectedCounterPartyMap)
    }

    Row(
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
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
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        includeTransactionsWithNoCounterParty =
                            !includeTransactionsWithNoCounterParty
                    }
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.no_counter_party_label),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .weight(1f, fill = false)
                )
                Switch(
                    checked = includeTransactionsWithNoCounterParty,
                    onCheckedChange = null,
                    thumbContent = {
                        Icon(
                            imageVector = if (includeTransactionsWithNoCounterParty) Icons.Filled.Check else Icons.Filled.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val allSelected = filteredListSelectedCount == filteredCounterPartyList.size
                    filteredCounterPartyList
                        .map { (counterParty, _) -> counterParty.counterPartyId }
                        .forEach { id -> selectedCounterParties[id] = !allSelected }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(
                    text = if (filteredListSelectedCount == filteredCounterPartyList.size) {
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
                    dismiss()
                    filter(selectedCounterParties, includeTransactionsWithNoCounterParty)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedCount > 0 || includeTransactionsWithNoCounterParty,
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
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = filteredCounterPartyList,
                key = { (counterParty, _) -> "counterParty-${counterParty.counterPartyId}" }
            ) { (counterParty, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedCounterParties.getOrDefault(counterParty.counterPartyId, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCounterParties[counterParty.counterPartyId] =
                            !selectedCounterParties[counterParty.counterPartyId]!!
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCounterPartyList.isEmpty()) {
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
                            .padding(10.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Composable
fun FilterCounterPartySheet(
    selectedCounterPartyIds: Set<Long>,
    includeNoCounterPartyTransactions: Boolean,
    counterParties: List<CounterPartyWithTransactionMetadata>,
    filter: (Set<Long>, Boolean) -> Unit,
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

    val counterPartyList by remember {
        mutableStateOf(
            counterParties.map { counterParty ->
                Pair(counterParty, getHighlightedString(counterParty.counterPartyName, ""))
            }
        )
    }

    var filteredCounterPartyList by remember {
        mutableStateOf(counterPartyList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCounterPartyList = if (searchText.isBlank()) {
            counterPartyList
        } else {
            counterPartyList
                .filter { (counterParty, _) ->
                    counterParty.counterPartyName.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (counterParty, _) ->
                    Pair(
                        counterParty,
                        getHighlightedString(counterParty.counterPartyName, searchText)
                    )
                }
        }
    }

    var includeTransactionsWithNoCounterParty by remember(includeNoCounterPartyTransactions) {
        mutableStateOf(includeNoCounterPartyTransactions)
    }

    val selectedCounterParties = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedCounterParties.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredCounterPartyList
                .map { (counterParty, _) -> selectedCounterParties[counterParty.counterPartyId] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedCounterParties.putAll(
            counterParties.associate { counterParty ->
                counterParty.counterPartyId to selectedCounterPartyIds.contains(counterParty.counterPartyId)
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, bottom = 14.dp),
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
            modifier = Modifier.height(230.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredCounterPartyList,
                key = { (counterParty, _) -> "counterParty-${counterParty.counterPartyId}" }
            ) { (counterParty, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedCounterParties.getOrDefault(counterParty.counterPartyId, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    onClick = { newCheckState ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCounterParties[counterParty.counterPartyId] = newCheckState
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCounterPartyList.isEmpty()) {
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
                            .padding(10.dp)
                            .animateItem()
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    includeTransactionsWithNoCounterParty = !includeTransactionsWithNoCounterParty
                }
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.no_counter_party_label),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .weight(1f, fill = false)
            )
            Switch(
                checked = includeTransactionsWithNoCounterParty,
                onCheckedChange = null,
                thumbContent = {
                    Icon(
                        imageVector = if (includeTransactionsWithNoCounterParty) Icons.Filled.Check else Icons.Filled.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val allSelected = filteredListSelectedCount == filteredCounterPartyList.size
                    filteredCounterPartyList
                        .map { (counterParty, _) -> counterParty.counterPartyId }
                        .forEach { id -> selectedCounterParties[id] = !allSelected }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                AnimatedToggleSelectIcon(
                    deselectVisible = filteredListSelectedCount == filteredCounterPartyList.size
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
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    filter(
                        selectedCounterParties.filter { entry -> entry.value }.keys,
                        includeTransactionsWithNoCounterParty
                    )
                    dismiss()
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedCount > 0 || includeTransactionsWithNoCounterParty,
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
fun FilterCounterPartyLandscapeSheet(
    selectedCounterPartyIds: Set<Long>,
    includeNoCounterPartyTransactions: Boolean,
    counterParties: List<CounterPartyWithTransactionMetadata>,
    filter: (Set<Long>, Boolean) -> Unit,
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

    val counterPartyList by remember {
        mutableStateOf(
            counterParties.map { counterParty ->
                Pair(counterParty, getHighlightedString(counterParty.counterPartyName, ""))
            }
        )
    }

    var filteredCounterPartyList by remember {
        mutableStateOf(counterPartyList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCounterPartyList = if (searchText.isBlank()) {
            counterPartyList
        } else {
            counterPartyList
                .filter { (counterParty, _) ->
                    counterParty.counterPartyName.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (counterParty, _) ->
                    Pair(
                        counterParty,
                        getHighlightedString(counterParty.counterPartyName, searchText)
                    )
                }
        }
    }

    var includeTransactionsWithNoCounterParty by remember(includeNoCounterPartyTransactions) {
        mutableStateOf(includeNoCounterPartyTransactions)
    }

    val selectedCounterParties = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedCounterParties.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredCounterPartyList
                .map { (counterParty, _) -> selectedCounterParties[counterParty.counterPartyId] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedCounterParties.putAll(
            counterParties.associate { counterParty ->
                counterParty.counterPartyId to selectedCounterPartyIds.contains(counterParty.counterPartyId)
            }
        )
    }

    Row(
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
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
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        includeTransactionsWithNoCounterParty =
                            !includeTransactionsWithNoCounterParty
                    }
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.no_counter_party_label),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .weight(1f, fill = false)
                )
                Switch(
                    checked = includeTransactionsWithNoCounterParty,
                    onCheckedChange = null,
                    thumbContent = {
                        Icon(
                            imageVector = if (includeTransactionsWithNoCounterParty) Icons.Filled.Check else Icons.Filled.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val allSelected = filteredListSelectedCount == filteredCounterPartyList.size
                    filteredCounterPartyList
                        .map { (counterParty, _) -> counterParty.counterPartyId }
                        .forEach { id -> selectedCounterParties[id] = !allSelected }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(
                    text = if (filteredListSelectedCount == filteredCounterPartyList.size) {
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
                        selectedCounterParties.filter { entry -> entry.value }.keys,
                        includeTransactionsWithNoCounterParty
                    )
                    dismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedCount > 0 || includeTransactionsWithNoCounterParty,
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
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = filteredCounterPartyList,
                key = { (counterParty, _) -> "counterParty-${counterParty.counterPartyId}" }
            ) { (counterParty, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedCounterParties.getOrDefault(counterParty.counterPartyId, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    onClick = { newCheckState ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCounterParties[counterParty.counterPartyId] = newCheckState
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCounterPartyList.isEmpty()) {
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
                            .padding(10.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}
