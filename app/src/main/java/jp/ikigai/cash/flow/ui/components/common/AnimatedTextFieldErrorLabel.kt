package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedTextFieldErrorLabel(
    visible: Boolean,
    errorLabel: String,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.fillMaxWidth().padding(start = 15.dp),
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Text(
            text = errorLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}