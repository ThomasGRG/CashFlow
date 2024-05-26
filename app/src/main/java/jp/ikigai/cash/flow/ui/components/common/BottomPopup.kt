package jp.ikigai.cash.flow.ui.components.common

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs

@Composable
fun BottomPopup(
    dismiss: () -> Unit,
    content: @Composable (() -> Unit) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember {
        MutableInteractionSource()
    }
    var showContent by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        showContent = true
    }

    BackHandler {
        showContent = false
        dismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = true,
                onClick = {
                    showContent = false
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                }
            ),
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = slideInVertically(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                ),
                initialOffsetY = { abs(it) }
            ),
            exit = slideOutVertically(
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    visibilityThreshold = IntOffset.VisibilityThreshold
                ),
                targetOffsetY = { it }
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable(enabled = false, onClick = {})
        ) {
            content {
                showContent = false
            }
        }
    }
}