package jp.ikigai.cash.flow.ui.screens.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.screenStates.common.ChooseIconScreenState
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.dialogs.IconDetailsDialog
import jp.ikigai.cash.flow.ui.viewmodels.common.ChooseIconScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChooseIconScreen(
    defaultIcon: String,
    navigateBackWithResult: (String) -> Unit,
    setSearchText: (String) -> Unit,
    state: ChooseIconScreenState
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val icons by remember(key1 = state.icons) {
        mutableStateOf(state.icons)
    }

    val showEmptyPlaceholder by remember(key1 = state.icons) {
        mutableStateOf(state.icons.isEmpty())
    }

    val iconCount by remember(key1 = state.iconCount) {
        mutableStateOf(state.iconCount)
    }

    val searchText by remember(key1 = state.searchText) {
        mutableStateOf(state.searchText)
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    var iconToPreview by remember {
        mutableStateOf("")
    }

    if (showDialog) {
        IconDetailsDialog(
            dismiss = { showDialog = false },
            iconName = iconToPreview
        )
    }

    BackHandler(
        enabled = !showDialog
    ) {
        navigateBackWithResult(defaultIcon)
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = false,
        toastBarText = "",
        onDismissToastBar = {},
        showEmptyPlaceholder = showEmptyPlaceholder,
        emptyPlaceholderText = "No results found for \"${searchText}\"",
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(text = "Choose icon")
                        Text(
                            text = "$iconCount icons",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            ThreeSlotRoundedBottomBar(
                navigateBack = {
                    navigateBackWithResult(defaultIcon)
                },
            )
        }
    ) { oneHandModeBoxHeight, resetOneHandMode ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(58.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp)
        ) {
            item(
                key = "one-hand-mode-expand-row",
                contentType = "row",
                span = StaggeredGridItemSpan.FullLine
            ) {
                OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
            }
            itemsIndexed(
                items = icons,
                key = { _, icon -> "icon-${icon.name}" }
            ) { _, icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = icon.name,
                    modifier = Modifier
                        .size(52.dp)
                        .padding(6.dp)
                        .combinedClickable(
                            enabled = !loading,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                navigateBackWithResult(icon.name)
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                resetOneHandMode()
                                iconToPreview = icon.name
                                showDialog = true
                            },
                            onLongClickLabel = icon.name,
                            role = Role.Button,
                        )
                )
            }
            item(
                key = "bottomPadding",
                span = StaggeredGridItemSpan.FullLine
            ) {
                Spacer(modifier = Modifier.height(75.dp))
            }
        }
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = setSearchText,
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth(),
                label = {
                    Text(text = "Search")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                shape = RoundedCornerShape(14.dp),
            )
        }
    }
}

@Preview
@Composable
fun ChooseIconScreenPreview() {
    ChooseIconScreen(
        defaultIcon = "",
        navigateBackWithResult = {},
        setSearchText = {},
        state = ChooseIconScreenState()
    )
}

fun NavGraphBuilder.chooseIconScreen(navController: NavController) {
    animatedComposable(
        route = Routes.ChooseIcon.route,
        arguments = listOf(
            navArgument("defaultIcon") {
                type = NavType.StringType
            }
        ),
    ) {
        val viewModel: ChooseIconScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        ChooseIconScreen(
            defaultIcon = viewModel.defaultIcon,
            navigateBackWithResult = { value ->
                navController.previousBackStackEntry?.savedStateHandle?.set("icon", value)
                navController.popBackStack()
            },
            setSearchText = viewModel::setSearchText,
            state = state
        )
    }
}