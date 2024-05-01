package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceFloppy
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.screenStates.upsert.UpsertSourceScreenState
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.ToggleButton
import jp.ikigai.cash.flow.ui.components.common.AnimatedTextFieldErrorLabel
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.dialogs.ResetIconDialog
import jp.ikigai.cash.flow.ui.components.sheets.CommonSelectionSheet
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertSourceScreenViewModel
import jp.ikigai.cash.flow.utils.TextFieldValueSaver
import jp.ikigai.cash.flow.utils.animatedComposable
import jp.ikigai.cash.flow.utils.getIconForSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpsertSourceScreen(
    navigateBack: () -> Unit,
    chooseIcon: (String) -> Unit,
    selectedIcon: String,
    upsertTransactionSource: (ImageVector, String, String, Double) -> Unit,
    events: Flow<Event>,
    state: UpsertSourceScreenState,
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    val currencySheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var icon by remember(key1 = selectedIcon) {
        mutableStateOf(selectedIcon.getIconForSource())
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

    var nameFieldValue by rememberSaveable(state.source, saver = TextFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(state.source.name)
        )
    }

    var nameValid by rememberSaveable {
        mutableStateOf(true)
    }

    var balanceFieldValue by rememberSaveable(state.source, saver = TextFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(state.source.balance.toString())
        )
    }

    var balanceValid by rememberSaveable {
        mutableStateOf(true)
    }

    val sourceUuid by remember(key1 = state.source.uuid) {
        mutableStateOf(state.source.uuid)
    }

    val currencies by remember(key1 = state.currencies) {
        mutableStateOf(state.currencies)
    }

    var selectedCurrencyFieldValue by rememberSaveable(state.source, saver = TextFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(state.source.currency)
        )
    }

    var currencyExpanded by remember { mutableStateOf(false) }

    if (currencyExpanded) {
        CommonSelectionSheet(
            sheetState = currencySheetState,
            dismiss = {
                scope.launch {
                    currencySheetState.hide()
                    currencyExpanded = false
                }
            },
            rowCount = 3,
            maxHeight = 180.0
        ) {
            items(
                items = currencies,
                key = { currency -> "currency-${currency.currencyCode}" }
            ) {
                ToggleButton(
                    label = "${it.displayName} (${it.currencyCode})",
                    selected = it.currencyCode == selectedCurrencyFieldValue.text,
                    toggle = {
                        selectedCurrencyFieldValue =
                            selectedCurrencyFieldValue.copy(text = it.currencyCode)
                        scope.launch {
                            currencySheetState.hide()
                            currencyExpanded = false
                        }
                    }
                )
            }
        }
    }

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        ResetIconDialog(
            dismiss = {
                showResetDialog = false
            },
            reset = {
                icon = Constants.DEFAULT_SOURCE_ICON
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
                    if (sourceUuid.isBlank()) {
                        Text(text = "Create source")
                    } else {
                        Text(text = "Update source")
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
                        upsertTransactionSource(
                            icon,
                            nameFieldValue.text,
                            selectedCurrencyFieldValue.text,
                            balanceFieldValue.text.toDoubleOrNull() ?: 0.0
                        )
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
                contentDescription = "default source icon",
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
            OutlinedTextField(
                value = nameFieldValue,
                onValueChange = { value ->
                    nameFieldValue = value
                    nameValid = value.text.isNotBlank()
                },
                enabled = enabled,
                modifier = Modifier
                    .focusRequester(focusRequester = focusRequester)
                    .fillMaxWidth(),
                label = {
                    Text(text = "Name")
                },
                isError = !nameValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                shape = RoundedCornerShape(14.dp),
            )
            AnimatedTextFieldErrorLabel(
                visible = !nameValid,
                errorLabel = "Name cannot be empty"
            )
            OutlinedTextField(
                value = balanceFieldValue,
                onValueChange = { value ->
                    balanceFieldValue = value
                    val newBalance = value.text.toDoubleOrNull()
                    balanceValid = newBalance != null
                },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth(),
                label = {
                    Text(text = "Balance")
                },
                isError = !balanceValid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Number
                ),
                shape = RoundedCornerShape(14.dp),
            )
            AnimatedTextFieldErrorLabel(
                visible = !balanceValid,
                errorLabel = "Enter a number >= 0"
            )
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {
                    if (enabled) {
                        resetOneHandMode()
                        currencyExpanded = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCurrencyFieldValue,
                    onValueChange = {},
                    enabled = enabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                    },
                    label = {
                        Text(text = "Currency")
                    },
                    singleLine = true,
                    readOnly = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun UpsertSourceScreenPreview() {
    UpsertSourceScreen(
        navigateBack = {},
        chooseIcon = {},
        selectedIcon = "",
        upsertTransactionSource = { _, _, _, _ -> },
        events = emptyList<Event>().asFlow(),
        state = UpsertSourceScreenState()
    )
}

fun NavGraphBuilder.upsertSourceScreen(navController: NavController) {
    animatedComposable(
        Routes.UpsertSource.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = ""
                type = NavType.StringType
            }
        ),
    ) {
        val viewModel: UpsertSourceScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertSourceScreen(
            navigateBack = {
                navController.popBackStack()
            },
            chooseIcon = { defaultIcon ->
                navController.navigate(Routes.ChooseIcon.getRoute(defaultIcon)) {
                    launchSingleTop = true
                }
            },
            selectedIcon = it.savedStateHandle.get<String>("icon")
                ?: Constants.DEFAULT_SOURCE_ICON.name,
            upsertTransactionSource = viewModel::upsertSource,
            events = viewModel.event,
            state = state
        )
    }
}