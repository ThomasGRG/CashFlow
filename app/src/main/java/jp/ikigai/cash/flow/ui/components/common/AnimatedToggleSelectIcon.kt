package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import jp.ikigai.cash.flow.R

@Composable
fun AnimatedToggleSelectIcon(
    deselectVisible: Boolean
) {
    Box(
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedVisibility(
            visible = deselectVisible,
            enter = expandHorizontally(expandFrom = Alignment.Start),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_deselect_24),
                contentDescription = "toggle_select_icon"
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.baseline_select_all_24),
            contentDescription = "toggle_select_icon"
        )
    }
}