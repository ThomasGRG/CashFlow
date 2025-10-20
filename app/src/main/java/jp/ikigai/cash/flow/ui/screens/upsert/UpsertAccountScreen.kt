package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import compose.icons.TablerIcons
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.CurrencyDollar
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.ui.components.bottombars.UpsertScreenBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmDeleteSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmNavigationSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.CurrencySheet
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.common.LandscapeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertAccountScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertAccountScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertAccountScreen(
    navigateBack: () -> Unit,
    checkNameAlreadyInUse: (String) -> Unit,
    setName: (String) -> Unit,
    setBalance: (String) -> Unit,
    setCurrency: (String) -> Unit,
    upsertAccount: (String, String, Double?) -> Unit,
    deleteAccount: () -> Unit,
    events: Flow<Event>,
    state: UpsertAccountScreenState,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val focusRequester = remember {
        FocusRequester()
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val enabled by remember(key1 = state.enabled) {
        mutableStateOf(state.enabled)
    }

    val name by remember(key1 = state.name) {
        mutableStateOf(state.name)
    }

    LaunchedEffect(key1 = state.name) {
        delay(250L)
        checkNameAlreadyInUse(state.name)
    }

    val nameValid by remember(key1 = state.nameValid) {
        mutableStateOf(state.nameValid)
    }

    val nameErrorStringRes by remember(key1 = state.nameErrorStringRes) {
        mutableIntStateOf(state.nameErrorStringRes)
    }

    val hasTransactions by remember(key1 = state.hasTransactions) {
        mutableStateOf(state.hasTransactions)
    }

    val balance by remember(key1 = state.balance) {
        mutableStateOf(state.balance)
    }

    val balanceValid by remember(key1 = state.balanceValid) {
        mutableStateOf(state.balanceValid)
    }

    val accountId by remember(key1 = state.account) {
        mutableLongStateOf(state.account.accountId)
    }

    val account by remember(key1 = state.account) {
        mutableStateOf(state.account)
    }

    val currencies by remember(key1 = state.currencies) {
        mutableStateOf(state.currencies)
    }

    val selectedCurrency by remember(key1 = state.selectedCurrency) {
        mutableStateOf(state.selectedCurrency)
    }

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    var showToastBar by remember { mutableStateOf(false) }

    var currentEvent: Event? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(Unit) {
        events.collectLatest { event ->
            showToastBar = false
            currentEvent = event
            showToastBar = true
        }
    }

    LaunchedEffect(showToastBar) {
        if (showToastBar) {
            delay(2000)
            showToastBar = false
            if (currentEvent == Event.SaveSuccess || currentEvent == Event.DeleteSuccess) {
                navigateBack()
            }
        }
    }

    val hasUnsavedChanges by remember(key1 = state.hasUnsavedChanges) {
        mutableStateOf(state.hasUnsavedChanges)
    }

    BackHandler(
        enabled = enabled && hasUnsavedChanges
    ) {
        sheetType = SheetType.CONFIRM_NAVIGATION
    }

    if (windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT && windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM) {
        LandscapeScaffold(
            loading = loading,
            showToastBar = showToastBar,
            toastBarText = currentEvent?.let {
                stringResource(id = it.message)
            } ?: "",
            onDismissToastBar = {
                showToastBar = false
                if (currentEvent == Event.SaveSuccess || currentEvent == Event.DeleteSuccess) {
                    navigateBack()
                }
            },
            sheetState = sheetState,
            showBottomSheet = sheetType != SheetType.NONE,
            bottomSheetContent = {
                when (sheetType) {
                    SheetType.CONFIRM_NAVIGATION -> {
                        ConfirmNavigationSheet(
                            message = stringResource(id = R.string.navigation_confirmation_label),
                            navigate = navigateBack,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.CURRENCY -> {
                        CurrencySheet(
                            index = currencies.indexOfFirst { it.currency.currencyCode == selectedCurrency },
                            selectedCurrency = selectedCurrency,
                            setSelectedCurrency = setCurrency,
                            currencies = currencies,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.CONFIRM_DELETE -> {
                        ConfirmDeleteSheet(
                            message = stringResource(id = R.string.delete_account_confirmation_label),
                            delete = deleteAccount,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.WARN_DELETE -> {
                        ConfirmDeleteSheet(
                            message = stringResource(id = R.string.account_transactions_deletion_warning_label),
                            delete = deleteAccount,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    else -> {}
                }
            },
            onDismissSheet = {
                sheetType = SheetType.NONE
            },
            firstColContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 2.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Constants.DEFAULT_ACCOUNT_ICON,
                        contentDescription = "default account icon",
                        modifier = Modifier
                            .size(120.dp),
                        tint = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    UpsertScreenBottomAppBar(
                        title = if (accountId == 0L) {
                            stringResource(id = R.string.create_account_label)
                        } else {
                            stringResource(id = R.string.update_account_label)
                        },
                        enabled = enabled,
                        navigateBack = {
                            keyboardController?.hide()
                            if (enabled && hasUnsavedChanges) {
                                sheetType = SheetType.CONFIRM_NAVIGATION
                            } else {
                                navigateBack()
                            }
                        },
                        saveClick = {
                            if (enabled) {
                                upsertAccount(
                                    name.trim(),
                                    selectedCurrency,
                                    balance.toDoubleOrNull()
                                )
                            }
                        },
                        deleteClick = if (accountId > 0 && enabled) {
                            {
                                sheetType = if (hasTransactions) {
                                    SheetType.WARN_DELETE
                                } else {
                                    SheetType.CONFIRM_DELETE
                                }
                            }
                        } else null
                    )
                }
            },
            secondColContent = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            rememberScrollState()
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoundedCornerOutlinedTextField(
                        enabled = enabled,
                        value = name,
                        onValueChange = setName,
                        hasValueChanged = { account.accountId > 0 && account.accountName != name },
                        modifier = Modifier.focusRequester(focusRequester = focusRequester),
                        label = stringResource(id = R.string.name_field_label),
                        placeHolder = stringResource(id = R.string.account_name_placeholder_label),
                        icon = TablerIcons.Typography,
                        iconDescription = "name icon",
                        isError = !nameValid,
                        errorHint = stringResource(id = nameErrorStringRes),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                    RoundedCornerOutlinedTextField(
                        value = balance,
                        onValueChange = setBalance,
                        hasValueChanged = {
                            val newBalance = balance.toDoubleOrNull()
                            account.accountId > 0 && newBalance != null && newBalance != account.balance
                        },
                        enabled = enabled,
                        label = stringResource(id = R.string.balance_field_label),
                        placeHolder = stringResource(id = R.string.balance_placeholder_label),
                        icon = TablerIcons.CashBanknote,
                        iconDescription = "balance icon",
                        isError = !balanceValid,
                        errorHint = stringResource(id = R.string.invalid_balance_error_label),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
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
                        hasValueChanged = {
                            account.accountId > 0 && account.currency != selectedCurrency
                        },
                        label = stringResource(id = R.string.currency_label),
                        placeHolder = "",
                        leadingIcon = TablerIcons.CurrencyDollar,
                        onClick = {
                            sheetType = SheetType.CURRENCY
                        }
                    )
                }
            }
        )
    } else {
        OneHandModeScaffold(
            loading = loading,
            showToastBar = showToastBar,
            toastBarText = currentEvent?.let {
                stringResource(id = it.message)
            } ?: "",
            onDismissToastBar = {
                showToastBar = false
                if (currentEvent == Event.SaveSuccess || currentEvent == Event.DeleteSuccess) {
                    navigateBack()
                }
            },
            sheetState = sheetState,
            showBottomSheet = sheetType != SheetType.NONE,
            bottomSheetContent = {
                when (sheetType) {
                    SheetType.CONFIRM_NAVIGATION -> {
                        ConfirmNavigationSheet(
                            message = stringResource(id = R.string.navigation_confirmation_label),
                            navigate = navigateBack,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.CURRENCY -> {
                        CurrencySheet(
                            index = currencies.indexOfFirst { it.currency.currencyCode == selectedCurrency },
                            selectedCurrency = selectedCurrency,
                            setSelectedCurrency = setCurrency,
                            currencies = currencies,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.CONFIRM_DELETE -> {
                        ConfirmDeleteSheet(
                            message = stringResource(id = R.string.delete_account_confirmation_label),
                            delete = deleteAccount,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.WARN_DELETE -> {
                        ConfirmDeleteSheet(
                            message = stringResource(id = R.string.account_transactions_deletion_warning_label),
                            delete = deleteAccount,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    else -> {}
                }
            },
            onDismissSheet = {
                sheetType = SheetType.NONE
            },
            topBar = { scrollBehavior, expandedHeight ->
                LargeTopAppBar(
                    title = {
                        if (accountId == 0L) {
                            Text(text = stringResource(id = R.string.create_account_label))
                        } else {
                            Text(text = stringResource(id = R.string.update_account_label))
                        }
                    },
                    expandedHeight = expandedHeight,
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                UpsertScreenBottomAppBar(
                    enabled = enabled,
                    navigateBack = {
                        keyboardController?.hide()
                        if (enabled && hasUnsavedChanges) {
                            sheetType = SheetType.CONFIRM_NAVIGATION
                        } else {
                            navigateBack()
                        }
                    },
                    saveClick = {
                        if (enabled) {
                            upsertAccount(
                                name.trim(),
                                selectedCurrency,
                                balance.toDoubleOrNull()
                            )
                        }
                    },
                    deleteClick = if (accountId > 0 && enabled) {
                        {
                            sheetType = if (hasTransactions) {
                                SheetType.WARN_DELETE
                            } else {
                                SheetType.CONFIRM_DELETE
                            }
                        }
                    } else null
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Constants.DEFAULT_ACCOUNT_ICON,
                    contentDescription = "default account icon",
                    modifier = Modifier
                        .size(120.dp),
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                RoundedCornerOutlinedTextField(
                    enabled = enabled,
                    value = name,
                    onValueChange = setName,
                    hasValueChanged = { account.accountId > 0 && account.accountName != name },
                    modifier = Modifier.focusRequester(focusRequester = focusRequester),
                    label = stringResource(id = R.string.name_field_label),
                    placeHolder = stringResource(id = R.string.account_name_placeholder_label),
                    icon = TablerIcons.Typography,
                    iconDescription = "name icon",
                    isError = !nameValid,
                    errorHint = stringResource(id = nameErrorStringRes),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    onDone = {
                        keyboardController?.hide()
                    }
                )
                RoundedCornerOutlinedTextField(
                    value = balance,
                    onValueChange = setBalance,
                    hasValueChanged = {
                        val newBalance = balance.toDoubleOrNull()
                        account.accountId > 0 && newBalance != null && newBalance != account.balance
                    },
                    enabled = enabled,
                    label = stringResource(id = R.string.balance_field_label),
                    placeHolder = stringResource(id = R.string.balance_placeholder_label),
                    icon = TablerIcons.CashBanknote,
                    iconDescription = "balance icon",
                    isError = !balanceValid,
                    errorHint = stringResource(id = R.string.invalid_balance_error_label),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
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
                    hasValueChanged = {
                        account.accountId > 0 && account.currency != selectedCurrency
                    },
                    label = stringResource(id = R.string.currency_label),
                    placeHolder = "",
                    leadingIcon = TablerIcons.CurrencyDollar,
                    onClick = {
                        sheetType = SheetType.CURRENCY
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun UpsertAccountScreenPreview() {
    UpsertAccountScreen(
        navigateBack = {},
        checkNameAlreadyInUse = {},
        setName = {},
        setBalance = {},
        setCurrency = {},
        upsertAccount = { _, _, _ -> },
        deleteAccount = {},
        events = emptyList<Event>().asFlow(),
        state = UpsertAccountScreenState()
    )
}

fun NavGraphBuilder.upsertAccountScreen(navController: NavController) {
    composable(
        route = Routes.UpsertAccount.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = 0L
                type = NavType.LongType
            }
        ),
    ) {
        val viewModel: UpsertAccountScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertAccountScreen(
            navigateBack = {
                navController.popBackStack()
            },
            checkNameAlreadyInUse = viewModel::checkNameAlreadyInUse,
            setName = viewModel::setName,
            setBalance = viewModel::setBalance,
            setCurrency = viewModel::setCurrency,
            upsertAccount = viewModel::upsertAccount,
            deleteAccount = viewModel::deleteAccount,
            events = viewModel.event,
            state = state
        )
    }
}