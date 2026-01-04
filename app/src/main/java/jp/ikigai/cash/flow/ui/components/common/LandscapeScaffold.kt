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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandscapeScaffold(
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
    firstColContent: @Composable BoxScope.() -> Unit,
    secondColContent: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .animateContentSize()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) { contentPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxHeight()
            ) {
                firstColContent()
            }
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxHeight()
                    .imePadding()
            ) {
                Surface(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    secondColContent()
                }
                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .align(Alignment.TopCenter)
                    )
                } else {
                    if (showEmptyPlaceholder) {
                        Text(
                            text = emptyPlaceholderText,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showToastBar,
                    enter = fadeIn() + scaleIn(initialScale = 0.6f),
                    exit = fadeOut() + scaleOut(targetScale = 0.6f),
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    ToastBar(
                        message = toastBarText,
                        onDismiss = onDismissToastBar
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
fun LandscapeScaffold(
    loading: Boolean,
    loadingIndicator: @Composable BoxScope.() -> Unit = {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .align(Alignment.TopCenter)
        )
    },
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showBottomSheet: Boolean = false,
    bottomSheetContent: @Composable () -> Unit = {},
    onDismissSheet: () -> Unit = {},
    showEmptyPlaceholder: Boolean,
    emptyPlaceholderText: String,
    firstColContent: @Composable BoxScope.() -> Unit,
    secondColContent: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .animateContentSize()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) { contentPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxHeight()
            ) {
                firstColContent()
            }
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxHeight()
                    .imePadding()
            ) {
                Surface(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    secondColContent()
                }
                if (loading) {
                    loadingIndicator()
                } else {
                    if (showEmptyPlaceholder) {
                        Text(
                            text = emptyPlaceholderText,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
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
fun LandscapeScaffold(
    loading: Boolean,
    showToastBar: Boolean,
    toastBarText: String,
    onDismissToastBar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showBottomSheet: Boolean = false,
    bottomSheetContent: @Composable () -> Unit = {},
    onDismissSheet: () -> Unit = {},
    firstColContent: @Composable BoxScope.() -> Unit,
    secondColContent: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .animateContentSize()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) { contentPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxHeight()
            ) {
                firstColContent()
            }
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxHeight()
                    .imePadding()
            ) {
                Surface(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    secondColContent()
                }
                if (loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .align(Alignment.TopCenter)
                    )
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showToastBar,
                    enter = fadeIn() + scaleIn(initialScale = 0.6f),
                    exit = fadeOut() + scaleOut(targetScale = 0.6f),
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    ToastBar(
                        message = toastBarText,
                        onDismiss = onDismissToastBar
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
fun LandscapeScaffold(
    showToastBar: Boolean,
    toastBarText: String,
    onDismissToastBar: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showBottomSheet: Boolean = false,
    bottomSheetContent: @Composable () -> Unit = {},
    onDismissSheet: () -> Unit = {},
    firstColContent: @Composable BoxScope.() -> Unit,
    secondColContent: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .animateContentSize()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .fillMaxSize()
    ) { contentPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxHeight()
            ) {
                firstColContent()
            }
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxHeight()
                    .imePadding()
            ) {
                Surface(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    secondColContent()
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showToastBar,
                    enter = fadeIn() + scaleIn(initialScale = 0.6f),
                    exit = fadeOut() + scaleOut(targetScale = 0.6f),
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    ToastBar(
                        message = toastBarText,
                        onDismiss = onDismissToastBar
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