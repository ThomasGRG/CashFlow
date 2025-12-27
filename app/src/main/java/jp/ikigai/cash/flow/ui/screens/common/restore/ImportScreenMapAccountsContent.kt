package jp.ikigai.cash.flow.ui.screens.common.restore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.InfoCircle
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TempAccountWithFormattedBalance
import jp.ikigai.cash.flow.ui.components.cards.MapAccountCard

@Composable
fun ImportScreenMapAccountsContent(
    hasDbAccounts: Boolean,
    tempAccounts: List<TempAccountWithFormattedBalance>,
    accountMappings: Map<Long, AccountWithTransactionMetadata>,
    selectedTempAccounts: Set<Long>,
    restoreBalanceAccounts: Set<Long>,
    conflictingTempAccounts: Set<Long>,
    selectAccount: (Long, String) -> Unit,
    toggleAccountSelected: (Long) -> Unit,
    toggleRestoreBalance: (Long) -> Unit,
    setAccountMapping: (Long, AccountWithTransactionMetadata) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        stickyHeader(
            key = "info",
            contentType = "info_header",
        ) {
            if (!hasDbAccounts) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 5.dp, bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.InfoCircle,
                        contentDescription = "info icon",
                    )
                    Text(
                        text = stringResource(id = R.string.no_accounts_available_for_mapping_label)
                    )
                }
            }
        }
        items(
            items = tempAccounts,
            key = { tempAccount -> tempAccount.tempAccountId }
        ) { tempAccount ->
            MapAccountCard(
                tempAccount = tempAccount,
                mappedAccount = accountMappings[
                    tempAccount.tempAccountId
                ] ?: AccountWithTransactionMetadata(
                    accountId = 0L,
                    accountName = "",
                    currency = "INR",
                    balance = 0.0,
                    formattedBalance = "",
                    transactionCount = 0L,
                    lastUsed = null
                ),
                modifier = Modifier.animateItem(),
                canSelect = hasDbAccounts,
                selected = selectedTempAccounts.contains(
                    tempAccount.tempAccountId
                ),
                restoreBalance = restoreBalanceAccounts.contains(
                    tempAccount.tempAccountId
                ),
                conflicting = conflictingTempAccounts.contains(
                    tempAccount.tempAccountId
                ),
                selectSource = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectAccount(tempAccount.tempAccountId, tempAccount.tempAccountCurrency)
                },
                toggleSelected = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    toggleAccountSelected(tempAccount.tempAccountId)
                },
                toggleRestoreBalance = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    toggleRestoreBalance(tempAccount.tempAccountId)
                },
                clearSelectedSource = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setAccountMapping(
                        tempAccount.tempAccountId,
                        AccountWithTransactionMetadata(
                            accountId = 0L,
                            accountName = "",
                            currency = "INR",
                            balance = 0.0,
                            formattedBalance = "",
                            transactionCount = 0L,
                            lastUsed = null
                        )
                    )
                }
            )
        }
    }
}