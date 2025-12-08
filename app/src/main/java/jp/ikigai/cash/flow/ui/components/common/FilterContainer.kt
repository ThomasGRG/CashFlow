package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun FilterContainer(
    padding: PaddingValues,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(paddingValues = padding)
            .height(ButtonDefaults.MinHeight)
            .horizontalScroll(
                rememberScrollState()
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
        )
        content()
        Spacer(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
        )
    }
}

@Composable
fun FilterContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .height(ButtonDefaults.MinHeight)
            .clip(
                RoundedCornerShape(8.dp)
            )
            .horizontalScroll(
                rememberScrollState()
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}