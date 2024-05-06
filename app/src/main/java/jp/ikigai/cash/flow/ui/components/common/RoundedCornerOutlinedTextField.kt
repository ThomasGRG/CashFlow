package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun RoundedCornerOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    label: String,
    placeHolder: String,
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
    val alpha by remember(key1 = enabled) {
        mutableStateOf(if (enabled) 1f else 0.38f)
    }

    var focused by remember {
        mutableStateOf(false)
    }

    val animatedIconColor by animateColorAsState(
        targetValue = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onBackground
        },
        label = "animated icon color"
    )

    val animatedLabelColor by animateColorAsState(
        targetValue = if (enabled) {
            if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                if (focused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            }
        } else {
            MaterialTheme.colorScheme.outline
        },
        label = "animated label color"
    )

    Box {
        Column {
            Spacer(modifier = Modifier.height(9.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        focused = it.isFocused || it.hasFocus
                    },
                placeholder = {
                    Text(text = placeHolder)
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconDescription,
                        tint = animatedIconColor
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "clear field",
                        modifier = Modifier
                            .clickable(
                                enabled = enabled,
                                onClick = {
                                    onValueChange(TextFieldValue(""))
                                }
                            )
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
        Row(
            modifier = Modifier
                .padding(start = 12.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = animatedLabelColor,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .alpha(alpha = alpha)
                    .padding(start = 3.dp, end = 3.dp)
            )
        }
    }
}

@Composable
fun RoundedCornerOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    label: String,
    placeHolder: String,
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
    val alpha by remember(key1 = enabled) {
        mutableStateOf(if (enabled) 1f else 0.38f)
    }

    var focused by remember {
        mutableStateOf(false)
    }

    val animatedIconColor by animateColorAsState(
        targetValue = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onBackground
        },
        label = "animated icon color"
    )

    val animatedLabelColor by animateColorAsState(
        targetValue = if (enabled) {
            if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                if (focused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            }
        } else {
            MaterialTheme.colorScheme.outline
        },
        label = "animated label color"
    )

    Box {
        Column {
            Spacer(modifier = Modifier.height(9.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        focused = it.isFocused || it.hasFocus
                    },
                placeholder = {
                    Text(text = placeHolder)
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconDescription,
                        tint = animatedIconColor
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "clear field",
                        modifier = Modifier
                            .clickable(
                                enabled = enabled,
                                onClick = {
                                    onValueChange("")
                                }
                            )
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
        Row(
            modifier = Modifier
                .padding(start = 12.dp)
                .align(Alignment.TopStart)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = animatedLabelColor,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .alpha(alpha = alpha)
                    .padding(start = 3.dp, end = 3.dp)
            )
        }
    }
}