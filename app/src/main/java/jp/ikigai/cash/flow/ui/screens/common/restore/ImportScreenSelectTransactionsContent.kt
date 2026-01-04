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

package jp.ikigai.cash.flow.ui.screens.common.restore

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.ui.components.cards.TransactionCard
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.common.TransactionGroupHeader
import java.time.LocalDate

@Composable
fun ImportScreenSelectTransactionsContent(
    loading: Boolean,
    searchText: String,
    setSearchText: (String) -> Unit,
    enabled: Boolean,
    focusRequester: FocusRequester,
    interactionSource: MutableInteractionSource,
    transactions: Map<LocalDate, List<TransactionWithChips>>,
    selectedLocalDates: Set<LocalDate>,
    enabledLocalDates: Set<LocalDate>,
    enabledTempTransactions: Set<Long>,
    selectedTransactions: Set<Long>,
    toggleLocalDateSelected: (Boolean, List<TransactionWithChips>) -> Unit,
    toggleTransactionSelected: (Long) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            SearchBox(
                modifier = Modifier
                    .padding(
                        top = 5.dp,
                        start = 10.dp,
                        end = 10.dp,
                        bottom = 10.dp
                    ),
                searchText = searchText,
                setSearchText = setSearchText,
                enabled = enabled,
                focusRequester = focusRequester,
                interactionSource = interactionSource
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                transactions.forEach { entry ->
                    stickyHeader {
                        TransactionGroupHeader(
                            date = entry.key,
                            selected = selectedLocalDates.contains(entry.key),
                            enabled = enabled && enabledLocalDates.contains(
                                entry.key
                            ),
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                toggleLocalDateSelected(
                                    selectedLocalDates.contains(entry.key),
                                    transactions[entry.key] ?: emptyList()
                                )
                            }
                        )
                    }
                    items(
                        items = entry.value,
                        key = { transactionWithChips -> transactionWithChips.id }
                    ) { transactionWithChips ->
                        TransactionCard(
                            checked = enabledTempTransactions.contains(
                                transactionWithChips.id
                            ) && selectedTransactions.contains(
                                transactionWithChips.id
                            ),
                            enabled = enabled && enabledTempTransactions.contains(
                                transactionWithChips.id
                            ),
                            transactionWithChips = transactionWithChips,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                toggleTransactionSelected(transactionWithChips.id)
                            },
                            onLongClick = {},
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
        if (!loading && transactions.isEmpty()) {
            Text(
                text = if (searchText.isNotBlank()) {
                    stringResource(
                        id = R.string.no_results_found_search_placeholder_label,
                        searchText
                    )
                } else {
                    stringResource(id = R.string.no_results_found_filters_placeholder_label)
                },
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
fun ImportScreenSelectTransactionsContent(
    loading: Boolean,
    searchText: String,
    enabled: Boolean,
    transactions: Map<LocalDate, List<TransactionWithChips>>,
    selectedLocalDates: Set<LocalDate>,
    enabledLocalDates: Set<LocalDate>,
    enabledTempTransactions: Set<Long>,
    selectedTransactions: Set<Long>,
    toggleLocalDateSelected: (Boolean, List<TransactionWithChips>) -> Unit,
    toggleTransactionSelected: (Long) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            transactions.forEach { entry ->
                stickyHeader {
                    TransactionGroupHeader(
                        date = entry.key,
                        selected = selectedLocalDates.contains(entry.key),
                        enabled = enabled && enabledLocalDates.contains(
                            entry.key
                        ),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            toggleLocalDateSelected(
                                selectedLocalDates.contains(entry.key),
                                transactions[entry.key] ?: emptyList()
                            )
                        }
                    )
                }
                items(
                    items = entry.value,
                    key = { transactionWithChips -> transactionWithChips.id }
                ) { transactionWithChips ->
                    TransactionCard(
                        checked = enabledTempTransactions.contains(
                            transactionWithChips.id
                        ) && selectedTransactions.contains(
                            transactionWithChips.id
                        ),
                        enabled = enabled && enabledTempTransactions.contains(
                            transactionWithChips.id
                        ),
                        transactionWithChips = transactionWithChips,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            toggleTransactionSelected(transactionWithChips.id)
                        },
                        onLongClick = {},
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
        if (!loading && transactions.isEmpty()) {
            Text(
                text = if (searchText.isNotBlank()) {
                    stringResource(
                        id = R.string.no_results_found_search_placeholder_label,
                        searchText
                    )
                } else {
                    stringResource(id = R.string.no_results_found_filters_placeholder_label)
                },
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}