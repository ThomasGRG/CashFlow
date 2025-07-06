package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.common.MultiSelectCard
import jp.ikigai.cash.flow.ui.components.common.SelectableCard

@Composable
fun SelectTransactionTypePopup(
    selectedTransactionType: TransactionType,
    setSelectedTransactionType: (TransactionType) -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .heightIn(max = 200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(
                key = "credit",
                contentType = "row"
            ) {
                SelectableCard(
                    checked = { selectedTransactionType == TransactionType.CREDIT },
                    label = AnnotatedString(stringResource(id = TransactionType.CREDIT.label)),
                    icon = TransactionType.CREDIT.icon,
                    iconTint = TransactionType.CREDIT.color,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedTransactionType(TransactionType.CREDIT)
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "debit",
                contentType = "row"
            ) {
                SelectableCard(
                    checked = { selectedTransactionType == TransactionType.DEBIT },
                    label = AnnotatedString(stringResource(id = TransactionType.DEBIT.label)),
                    icon = TransactionType.DEBIT.icon,
                    iconTint = TransactionType.DEBIT.color,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedTransactionType(TransactionType.DEBIT)
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
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
    }
}

@Composable
fun FilterTransactionTypePopup(
    selectedTransactionTypes: List<TransactionType>,
    filter: (List<TransactionType>) -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    val selectedTypes = remember {
        mutableStateListOf<TransactionType>()
    }

    LaunchedEffect(Unit) {
        selectedTypes.addAll(selectedTransactionTypes)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .heightIn(max = 200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(
                key = "credit",
                contentType = "row"
            ) {
                MultiSelectCard(
                    checked = {
                        selectedTypes.contains(TransactionType.CREDIT)
                    },
                    label = AnnotatedString(stringResource(id = TransactionType.CREDIT.label)),
                    icon = TransactionType.CREDIT.icon,
                    iconTint = TransactionType.CREDIT.color,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (selectedTypes.contains(TransactionType.CREDIT)) {
                            selectedTypes.remove(TransactionType.CREDIT)
                        } else {
                            selectedTypes.add(TransactionType.CREDIT)
                        }
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "debit",
                contentType = "row"
            ) {
                MultiSelectCard(
                    checked = {
                        selectedTypes.contains(TransactionType.DEBIT)
                    },
                    label = AnnotatedString(stringResource(id = TransactionType.DEBIT.label)),
                    icon = TransactionType.DEBIT.icon,
                    iconTint = TransactionType.DEBIT.color,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (selectedTypes.contains(TransactionType.DEBIT)) {
                            selectedTypes.remove(TransactionType.DEBIT)
                        } else {
                            selectedTypes.add(TransactionType.DEBIT)
                        }
                    },
                    modifier = Modifier.animateItem()
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
                    dismiss()
                    filter(selectedTypes)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedTypes.isNotEmpty()
            ) {
                Text(
                    text = stringResource(
                        id = R.string.filter_button_with_count_label,
                        selectedTypes.size
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun SelectTransactionTypePopupPreview() {
    SelectTransactionTypePopup(
        selectedTransactionType = TransactionType.CREDIT,
        setSelectedTransactionType = {},
        dismiss = {}
    )
}