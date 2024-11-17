package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Calculator
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.Scale
import compose.icons.tablericons.Stack
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.dto.UpsertTemplateItemCardInfo
import jp.ikigai.cash.flow.data.dto.UpsertTransactionItemCardInfo
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField

@Composable
fun UpsertTransactionItemCard(
    modifier: Modifier,
    index: Int,
    enabled: Boolean,
    data: UpsertTransactionItemCardInfo,
    onItemClick: () -> Unit,
    onUnitClick: () -> Unit,
    updatePrice: (String, Int) -> Unit,
    updateQuantity: (String, Int) -> Unit,
    remove: (Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var expanded by remember {
        mutableStateOf(true)
    }

    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        label = "expanded state icon rotation animation"
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                }
                .padding(start = 13.dp, end = 13.dp, top = 17.dp, bottom = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(
                    id = R.string.item_number_label,
                    index + 1,
                    data.totalDisplayPrice
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "expanded state icon",
                modifier = Modifier.rotate(rotationState)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    HorizontalDivider()
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 13.dp, end = 13.dp, top = 13.dp, bottom = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomOutlinedButton(
                        enabled = enabled,
                        value = data.item.name,
                        label = stringResource(id = R.string.item_field_label),
                        placeHolder = "",
                        leadingIcon = TablerIcons.Stack,
                        onClick = onItemClick
                    )
                    RoundedCornerOutlinedTextField(
                        value = data.displayPrice,
                        onValueChange = {
                            updatePrice(it, index)
                        },
                        enabled = enabled,
                        label = stringResource(id = R.string.price_field_label),
                        placeHolder = stringResource(id = R.string.price_placeholder_label),
                        icon = TablerIcons.CashBanknote,
                        iconDescription = "amount icon",
                        isError = !data.priceValid,
                        errorHint = stringResource(id = R.string.invalid_price_error_label),
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                    CustomOutlinedButton(
                        enabled = enabled,
                        value = stringResource(id = data.unit.label),
                        label = stringResource(id = R.string.unit_field_label),
                        placeHolder = "",
                        leadingIcon = TablerIcons.Scale,
                        onClick = onUnitClick
                    )
                    RoundedCornerOutlinedTextField(
                        value = data.displayQuantity,
                        onValueChange = {
                            updateQuantity(it, index)
                        },
                        enabled = enabled,
                        label = stringResource(id = R.string.quantity_field_label),
                        placeHolder = stringResource(id = R.string.quantity_placeholder_label),
                        icon = TablerIcons.Calculator,
                        iconDescription = "quantity icon",
                        isError = !data.quantityValid,
                        errorHint = stringResource(id = R.string.invalid_amount_error_label),
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 13.dp, end = 13.dp, top = 8.dp, bottom = 13.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            remove(index)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(text = stringResource(id = R.string.remove_button_label))
                    }
                }
            }
        }
    }
}

@Composable
fun UpsertTemplateItemCard(
    modifier: Modifier,
    index: Int,
    enabled: Boolean,
    data: UpsertTemplateItemCardInfo,
    onItemClick: () -> Unit,
    onUnitClick: () -> Unit,
    updatePrice: (String, Int) -> Unit,
    updateQuantity: (String, Int) -> Unit,
    remove: (Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var expanded by remember {
        mutableStateOf(true)
    }

    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        label = "expanded state icon rotation animation"
    )

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                }
                .padding(start = 13.dp, end = 13.dp, top = 17.dp, bottom = 17.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(
                    id = R.string.item_number_label,
                    index + 1,
                    data.totalDisplayPrice
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "expanded state icon",
                modifier = Modifier.rotate(rotationState)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp)
                ) {
                    HorizontalDivider()
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 13.dp, end = 13.dp, top = 13.dp, bottom = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomOutlinedButton(
                        enabled = enabled,
                        value = data.item.name,
                        label = stringResource(id = R.string.item_field_label),
                        placeHolder = "",
                        leadingIcon = TablerIcons.Stack,
                        onClick = onItemClick
                    )
                    RoundedCornerOutlinedTextField(
                        value = data.displayPrice,
                        onValueChange = {
                            updatePrice(it, index)
                        },
                        enabled = enabled,
                        label = stringResource(id = R.string.price_field_label),
                        placeHolder = stringResource(id = R.string.price_placeholder_label),
                        icon = TablerIcons.CashBanknote,
                        iconDescription = "amount icon",
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                    CustomOutlinedButton(
                        enabled = enabled,
                        value = stringResource(id = data.unit.label),
                        label = stringResource(id = R.string.unit_field_label),
                        placeHolder = "",
                        leadingIcon = TablerIcons.Scale,
                        onClick = onUnitClick
                    )
                    RoundedCornerOutlinedTextField(
                        value = data.displayQuantity,
                        onValueChange = {
                            updateQuantity(it, index)
                        },
                        enabled = enabled,
                        label = stringResource(id = R.string.quantity_field_label),
                        placeHolder = stringResource(id = R.string.quantity_placeholder_label),
                        icon = TablerIcons.Calculator,
                        iconDescription = "quantity icon",
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 13.dp, end = 13.dp, top = 8.dp, bottom = 13.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            remove(index)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(text = stringResource(id = R.string.remove_button_label))
                    }
                }
            }
        }
    }
}