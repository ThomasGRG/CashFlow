package jp.ikigai.cash.flow.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.ui.components.common.AnimatedTextFieldErrorLabel

@Composable
fun CustomOutlinedButton(
    enabled: Boolean,
    value: String,
    label: String,
    placeHolder: String,
    isError: Boolean = false,
    errorHint: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val alpha by remember(key1 = enabled) {
        derivedStateOf {
            if (enabled) 1f else 0.38f
        }
    }

    val text by remember(key1 = value, key2 = placeHolder) {
        derivedStateOf {
            value.ifBlank { placeHolder }
        }
    }

    val borderColor = if (enabled) {
        if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    } else MaterialTheme.colorScheme.outline.copy(alpha = alpha)

    val textColor = if (value.isBlank()) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onBackground
        }
    }

    Box {
        Column {
            Spacer(modifier = Modifier.height(9.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .heightIn(min = 56.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    leadingIcon?.invoke()
                    Text(
                        text = text,
                        color = textColor,
                        modifier = Modifier
                            .alpha(alpha = alpha)
                            .clickable(
                                enabled = enabled,
                                onClick = onClick,
                                indication = null,
                                interactionSource = interactionSource
                            )
                            .padding(
                                start = 17.dp,
                                end = 17.dp,
                                top = 15.dp,
                                bottom = 15.dp
                            )
                            .weight(1f)
                    )
                    trailingIcon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = "trailing icon",
                            modifier = Modifier
                                .alpha(alpha = alpha)
                                .clickable(
                                    enabled = enabled,
                                    onClick = {
                                        onTrailingIconClick?.invoke()
                                    }
                                )
                        )
                    }
                }
            }
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
                color = borderColor,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .alpha(alpha = alpha)
                    .padding(start = 3.dp, end = 3.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CustomOutlinedButtonPreview() {
    Column {
        Column(
            Modifier
                .heightIn(max = 80.dp)
                .padding(5.dp)
        ) {
            CustomOutlinedButton(
                enabled = true,
                value = "Text",
                label = "Category",
                placeHolder = "Select a category",
                onClick = {}
            )
        }
        Column(
            Modifier
                .heightIn(max = 80.dp)
                .padding(5.dp)
        ) {
            CustomOutlinedButton(
                enabled = true,
                value = "Text",
                label = "Category",
                placeHolder = "Select a category",
                isError = true,
                errorHint = "Cannot be",
                onClick = {}
            )
        }
    }
}