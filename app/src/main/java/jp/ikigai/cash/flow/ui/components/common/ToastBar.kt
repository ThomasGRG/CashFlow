package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ToastBar(
    message: String,
    onDismiss: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
            IconButton(
                onClick = { onDismiss() },
                content = {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "dismiss",
                    )
                },
            )
        }
    }
}

@Preview
@Composable
fun ToastBarPreview() {
    ToastBar(
        message = "Test",
        onDismiss = {}
    )
}