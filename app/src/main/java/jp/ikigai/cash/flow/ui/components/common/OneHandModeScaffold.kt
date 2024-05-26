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
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
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

    var screenHeight by remember {
        mutableIntStateOf(configuration.screenHeightDp)
    }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.screenHeightDp }
            .collectLatest { screenHeight = it }
    }

    val pullDownDistance = remember(key1 = screenHeight) {
        screenHeight * 0.4
    }

    val oneHandModeState = rememberPullToRefreshState()

    var oneHandModeBoxHeight by remember {
        mutableStateOf(0.0)
    }

    LaunchedEffect(key1 = oneHandModeState.isRefreshing) {
        if (oneHandModeState.isRefreshing) {
            keyboardController?.hide()
            oneHandModeBoxHeight = pullDownDistance
            delay(3000)
            oneHandModeBoxHeight = 0.0
            oneHandModeState.endRefresh()
        }
    }

    LaunchedEffect(key1 = showBottomPopup) {
        keyboardController?.hide()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .animateContentSize()
                .navigationBarsPadding()
                .imePadding()
                .fillMaxSize()
                .nestedScroll(oneHandModeState.nestedScrollConnection),
            topBar = topBar,
            bottomBar = bottomBar
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                content(
                    oneHandModeBoxHeight
                ) {
                    oneHandModeBoxHeight = 0.0
                    oneHandModeState.endRefresh()
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
                    } else {
                        PullToRefreshContainer(
                            state = oneHandModeState,
                            modifier = Modifier.align(Alignment.TopCenter),
                            indicator = { state ->
                                OneHandModeIndicator(state = state)
                            }
                        )
                    }
                }
                AnimatedVisibility(
                    visible = showToastBar,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
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
            modifier = Modifier.imePadding()
        ) {
            BottomPopup(
                dismiss = onDismissPopup
            ) {
                bottomPopupContent(it)
            }
        }
    }
}