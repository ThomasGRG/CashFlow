package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedCornerOutlinedTextField(
    value: String,
    enabled: Boolean,
    expanded: Boolean,
    label: String,
    icon: ImageVector,
    iconDescription: String,
    onClick: () -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = false,
        onExpandedChange = {
            if (enabled) {
                onClick()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            label = {
                Text(
                    text = label
                )
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
    }
}

@Composable
fun RoundedCornerOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    label: String,
    icon: ImageVector,
    iconDescription: String,
    isError: Boolean = false,
    errorHint: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Done
    ),
    onDone: KeyboardActionScope.() -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth(),
        label = {
            Text(text = label)
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
        },
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = onDone
        ),
        shape = RoundedCornerShape(14.dp),
    )
    AnimatedTextFieldErrorLabel(
        visible = isError,
        errorLabel = errorHint
    )
}

@Composable
fun RoundedCornerOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    label: String,
    icon: ImageVector,
    iconDescription: String,
    isError: Boolean = false,
    errorHint: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Done
    ),
    onDone: KeyboardActionScope.() -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth(),
        label = {
            Text(text = label)
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
        },
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = onDone
        ),
        shape = RoundedCornerShape(14.dp),
    )
    AnimatedTextFieldErrorLabel(
        visible = isError,
        errorLabel = errorHint
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedCornerOutlinedTextField(
    value: TextFieldValue,
    enabled: Boolean,
    expanded: Boolean,
    label: String,
    icon: ImageVector,
    iconDescription: String,
    onClick: () -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = false,
        onExpandedChange = {
            if (enabled) {
                onClick()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = {
                Text(text = label)
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(14.dp),
        )
    }
}