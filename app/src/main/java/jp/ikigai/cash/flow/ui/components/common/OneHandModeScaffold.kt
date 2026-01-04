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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneHandModeScaffold(
    loading: Boolean,
    showToastBar: Boolean,
    toastBarText: String,
    onDismissToastBar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showBottomSheet: Boolean = false,
    bottomSheetContent: @Composable () -> Unit = {},
    onDismissSheet: () -> Unit = {},
    showEmptyPlaceholder: Boolean,
    emptyPlaceholderText: String,
    topBar: @Composable (TopAppBarScrollBehavior, Dp) -> Unit = { _, _ -> },
    bottomBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = topAppBarState
    )

    LaunchedEffect(key1 = showBottomSheet) {
        keyboardController?.hide()
    }

    val imeInsets = WindowInsets.ime

    val imeVisible by remember(
        key1 = imeInsets,
        key2 = density,
    ) {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }

    LaunchedEffect(key1 = imeVisible) {
        if (imeVisible) {
            animate(
                initialValue = topAppBarState.heightOffset,
                targetValue = topAppBarState.heightOffsetLimit,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            ) { value, _ ->
                topAppBarState.heightOffset = value
            }
        }
    }

    val expandedHeight by remember(key1 = windowInfo, key2 = density) {
        mutableStateOf(
            with(density) {
                (windowInfo.containerSize.height / 3).toDp()
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .animateContentSize()
            .navigationBarsPadding()
            .imePadding()
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            topBar(topAppBarScrollBehavior, expandedHeight)
        },
        bottomBar = bottomBar
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            content()
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
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .align(Alignment.BottomCenter)
            ) {
                ToastBar(
                    message = toastBarText,
                    onDismiss = onDismissToastBar
                )
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = onDismissSheet,
                sheetState = sheetState,
                sheetGesturesEnabled = false,
            ) {
                bottomSheetContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneHandModeScaffold(
    loading: Boolean,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showBottomSheet: Boolean = false,
    bottomSheetContent: @Composable () -> Unit = {},
    onDismissSheet: () -> Unit = {},
    showEmptyPlaceholder: Boolean,
    emptyPlaceholderText: String,
    topBar: @Composable (TopAppBarScrollBehavior, Dp) -> Unit = { _, _ -> },
    bottomBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = topAppBarState
    )

    LaunchedEffect(key1 = showBottomSheet) {
        keyboardController?.hide()
    }

    val imeInsets = WindowInsets.ime

    val imeVisible by remember(
        key1 = imeInsets,
        key2 = density,
    ) {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }

    LaunchedEffect(key1 = imeVisible) {
        if (imeVisible) {
            animate(
                initialValue = topAppBarState.heightOffset,
                targetValue = topAppBarState.heightOffsetLimit,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            ) { value, _ ->
                topAppBarState.heightOffset = value
            }
        }
    }

    val expandedHeight by remember(key1 = windowInfo, key2 = density) {
        mutableStateOf(
            with(density) {
                (windowInfo.containerSize.height / 3).toDp()
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .animateContentSize()
            .navigationBarsPadding()
            .imePadding()
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            topBar(topAppBarScrollBehavior, expandedHeight)
        },
        bottomBar = bottomBar
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            content()
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
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = onDismissSheet,
                sheetState = sheetState,
                sheetGesturesEnabled = false,
            ) {
                bottomSheetContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneHandModeScaffold(
    showToastBar: Boolean,
    toastBarText: String,
    onDismissToastBar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showBottomSheet: Boolean = false,
    bottomSheetContent: @Composable () -> Unit = {},
    onDismissSheet: () -> Unit = {},
    topBar: @Composable (TopAppBarScrollBehavior, Dp) -> Unit = { _, _ -> },
    bottomBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = topAppBarState
    )

    LaunchedEffect(key1 = showBottomSheet) {
        keyboardController?.hide()
    }

    val imeInsets = WindowInsets.ime

    val imeVisible by remember(
        key1 = imeInsets,
        key2 = density,
    ) {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }

    LaunchedEffect(key1 = imeVisible) {
        if (imeVisible) {
            animate(
                initialValue = topAppBarState.heightOffset,
                targetValue = topAppBarState.heightOffsetLimit,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            ) { value, _ ->
                topAppBarState.heightOffset = value
            }
        }
    }

    val expandedHeight by remember(key1 = windowInfo, key2 = density) {
        mutableStateOf(
            with(density) {
                (windowInfo.containerSize.height / 3).toDp()
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .animateContentSize()
            .navigationBarsPadding()
            .imePadding()
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            topBar(topAppBarScrollBehavior, expandedHeight)
        },
        bottomBar = bottomBar
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            content()
            AnimatedVisibility(
                visible = showToastBar,
                enter = fadeIn() + scaleIn(initialScale = 0.6f),
                exit = fadeOut() + scaleOut(targetScale = 0.6f),
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .align(Alignment.BottomCenter)
            ) {
                ToastBar(
                    message = toastBarText,
                    onDismiss = onDismissToastBar
                )
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = onDismissSheet,
                sheetState = sheetState,
                sheetGesturesEnabled = false,
            ) {
                bottomSheetContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneHandModeScaffold(
    loading: Boolean,
    showToastBar: Boolean,
    toastBarText: String,
    onDismissToastBar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showBottomSheet: Boolean = false,
    bottomSheetContent: @Composable () -> Unit = {},
    onDismissSheet: () -> Unit = {},
    topBar: @Composable (TopAppBarScrollBehavior, Dp) -> Unit = { _, _ -> },
    bottomBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val topAppBarState = rememberTopAppBarState()
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = topAppBarState
    )

    LaunchedEffect(key1 = showBottomSheet) {
        keyboardController?.hide()
    }

    val imeInsets = WindowInsets.ime

    val imeVisible by remember(
        key1 = imeInsets,
        key2 = density,
    ) {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }

    LaunchedEffect(key1 = imeVisible) {
        if (imeVisible) {
            animate(
                initialValue = topAppBarState.heightOffset,
                targetValue = topAppBarState.heightOffsetLimit,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                )
            ) { value, _ ->
                topAppBarState.heightOffset = value
            }
        }
    }

    val expandedHeight by remember(key1 = windowInfo, key2 = density) {
        mutableStateOf(
            with(density) {
                (windowInfo.containerSize.height / 3).toDp()
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .animateContentSize()
            .navigationBarsPadding()
            .imePadding()
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            topBar(topAppBarScrollBehavior, expandedHeight)
        },
        bottomBar = bottomBar
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            content()
            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
            AnimatedVisibility(
                visible = showToastBar,
                enter = fadeIn() + scaleIn(initialScale = 0.6f),
                exit = fadeOut() + scaleOut(targetScale = 0.6f),
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .align(Alignment.BottomCenter)
            ) {
                ToastBar(
                    message = toastBarText,
                    onDismiss = onDismissToastBar
                )
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = onDismissSheet,
                sheetState = sheetState,
                sheetGesturesEnabled = false,
            ) {
                bottomSheetContent()
            }
        }
    }
}