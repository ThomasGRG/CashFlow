/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui.components.common

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
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
import androidx.compose.runtime.remember
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

    val transitionState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    BackHandler {
        transitionState.targetState = false
    }

    if (transitionState.isIdle && !transitionState.currentState) {
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
                    transitionState.targetState = false
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ),
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
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
                .clickable(
                    enabled = false,
                    onClick = {}
                )
        ) {
            content {
                transitionState.targetState = false
            }
        }
    }
}