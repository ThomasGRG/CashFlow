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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CashBanknote
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountFilterSheet(
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
            .padding(start = 24.dp, end = 24.dp, bottom = 14.dp),
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
            backgroundColor = BottomSheetDefaults.ContainerColor,
            icon = TablerIcons.CashBanknote,
            iconDescription = "amount icon",
            isError = !minimumAmountValid,
            errorHint = stringResource(id = R.string.invalid_min_amount_error_label, maximumAmount),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
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
            backgroundColor = BottomSheetDefaults.ContainerColor,
            icon = TablerIcons.CashBanknote,
            iconDescription = "amount icon",
            isError = !maximumAmountValid,
            errorHint = stringResource(id = R.string.invalid_max_amount_error_label, minimumAmount),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountFilterLandscapeSheet(
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

    Row(
        modifier = Modifier.fillMaxWidth(),
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
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = minimumAmountValid && maximumAmountValid
            ) {
                Text(text = stringResource(id = R.string.filter_button_label))
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
        Column(
            modifier = Modifier
                .weight(2f)
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp, top = 8.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoundedCornerOutlinedTextField(
                value = minimumAmount,
                onValueChange = {
                    minimumAmount = it
                },
                enabled = true,
                label = stringResource(id = R.string.minimum_amount_field_label),
                placeHolder = stringResource(id = R.string.minimum_amount_placeholder_label),
                backgroundColor = BottomSheetDefaults.ContainerColor,
                icon = TablerIcons.CashBanknote,
                iconDescription = "amount icon",
                isError = !minimumAmountValid,
                errorHint = stringResource(
                    id = R.string.invalid_min_amount_error_label,
                    maximumAmount
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
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
                backgroundColor = BottomSheetDefaults.ContainerColor,
                icon = TablerIcons.CashBanknote,
                iconDescription = "amount icon",
                isError = !maximumAmountValid,
                errorHint = stringResource(
                    id = R.string.invalid_max_amount_error_label,
                    minimumAmount
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                onDone = {
                    keyboardController?.hide()
                }
            )
        }
    }
}
