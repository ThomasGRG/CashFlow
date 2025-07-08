package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import compose.icons.TablerIcons
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.CurrencyDollar
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.popups.ConfirmDeletePopup
import jp.ikigai.cash.flow.ui.components.popups.ConfirmNavigationPopup
import jp.ikigai.cash.flow.ui.components.popups.CurrencyPopup
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertAccountScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertAccountScreenViewModel
import jp.ikigai.cash.flow.utils.TextFieldValueSaver
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertAccountScreen(
    navigateBack: () -> Unit,
    checkNameAlreadyInUse: (String) -> Unit,
    setName: (String) -> Unit,
    upsertTransactionSource: (String, String, Double) -> Unit,
    deleteSource: () -> Unit,
    hasChanges: (String, String) -> Boolean,
    events: Flow<Event>,
    state: UpsertAccountScreenState,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

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

    var balanceFieldValue by rememberSaveable(state.account, saver = TextFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(state.account.balance.toString())
        )
    }

    var balanceValid by rememberSaveable {
        mutableStateOf(true)
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

    var selectedCurrency by rememberSaveable(state.account) {
        mutableStateOf(state.account.currency)
    }

    var popupType by remember {
        mutableStateOf(PopupType.NONE)
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

    BackHandler {
        if (enabled && hasChanges(balanceFieldValue.text, selectedCurrency)) {
            popupType = PopupType.CONFIRM_NAVIGATION
        } else {
            navigateBack()
        }
    }

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
        showBottomPopup = popupType != PopupType.NONE,
        bottomPopupContent = { hidePopup ->
            when (popupType) {
                PopupType.CONFIRM_NAVIGATION -> {
                    ConfirmNavigationPopup(
                        message = stringResource(id = R.string.navigation_confirmation_label),
                        dismiss = hidePopup,
                        navigate = navigateBack
                    )
                }

                PopupType.CURRENCY -> {
                    CurrencyPopup(
                        index = currencies.indexOfFirst { it.currency.currencyCode == selectedCurrency },
                        selectedCurrency = selectedCurrency,
                        setSelectedCurrency = { currency ->
                            selectedCurrency = currency
                        },
                        currencies = currencies,
                        dismiss = hidePopup
                    )
                }

                PopupType.CONFIRM_DELETE -> {
                    ConfirmDeletePopup(
                        message = stringResource(id = R.string.delete_account_confirmation_label),
                        dismiss = hidePopup,
                        delete = deleteSource
                    )
                }

                PopupType.WARN_DELETE -> {
                    ConfirmDeletePopup(
                        message = stringResource(id = R.string.account_transactions_deletion_warning_label),
                        dismiss = hidePopup,
                        delete = deleteSource
                    )
                }

                else -> {}
            }
        },
        onDismissPopup = {
            popupType = PopupType.NONE
        },
        showEmptyPlaceholder = false,
        emptyPlaceholderText = "",
        topBar = {
            TopAppBar(
                title = {
                    if (accountId == 0L) {
                        Text(text = stringResource(id = R.string.create_account_label))
                    } else {
                        Text(text = stringResource(id = R.string.update_account_label))
                    }
                }
            )
        },
        bottomBar = {
            ThreeSlotRoundedBottomBar(
                navigateBack = {
                    keyboardController?.hide()
                    if (enabled && hasChanges(balanceFieldValue.text, selectedCurrency)) {
                        popupType = PopupType.CONFIRM_NAVIGATION
                    } else {
                        navigateBack()
                    }
                },
                enabled = enabled,
                floatingButtonIcon = {
                    Icon(
                        imageVector = TablerIcons.DeviceFloppy,
                        contentDescription = TablerIcons.DeviceFloppy.name
                    )
                },
                floatingButtonAction = {
                    if (enabled) {
                        upsertTransactionSource(
                            name.trim(),
                            selectedCurrency,
                            balanceFieldValue.text.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                extraButtonIcon = if (accountId > 0) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = Icons.Outlined.Delete.name,
                        )
                    }
                } else null,
                extraButtonAction = if (accountId > 0 && enabled) {
                    {
                        popupType = if (hasTransactions) {
                            PopupType.WARN_DELETE
                        } else {
                            PopupType.CONFIRM_DELETE
                        }
                    }
                } else null
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
                imageVector = Constants.DEFAULT_ACCOUNT_ICON,
                contentDescription = "default source icon",
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
                value = balanceFieldValue,
                onValueChange = { value ->
                    balanceFieldValue = value
                    val newBalance = value.text.toDoubleOrNull()
                    balanceValid = newBalance != null
                },
                hasValueChanged = {
                    val newBalance = balanceFieldValue.text.toDoubleOrNull()
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
                    resetOneHandMode()
                    popupType = PopupType.CURRENCY
                }
            )
        }
    }
}

@Preview
@Composable
fun UpsertSourceScreenPreview() {
    UpsertAccountScreen(
        navigateBack = {},
        checkNameAlreadyInUse = {},
        setName = {},
        upsertTransactionSource = { _, _, _ -> },
        deleteSource = {},
        hasChanges = { _, _ -> false },
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
            upsertTransactionSource = viewModel::upsertSource,
            deleteSource = viewModel::deleteSource,
            hasChanges = viewModel::hasChanges,
            events = viewModel.event,
            state = state
        )
    }
}