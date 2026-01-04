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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Search
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.MultiSelectCard
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.common.SelectableCard
import jp.ikigai.cash.flow.utils.getHighlightedString

@Composable
fun SelectAccountSheet(
    index: Int,
    selectedAccountId: Long,
    setSelectedAccount: (AccountWithTransactionMetadata) -> Unit,
    accounts: List<AccountWithTransactionMetadata>,
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

    val accountList by remember {
        mutableStateOf(
            accounts.map { account ->
                val highlightedString = getHighlightedString(account.accountName, "")
                Pair(
                    account,
                    highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}"))
                )
            }
        )
    }

    var filteredAccountList by remember {
        mutableStateOf(accountList)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredAccountList = if (searchText.isBlank()) {
            accountList
        } else {
            accountList
                .filter {
                    it.first.accountName.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    val highlightedString = getHighlightedString(it.first.accountName, searchText)
                    Pair(
                        it.first,
                        highlightedString.plus(AnnotatedString(" - ${it.first.formattedBalance}"))
                    )
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
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredAccountList,
                key = { (account, _) -> "account-${account.accountId}" }
            ) { (account, annotatedName) ->
                SelectableCard(
                    checked = { account.accountId == selectedAccountId },
                    label = annotatedName,
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedAccount(account)
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredAccountList.isEmpty()) {
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
fun SelectAccountLandscapeSheet(
    index: Int,
    selectedAccountId: Long,
    setSelectedAccount: (AccountWithTransactionMetadata) -> Unit,
    accounts: List<AccountWithTransactionMetadata>,
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

    val accountList by remember {
        mutableStateOf(
            accounts.map { account ->
                val highlightedString = getHighlightedString(account.accountName, "")
                Pair(
                    account,
                    highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}"))
                )
            }
        )
    }

    var filteredAccountList by remember {
        mutableStateOf(accountList)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredAccountList = if (searchText.isBlank()) {
            accountList
        } else {
            accountList
                .filter {
                    it.first.accountName.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    val highlightedString = getHighlightedString(it.first.accountName, searchText)
                    Pair(
                        it.first,
                        highlightedString.plus(AnnotatedString(" - ${it.first.formattedBalance}"))
                    )
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
                items = filteredAccountList,
                key = { (account, _) -> "account-${account.accountId}" }
            ) { (account, annotatedName) ->
                SelectableCard(
                    checked = { account.accountId == selectedAccountId },
                    label = annotatedName,
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedAccount(account)
                    },
                    modifier = Modifier.animateItem(),
                )
            }
            if (searchText.isNotBlank() && filteredAccountList.isEmpty()) {
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
fun FilterAccountSheet(
    selectedAccountsMap: Map<Long, Boolean>,
    accounts: List<AccountWithTransactionMetadata>,
    filter: (Map<Long, Boolean>) -> Unit,
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

    val accountList by remember {
        mutableStateOf(
            accounts.map { account ->
                val highlightedString = getHighlightedString(account.accountName, "")
                Pair(
                    account,
                    highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}"))
                )
            }
        )
    }

    var filteredAccountList by remember {
        mutableStateOf(accountList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredAccountList = if (searchText.isBlank()) {
            accountList
        } else {
            accountList
                .filter { (account, _) ->
                    account.accountName.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (account, _) ->
                    val highlightedString = getHighlightedString(account.accountName, searchText)
                    Pair(
                        account,
                        highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}")),
                    )
                }
        }
    }

    val selectedAccounts = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedAccounts.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredAccountList
                .map { (account, _) -> selectedAccounts[account.accountId] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedAccounts.putAll(selectedAccountsMap)
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
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredAccountList,
                key = { (account, _) -> "account-${account.accountId}" }
            ) { (account, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedAccounts.getOrDefault(account.accountId, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedAccounts[account.accountId] = !selectedAccounts[account.accountId]!!
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredAccountList.isEmpty()) {
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
                    val allSelected = filteredListSelectedCount == filteredAccountList.size
                    filteredAccountList
                        .map { (account, _) -> account.accountId }
                        .forEach { id -> selectedAccounts[id] = !allSelected }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                AnimatedToggleSelectIcon(
                    deselectVisible = filteredListSelectedCount == filteredAccountList.size
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
                    dismiss()
                    filter(selectedAccounts)
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
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
        }
    }
}

@Composable
fun FilterAccountLandscapeSheet(
    selectedAccountsMap: Map<Long, Boolean>,
    accounts: List<AccountWithTransactionMetadata>,
    filter: (Map<Long, Boolean>) -> Unit,
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

    val accountList by remember {
        mutableStateOf(
            accounts.map { account ->
                val highlightedString = getHighlightedString(account.accountName, "")
                Pair(
                    account,
                    highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}"))
                )
            }
        )
    }

    var filteredAccountList by remember {
        mutableStateOf(accountList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredAccountList = if (searchText.isBlank()) {
            accountList
        } else {
            accountList
                .filter { (account, _) ->
                    account.accountName.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (account, _) ->
                    val highlightedString = getHighlightedString(account.accountName, searchText)
                    Pair(
                        account,
                        highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}")),
                    )
                }
        }
    }

    val selectedAccounts = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedAccounts.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredAccountList
                .map { (account, _) -> selectedAccounts[account.accountId] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedAccounts.putAll(selectedAccountsMap)
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
                    val allSelected = filteredListSelectedCount == filteredAccountList.size
                    filteredAccountList
                        .map { (account, _) -> account.accountId }
                        .forEach { id -> selectedAccounts[id] = !allSelected }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(
                    text = if (filteredListSelectedCount == filteredAccountList.size) {
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
                    filter(selectedAccounts)
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
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(
                items = filteredAccountList,
                key = { (account, _) -> "account-${account.accountId}" }
            ) { (account, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedAccounts.getOrDefault(account.accountId, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedAccounts[account.accountId] =
                            !selectedAccounts[account.accountId]!!
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredAccountList.isEmpty()) {
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
fun FilterAccountSheet(
    selectedAccountIds: Set<Long>,
    accounts: List<AccountWithTransactionMetadata>,
    filter: (Set<Long>) -> Unit,
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

    val accountList by remember {
        mutableStateOf(
            accounts.map { account ->
                val highlightedString = getHighlightedString(account.accountName, "")
                Pair(
                    account,
                    highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}"))
                )
            }
        )
    }

    var filteredAccountList by remember {
        mutableStateOf(accountList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredAccountList = if (searchText.isBlank()) {
            accountList
        } else {
            accountList
                .filter { (account, _) ->
                    account.accountName.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (account, _) ->
                    val highlightedString = getHighlightedString(account.accountName, searchText)
                    Pair(
                        account,
                        highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}")),
                    )
                }
        }
    }

    val selectedAccounts = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedAccounts.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredAccountList
                .map { (account, _) -> selectedAccounts[account.accountId] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        accounts.forEach { account ->
            selectedAccounts[account.accountId] = selectedAccountIds.contains(account.accountId)
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
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredAccountList,
                key = { (account, _) -> "account-${account.accountId}" }
            ) { (account, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedAccounts.getOrDefault(account.accountId, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    onClick = { newCheckState ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedAccounts[account.accountId] = newCheckState
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredAccountList.isEmpty()) {
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
                    val allSelected = filteredListSelectedCount == filteredAccountList.size
                    filteredAccountList
                        .map { (account, _) -> account.accountId }
                        .forEach { id -> selectedAccounts[id] = !allSelected }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                AnimatedToggleSelectIcon(
                    deselectVisible = filteredListSelectedCount == filteredAccountList.size
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
                        selectedAccounts.filter { entry -> entry.value }.keys
                    )
                    dismiss()
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
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
        }
    }
}

@Composable
fun FilterAccountLandscapeSheet(
    selectedAccountIds: Set<Long>,
    accounts: List<AccountWithTransactionMetadata>,
    filter: (Set<Long>) -> Unit,
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

    val accountList by remember {
        mutableStateOf(
            accounts.map { account ->
                val highlightedString = getHighlightedString(account.accountName, "")
                Pair(
                    account,
                    highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}"))
                )
            }
        )
    }

    var filteredAccountList by remember {
        mutableStateOf(accountList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredAccountList = if (searchText.isBlank()) {
            accountList
        } else {
            accountList
                .filter { (account, _) ->
                    account.accountName.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (account, _) ->
                    val highlightedString = getHighlightedString(account.accountName, searchText)
                    Pair(
                        account,
                        highlightedString.plus(AnnotatedString(" - ${account.formattedBalance}")),
                    )
                }
        }
    }

    val selectedAccounts = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedAccounts.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredAccountList
                .map { (account, _) -> selectedAccounts[account.accountId] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        accounts.forEach { account ->
            selectedAccounts[account.accountId] = selectedAccountIds.contains(account.accountId)
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
            FilledTonalIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val allSelected = filteredListSelectedCount == filteredAccountList.size
                    filteredAccountList
                        .map { (account, _) -> account.accountId }
                        .forEach { id -> selectedAccounts[id] = !allSelected }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(
                    text = if (filteredListSelectedCount == filteredAccountList.size) {
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
                        selectedAccounts.filter { entry -> entry.value }.keys
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
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = filteredAccountList,
                key = { (account, _) -> "account-${account.accountId}" }
            ) { (account, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedAccounts.getOrDefault(account.accountId, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    onClick = { newCheckState ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedAccounts[account.accountId] = newCheckState
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredAccountList.isEmpty()) {
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
