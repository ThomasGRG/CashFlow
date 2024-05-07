package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
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
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.CurrencyDollar
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.buttons.ToggleRow
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.dialogs.ResetIconDialog
import jp.ikigai.cash.flow.ui.components.sheets.CommonSelectionSheet
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertSourceScreenState
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
    val configuration = LocalConfiguration.current

    var screenHeight by remember {
        mutableIntStateOf(configuration.screenHeightDp)
    }
    val sheetMaxHeight = remember(key1 = screenHeight) {
        screenHeight * 0.4
    }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.screenHeightDp }
            .collectLatest { screenHeight = it }
    }

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

    var selectedCurrency by rememberSaveable(state.source) {
        mutableStateOf(state.source.currency)
    }

    var currencyExpanded by remember { mutableStateOf(false) }

    if (currencyExpanded) {
        CommonSelectionSheet(
            index = currencies.indexOfFirst { it.currencyCode == selectedCurrency },
            sheetState = currencySheetState,
            dismiss = {
                scope.launch {
                    currencySheetState.hide()
                    currencyExpanded = false
                }
            },
            maxHeight = sheetMaxHeight,
        ) {
            items(
                items = currencies,
                key = { currency -> "currency-${currency.currencyCode}" }
            ) {
                ToggleRow(
                    identifier = it.currencyCode,
                    label = "${it.displayName} (${it.currencyCode})",
                    selected = it.currencyCode == selectedCurrency,
                    onClick = { currencyCode ->
                        scope.launch {
                            currencySheetState.hide()
                            currencyExpanded = false
                            selectedCurrency = currencyCode
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
                Event.NameAlreadyTaken -> "Name already in use"
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
                            selectedCurrency,
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
            RoundedCornerOutlinedTextField(
                enabled = enabled,
                value = nameFieldValue,
                onValueChange = { value ->
                    nameFieldValue = value
                    nameValid = value.text.isNotBlank()
                },
                modifier = Modifier.focusRequester(focusRequester = focusRequester),
                label = "Name",
                placeHolder = "Enter source name",
                icon = TablerIcons.Typography,
                iconDescription = "name icon",
                isError = !nameValid,
                errorHint = "Name cannot be empty",
                onDone = {
                    keyboardController?.hide()
                }
            )
            RoundedCornerOutlinedTextField(
                value = balanceFieldValue,
                onValueChange = { value ->
                    balanceFieldValue = value
                    val newBalance = value.text.toDoubleOrNull()
                    balanceValid = newBalance != null
                },
                enabled = enabled,
                label = "Balance",
                placeHolder = "Enter source balance",
                icon = TablerIcons.CashBanknote,
                iconDescription = "balance icon",
                isError = !balanceValid,
                errorHint = "Enter a number >= 0",
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                onDone = {
                    keyboardController?.hide()
                }
            )
            CustomOutlinedButton(
                enabled = enabled,
                value = selectedCurrency,
                label = "Currency",
                placeHolder = "",
                leadingIcon = {
                    Icon(
                        imageVector = TablerIcons.CurrencyDollar,
                        contentDescription = "currency icon",
                    )
                },
                onClick = {
                    resetOneHandMode()
                    currencyExpanded = true
                }
            )
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