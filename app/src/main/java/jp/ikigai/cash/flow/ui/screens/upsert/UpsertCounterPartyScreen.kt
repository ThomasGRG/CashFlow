package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.screenStates.upsert.UpsertCounterPartyScreenState
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.dialogs.ResetIconDialog
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertCounterPartyScreenViewModel
import jp.ikigai.cash.flow.utils.TextFieldValueSaver
import jp.ikigai.cash.flow.utils.animatedComposable
import jp.ikigai.cash.flow.utils.getIconForCounterParty
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpsertCounterPartyScreen(
    navigateBack: () -> Unit,
    chooseIcon: (String) -> Unit,
    selectedIcon: String,
    upsertCounterParty: (ImageVector, String) -> Unit,
    events: Flow<Event>,
    state: UpsertCounterPartyScreenState,
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var icon by remember(key1 = selectedIcon) {
        mutableStateOf(selectedIcon.getIconForCounterParty())
    }

    val focusRequester = remember {
        FocusRequester()
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val enabled by remember(key1 = state.enabled) {
        mutableStateOf(state.enabled)
    }

    var nameFieldValue by rememberSaveable(state.counterParty, saver = TextFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(state.counterParty.name)
        )
    }

    var nameValid by rememberSaveable {
        mutableStateOf(true)
    }

    val counterPartyUuid by remember(key1 = state.counterParty.uuid) {
        mutableStateOf(state.counterParty.uuid)
    }

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        ResetIconDialog(
            dismiss = {
                showResetDialog = false
            },
            reset = {
                icon = Constants.DEFAULT_COUNTERPARTY_ICON
                showResetDialog = false
            }
        )
    }

    var toastBarString by remember { mutableStateOf("") }

    var showToastBar by remember { mutableStateOf(false) }

    var currentEvent: Event? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(Unit) {
        events.collectLatest { event ->
            showToastBar = false
            currentEvent = event
            toastBarString = when (event) {
                Event.SaveSuccess -> "Saved successfully"
                Event.InternalError -> "Internal error"
                else -> ""
            }
            showToastBar = true
        }
    }

    LaunchedEffect(showToastBar) {
        if (showToastBar) {
            delay(2000)
            showToastBar = false
            if (currentEvent == Event.SaveSuccess) {
                navigateBack()
            }
        }
    }

    LaunchedEffect(key1 = state.enabled) {
        if (enabled) {
            nameFieldValue = nameFieldValue.copy(
                selection = TextRange(nameFieldValue.text.length)
            )
            delay(500)
            focusRequester.requestFocus()
        }
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = showToastBar,
        toastBarText = toastBarString,
        onDismissToastBar = {
            showToastBar = false
            if (currentEvent == Event.SaveSuccess) {
                navigateBack()
            }
        },
        showEmptyPlaceholder = false,
        emptyPlaceholderText = "",
        topBar = {
            TopAppBar(
                title = {
                    if (counterPartyUuid.isBlank()) {
                        Text(text = "Create counter party")
                    } else {
                        Text(text = "Update counter party")
                    }
                }
            )
        },
        bottomBar = {
            ThreeSlotRoundedBottomBar(
                navigateBack = {
                    keyboardController?.hide()
                    navigateBack()
                },
                floatingButtonIcon = {
                    Icon(
                        imageVector = TablerIcons.DeviceFloppy,
                        contentDescription = TablerIcons.DeviceFloppy.name
                    )
                },
                floatingButtonAction = {
                    if (enabled) {
                        upsertCounterParty(icon, nameFieldValue.text)
                    }
                },
            )
        }
    ) { oneHandModeBoxHeight, resetOneHandMode ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
            Icon(
                imageVector = icon,
                contentDescription = "default counter party icon",
                modifier = Modifier
                    .size(120.dp)
                    .combinedClickable(
                        enabled = enabled,
                        onClick = {
                            resetOneHandMode()
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            chooseIcon(icon.name)
                        },
                        onLongClick = {
                            resetOneHandMode()
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showResetDialog = true
                        }
                    ),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                }
            )
            RoundedCornerOutlinedTextField(
                enabled = enabled,
                value = nameFieldValue,
                onValueChange = { value ->
                    nameFieldValue = value
                    nameValid = value.text.isNotBlank()
                },
                modifier = Modifier.focusRequester(focusRequester = focusRequester),
                label = "Name",
                icon = TablerIcons.Typography,
                iconDescription = "name icon",
                isError = !nameValid,
                errorHint = "Name cannot be empty",
                onDone = {
                    keyboardController?.hide()
                }
            )
        }
    }
}

@Preview
@Composable
fun UpsertCounterPartyScreenPreview() {
    UpsertCounterPartyScreen(
        navigateBack = {},
        chooseIcon = {},
        selectedIcon = "",
        upsertCounterParty = { _, _ -> },
        events = emptyList<Event>().asFlow(),
        state = UpsertCounterPartyScreenState()
    )
}

fun NavGraphBuilder.upsertCounterPartyScreen(navController: NavController) {
    animatedComposable(
        Routes.UpsertCounterParty.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = ""
                type = NavType.StringType
            }
        )
    ) {
        val viewModel: UpsertCounterPartyScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertCounterPartyScreen(
            navigateBack = {
                navController.popBackStack()
            },
            chooseIcon = { defaultIcon ->
                navController.navigate(Routes.ChooseIcon.getRoute(defaultIcon)) {
                    launchSingleTop = true
                }
            },
            selectedIcon = it.savedStateHandle.get<String>("icon") ?: Constants.DEFAULT_COUNTERPARTY_ICON.name,
            upsertCounterParty = viewModel::upsertCounterParty,
            events = viewModel.event,
            state = state
        )
    }
}