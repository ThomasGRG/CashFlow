package jp.ikigai.cash.flow.ui.components.bottombars

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RoundedBottomBar(
    content: @Composable () -> Unit
) {
    BottomAppBar(
        modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun RoundedBottomBarPreview() {
    RoundedBottomBar { }
}