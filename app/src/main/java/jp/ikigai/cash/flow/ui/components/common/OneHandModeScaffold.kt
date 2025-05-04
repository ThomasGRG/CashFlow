package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneHandModeScaffold(
    loading: Boolean,
    showToastBar: Boolean,
    toastBarText: String,
    onDismissToastBar: () -> Unit,
    showBottomPopup: Boolean = false,
    bottomPopupContent: @Composable (() -> Unit) -> Unit = {},
    onDismissPopup: () -> Unit = {},
    showEmptyPlaceholder: Boolean,
    emptyPlaceholderText: String,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.(Double, () -> Unit) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val configuration = LocalConfiguration.current
    val haptics = LocalHapticFeedback.current

    var screenHeight by remember {
        mutableIntStateOf(configuration.screenHeightDp)
    }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.screenHeightDp }
            .collectLatest { screenHeight = it }
    }

    var isPulledDown by remember {
        mutableStateOf(false)
    }

    val pullDownDistance = remember(key1 = screenHeight) {
        screenHeight * 0.4
    }

    val oneHandModeState = rememberPullToRefreshState()

    var oneHandModeBoxHeight by remember {
        mutableDoubleStateOf(0.0)
    }

    LaunchedEffect(key1 = isPulledDown) {
        if (isPulledDown) {
            keyboardController?.hide()
            oneHandModeBoxHeight = pullDownDistance
            delay(3000)
            oneHandModeBoxHeight = 0.0
            isPulledDown = false
        }
    }

    LaunchedEffect(key1 = showBottomPopup) {
        keyboardController?.hide()
    }

    Box {
        Scaffold(
            modifier = Modifier
                .animateContentSize()
                .navigationBarsPadding()
                .imePadding()
                .fillMaxSize(),
            topBar = topBar,
            bottomBar = bottomBar
        ) { contentPadding ->
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                isRefreshing = isPulledDown,
                onRefresh = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    isPulledDown = true
                },
                state = oneHandModeState,
                indicator = {
                    OneHandModeIndicator(
                        state = oneHandModeState,
                        isRefreshing = isPulledDown,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                content(
                    oneHandModeBoxHeight
                ) {
                    oneHandModeBoxHeight = 0.0
                    isPulledDown = false
                }
                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                } else {
                    if (showEmptyPlaceholder) {
                        Text(
                            text = emptyPlaceholderText, modifier = Modifier.align(
                                Alignment.Center
                            )
                        )
                    }
                }
                AnimatedVisibility(
                    visible = showToastBar,
                    enter = fadeIn() + scaleIn(initialScale = 0.6f),
                    exit = fadeOut() + scaleOut(targetScale = 0.6f),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    ToastBar(
                        message = toastBarText,
                        onDismiss = onDismissToastBar
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = showBottomPopup,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
        ) {
            BottomPopup(
                dismiss = onDismissPopup
            ) {
                bottomPopupContent(it)
            }
        }
    }
}