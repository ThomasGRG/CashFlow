package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TempAccountWithFormattedBalance
import jp.ikigai.cash.flow.TempCategory
import jp.ikigai.cash.flow.TempCounterParty
import jp.ikigai.cash.flow.TempMethod
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton

@Composable
fun MapCategoryCard(
    modifier: Modifier = Modifier,
    tempCategory: TempCategory,
    mappedCategory: CategoryWithTransactionMetadata,
    canSelect: Boolean,
    selected: Boolean,
    conflicting: Boolean,
    toggleSelected: () -> Unit,
    selectCategory: () -> Unit,
    clearSelectedCategory: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.38f,
        label = "animated alpha"
    )

    OutlinedCard(
        onClick = toggleSelected,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = tempCategory.tempCategoryName,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .alpha(alpha)
                        .weight(1f, fill = false)
                        .padding(end = 10.dp)
                )
                Checkbox(
                    checked = selected,
                    onCheckedChange = null,
                    modifier = Modifier.alpha(alpha)
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.99f)
                    .padding(bottom = 10.dp)
                    .alpha(alpha)
            )
            CustomOutlinedButton(
                value = mappedCategory.categoryName,
                leadingIcon = mappedCategory.icon,
                trailingIcon = Icons.Filled.Clear,
                onTrailingIconClick = clearSelectedCategory,
                enabled = canSelect && selected,
                label = stringResource(id = R.string.map_to_field_label),
                onClick = selectCategory,
                placeHolder = stringResource(id = R.string.select_category_placeholder_label),
                modifier = Modifier.padding(bottom = 5.dp),
                isError = selected && conflicting && mappedCategory.categoryId == 0L,
                errorHint = stringResource(id = R.string.category_exists_error_label)
            )
            AnimatedVisibility(
                visible = canSelect && selected && !conflicting && mappedCategory.categoryId == 0L
            ) {
                Text(
                    text = stringResource(id = R.string.new_category_label),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, top = 5.dp)
                        .alpha(0.6f)
                )
            }
        }
    }
}

@Composable
fun MapCounterPartyCard(
    modifier: Modifier = Modifier,
    tempCounterParty: TempCounterParty,
    mappedCounterParty: CounterPartyWithTransactionMetadata,
    canSelect: Boolean,
    selected: Boolean,
    conflicting: Boolean,
    toggleSelected: () -> Unit,
    selectCounterParty: () -> Unit,
    clearSelectedCounterParty: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.38f,
        label = "animated alpha"
    )

    OutlinedCard(
        onClick = toggleSelected,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = tempCounterParty.tempCounterPartyName,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .alpha(alpha)
                        .weight(1f, fill = false)
                        .padding(end = 10.dp)
                )
                Checkbox(
                    checked = selected,
                    onCheckedChange = null,
                    modifier = Modifier.alpha(alpha)
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.99f)
                    .padding(bottom = 10.dp)
                    .alpha(alpha)
            )
            CustomOutlinedButton(
                value = mappedCounterParty.counterPartyName,
                leadingIcon = Constants.DEFAULT_COUNTERPARTY_ICON,
                trailingIcon = Icons.Filled.Clear,
                onTrailingIconClick = clearSelectedCounterParty,
                enabled = canSelect && selected,
                label = stringResource(id = R.string.map_to_field_label),
                onClick = selectCounterParty,
                placeHolder = stringResource(id = R.string.counter_party_placeholder_label),
                modifier = Modifier.padding(bottom = 5.dp),
                isError = selected && conflicting && mappedCounterParty.counterPartyId == 0L,
                errorHint = stringResource(id = R.string.counter_party_exists_error_label)
            )
            AnimatedVisibility(
                visible = canSelect && selected && !conflicting && mappedCounterParty.counterPartyId == 0L
            ) {
                Text(
                    text = stringResource(id = R.string.new_counterParty_label),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, top = 5.dp)
                        .alpha(0.6f)
                )
            }
        }
    }
}

@Composable
fun MapMethodCard(
    modifier: Modifier = Modifier,
    tempMethod: TempMethod,
    mappedMethod: MethodWithTransactionMetadata,
    canSelect: Boolean,
    selected: Boolean,
    conflicting: Boolean,
    toggleSelected: () -> Unit,
    selectMethod: () -> Unit,
    clearSelectedMethod: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.38f,
        label = "animated alpha"
    )

    OutlinedCard(
        onClick = toggleSelected,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = tempMethod.tempMethodName,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .alpha(alpha)
                        .weight(1f, fill = false)
                        .padding(end = 10.dp)
                )
                Checkbox(
                    checked = selected,
                    onCheckedChange = null,
                    modifier = Modifier.alpha(alpha)
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.99f)
                    .padding(bottom = 10.dp)
                    .alpha(alpha)
            )
            CustomOutlinedButton(
                value = mappedMethod.methodName,
                leadingIcon = Constants.DEFAULT_METHOD_ICON,
                trailingIcon = Icons.Filled.Clear,
                onTrailingIconClick = clearSelectedMethod,
                enabled = canSelect && selected,
                label = stringResource(id = R.string.map_to_field_label),
                onClick = selectMethod,
                placeHolder = stringResource(id = R.string.select_method_placeholder_label),
                modifier = Modifier.padding(bottom = 5.dp),
                isError = selected && conflicting && mappedMethod.methodId == 0L,
                errorHint = stringResource(id = R.string.method_exists_error_label)
            )
            AnimatedVisibility(
                visible = canSelect && selected && !conflicting && mappedMethod.methodId == 0L
            ) {
                Text(
                    text = stringResource(id = R.string.new_method_label),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, top = 5.dp)
                        .alpha(0.6f)
                )
            }
        }
    }
}

@Composable
fun MapAccountCard(
    modifier: Modifier = Modifier,
    tempAccount: TempAccountWithFormattedBalance,
    mappedAccount: AccountWithTransactionMetadata,
    canSelect: Boolean,
    selected: Boolean,
    conflicting: Boolean,
    restoreBalance: Boolean,
    toggleRestoreBalance: () -> Unit,
    toggleSelected: () -> Unit,
    selectSource: () -> Unit,
    clearSelectedSource: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.38f,
        label = "animated alpha"
    )

    val switchAlpha by animateFloatAsState(
        targetValue = if (selected && mappedAccount.accountId > 0) 1f else 0.38f,
        label = "animated alpha"
    )

    OutlinedCard(
        onClick = toggleSelected,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = tempAccount.tempAccountName,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .alpha(alpha)
                        .weight(1f, fill = false)
                        .padding(end = 10.dp)
                )
                Checkbox(
                    checked = selected,
                    onCheckedChange = null,
                    modifier = Modifier.alpha(alpha)
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.99f)
                    .padding(bottom = 10.dp)
                    .alpha(alpha)
            )
            AnimatedVisibility(
                visible = mappedAccount.accountId > 0 && tempAccount.tempAccountBalance != mappedAccount.balance
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClick = toggleRestoreBalance,
                            enabled = selected && mappedAccount.accountId > 0
                        )
                        .padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.update_account_balance_to_label,
                            tempAccount.formattedBalance
                        ),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = switchAlpha),
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .weight(1f, fill = false)
                    )
                    Switch(
                        checked = restoreBalance,
                        onCheckedChange = null,
                        enabled = selected && mappedAccount.accountId > 0,
                        thumbContent = {
                            Icon(
                                imageVector = if (restoreBalance) Icons.Filled.Check else Icons.Filled.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    )
                }
            }
            CustomOutlinedButton(
                value = if (mappedAccount.accountId > 0) "${mappedAccount.accountName} - ${mappedAccount.formattedBalance}" else "",
                leadingIcon = Constants.DEFAULT_ACCOUNT_ICON,
                trailingIcon = Icons.Filled.Clear,
                onTrailingIconClick = clearSelectedSource,
                enabled = canSelect && selected,
                label = stringResource(id = R.string.map_to_field_label),
                onClick = selectSource,
                placeHolder = stringResource(id = R.string.select_account_placeholder_label),
                modifier = Modifier.padding(bottom = 5.dp),
                isError = selected && conflicting && mappedAccount.accountId == 0L,
                errorHint = stringResource(id = R.string.account_exists_error_label)
            )
            AnimatedVisibility(
                visible = canSelect && selected && !conflicting && mappedAccount.accountId == 0L
            ) {
                Text(
                    text = stringResource(id = R.string.new_account_label),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, top = 5.dp)
                        .alpha(0.6f)
                )
            }
        }
    }
}