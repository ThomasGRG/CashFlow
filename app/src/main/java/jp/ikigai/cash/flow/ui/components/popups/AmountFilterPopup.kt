package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CashBanknote
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField

@Composable
fun AmountFilterPopup(
    minAmount: Double,
    maxAmount: Double,
    filter: (Double, Double) -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var minimumAmount by remember {
        mutableStateOf(minAmount.toString())
    }

    var maximumAmount by remember {
        mutableStateOf(maxAmount.toString())
    }

    val minimumAmountValid by remember {
        derivedStateOf {
            val minimumAmountValue = minimumAmount.toDoubleOrNull() ?: 0.0
            val maximumAmountValue = maximumAmount.toDoubleOrNull() ?: 0.0
            !(minimumAmountValue > 0 && maximumAmountValue > 0 && minimumAmountValue > maximumAmountValue)
        }
    }

    val maximumAmountValid by remember {
        derivedStateOf {
            val minimumAmountValue = minimumAmount.toDoubleOrNull() ?: 0.0
            val maximumAmountValue = maximumAmount.toDoubleOrNull() ?: 0.0
            !(minimumAmountValue > 0 && maximumAmountValue > 0 && maximumAmountValue < minimumAmountValue)
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
        RoundedCornerOutlinedTextField(
            value = minimumAmount,
            onValueChange = {
                minimumAmount = it
            },
            enabled = true,
            label = stringResource(id = R.string.minimum_amount_field_label),
            placeHolder = stringResource(id = R.string.minimum_amount_placeholder_label),
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            icon = TablerIcons.CashBanknote,
            iconDescription = "amount icon",
            isError = !minimumAmountValid,
            errorHint = stringResource(id = R.string.invalid_min_amount_error_label, maximumAmount),
            onDone = {
                keyboardController?.hide()
            }
        )
        RoundedCornerOutlinedTextField(
            value = maximumAmount,
            onValueChange = {
                maximumAmount = it
            },
            enabled = true,
            label = stringResource(id = R.string.maximum_amount_field_label),
            placeHolder = stringResource(id = R.string.maximum_amount_placeholder_label),
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            icon = TablerIcons.CashBanknote,
            iconDescription = "amount icon",
            isError = !maximumAmountValid,
            errorHint = stringResource(id = R.string.invalid_max_amount_error_label, minimumAmount),
            onDone = {
                keyboardController?.hide()
            }
        )
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
                    filter(
                        minimumAmount.toDoubleOrNull() ?: 0.0,
                        maximumAmount.toDoubleOrNull() ?: 0.0
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = minimumAmountValid && maximumAmountValid
            ) {
                Text(text = stringResource(id = R.string.filter_button_label))
            }
        }
    }
}
