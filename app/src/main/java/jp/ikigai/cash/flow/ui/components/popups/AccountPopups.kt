package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.MultiSelectCard
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.common.SelectableCard
import jp.ikigai.cash.flow.utils.getHighlightedString

@Composable
fun SelectAccountPopup(
    index: Int,
    selectedAccountId: Long,
    setSelectedAccount: (Account) -> Unit,
    accounts: List<Account>,
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
                val highlightedString = getHighlightedString(account.name, "")
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
                    it.first.name.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    val highlightedString = getHighlightedString(it.first.name, searchText)
                    Pair(
                        it.first,
                        highlightedString.plus(AnnotatedString(" - ${it.first.formattedBalance}"))
                    )
                }
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
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
                key = { (account, _) -> "account-${account.id}" }
            ) { (account, annotatedName) ->
                SelectableCard(
                    checked = { account.id == selectedAccountId },
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
fun FilterAccountPopup(
    selectedAccountsMap: Map<Long, Boolean>,
    accounts: List<Account>,
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
                val highlightedString = getHighlightedString(account.name, "")
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
                    account.name.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (account, _) ->
                    val highlightedString = getHighlightedString(account.name, searchText)
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
                .map { (account, _) -> selectedAccounts[account.id] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedAccounts.putAll(selectedAccountsMap)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(20.dp),
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
                key = { (account, _) -> "account-${account.id}" }
            ) { (account, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedAccounts.getOrDefault(account.id, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedAccounts[account.id] = !selectedAccounts[account.id]!!
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
                            id = R.string.choose_icon_screen_empty_placeholder_label,
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
                        .map { (account, _) -> account.id }
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
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
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
fun FilterAccountPopup(
    selectedAccountIds: Set<Long>,
    accounts: List<Account>,
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
                val highlightedString = getHighlightedString(account.name, "")
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
                    account.name.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (account, _) ->
                    val highlightedString = getHighlightedString(account.name, searchText)
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
                .map { (account, _) -> selectedAccounts[account.id] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        accounts.forEach { account ->
            selectedAccounts[account.id] = selectedAccountIds.contains(account.id)
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(20.dp),
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
                key = { (account, _) -> "account-${account.id}" }
            ) { (account, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedAccounts.getOrDefault(account.id, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    onClick = { newCheckState ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedAccounts[account.id] = newCheckState
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
                            id = R.string.choose_icon_screen_empty_placeholder_label,
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
                        .map { (account, _) -> account.id }
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
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
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
