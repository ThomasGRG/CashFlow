package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.FileText
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.popups.ConfirmDeletePopup
import jp.ikigai.cash.flow.ui.components.popups.ConfirmNavigationPopup
import jp.ikigai.cash.flow.ui.components.popups.DatePickerPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectAccountPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCategoryPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCounterPartyPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectMethodPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectTransactionTypePopup
import jp.ikigai.cash.flow.ui.components.popups.TimePickerPopup
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertTransactionScreenViewModel
import jp.ikigai.cash.flow.utils.TextFieldValueSaver
import jp.ikigai.cash.flow.utils.animatedComposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertTransactionScreen(
    navigateBack: () -> Unit,
    setLocale: (Locale?) -> Unit,
    setTitle: (String) -> Unit,
    setAmount: (String) -> Unit,
    setDate: (ZonedDateTime) -> Unit,
    setTime: (ZonedDateTime) -> Unit,
    setSelectedAccount: (Account) -> Unit,
    setSelectedCategory: (Category) -> Unit,
    setSelectedCounterParty: (CounterParty) -> Unit,
    setSelectedMethod: (Method) -> Unit,
    setTransactionType: (TransactionType) -> Unit,
    upsertTransaction: (String, String) -> Unit,
    deleteTransaction: () -> Unit,
    filterTransactionTitles: (String) -> List<String>,
    hasChanges: (String) -> Boolean,
    events: Flow<Event>,
    state: UpsertTransactionScreenState
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val configuration = LocalConfiguration.current

    val locale by remember(key1 = configuration) {
        mutableStateOf(
            ConfigurationCompat.getLocales(configuration).get(0)
        )
    }

    LaunchedEffect(key1 = locale) {
        setLocale(locale)
    }

    val selectableDates by remember {
        mutableStateOf(
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val currentDateMillis = LocalDate.now(ZoneId.of("UTC")).plusDays(1)
                        .toEpochDay() * 24 * 60 * 60 * 1000
                    return utcTimeMillis < currentDateMillis
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year <= YearMonth.now().year
                }
            }
        )
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val enabled by remember(key1 = state.enabled) {
        mutableStateOf(state.enabled)
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

    val accounts by remember(key1 = state.accounts) {
        mutableStateOf(state.accounts)
    }

    val selectedAccount by remember(key1 = state.selectedAccount) {
        mutableStateOf(state.selectedAccount)
    }

    val accountValid by remember(key1 = state.accountValid) {
        mutableStateOf(state.accountValid)
    }

    val accountErrorStringRes by remember(key1 = state.accountErrorStringRes) {
        mutableIntStateOf(state.accountErrorStringRes)
    }

    val categories by remember(key1 = state.categories) {
        mutableStateOf(state.categories)
    }

    val selectedCategory by remember(key1 = state.selectedCategory) {
        mutableStateOf(state.selectedCategory)
    }

    val categoryValid by remember(key1 = state.categoryValid) {
        mutableStateOf(state.categoryValid)
    }

    val counterParties by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties)
    }

    val selectedCounterParty by remember(key1 = state.selectedCounterParty) {
        mutableStateOf(state.selectedCounterParty)
    }

    val methods by remember(key1 = state.methods) {
        mutableStateOf(state.methods)
    }

    val selectedMethod by remember(key1 = state.selectedMethod) {
        mutableStateOf(state.selectedMethod)
    }

    val methodValid by remember(key1 = state.methodValid) {
        mutableStateOf(state.methodValid)
    }

    val transactionType by remember(key1 = state.type) {
        mutableStateOf(state.type)
    }

    val transactionId by remember(key1 = state.transaction) {
        mutableLongStateOf(state.transaction.id)
    }

    val transaction by remember(key1 = state.transaction) {
        mutableStateOf(state.transaction)
    }

    val titleFieldInteractionSource = remember {
        MutableInteractionSource()
    }

    val isTitleFieldFocused by titleFieldInteractionSource.collectIsFocusedAsState()

    val title by remember(key1 = state.title) {
        mutableStateOf(state.title)
    }

    val titleValid by remember(key1 = state.titleValid) {
        mutableStateOf(state.titleValid)
    }

    val filteredTransactionTitles by remember(
        key1 = title,
        key2 = state.transactionTitles
    ) {
        mutableStateOf(filterTransactionTitles(title))
    }

    var descriptionFieldValue by rememberSaveable(state.transaction, saver = TextFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(state.transaction.description)
        )
    }

    val amount by remember(key1 = state.displayAmount) {
        mutableStateOf(state.displayAmount)
    }

    val amountValid by remember(key1 = state.amountValid) {
        mutableStateOf(state.amountValid)
    }

    val dateTime by remember(key1 = state.dateTime) {
        mutableStateOf(state.dateTime)
    }

    val date by remember(key1 = state.dateString) {
        mutableStateOf(state.dateString)
    }

    val time by remember(key1 = state.timeString) {
        mutableStateOf(state.timeString)
    }

    val timeValid by remember(key1 = state.timeValid) {
        mutableStateOf(state.timeValid)
    }

    var popupType by remember {
        mutableStateOf(PopupType.NONE)
    }

    BackHandler {
        if (enabled && hasChanges(descriptionFieldValue.text)) {
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

                PopupType.DATE -> {
                    DatePickerPopup(
                        date = dateTime,
                        setDate = setDate,
                        selectableDates = selectableDates,
                        dismiss = hidePopup
                    )
                }

                PopupType.TIME -> {
                    TimePickerPopup(
                        time = dateTime,
                        updateTime = setTime,
                        dismiss = hidePopup
                    )
                }

                PopupType.CATEGORY -> {
                    SelectCategoryPopup(
                        index = categories.indexOfFirst { it.id == selectedCategory.id }
                            .coerceAtLeast(0),
                        selectedCategoryId = selectedCategory.id,
                        setSelectedCategory = setSelectedCategory,
                        categories = categories,
                        dismiss = hidePopup
                    )
                }

                PopupType.COUNTERPARTY -> {
                    SelectCounterPartyPopup(
                        index = counterParties.indexOfFirst { it.id == selectedCounterParty.id }
                            .coerceAtLeast(0),
                        selectedCounterPartyId = selectedCounterParty.id,
                        setSelectedCounterParty = setSelectedCounterParty,
                        counterParties = counterParties,
                        dismiss = hidePopup
                    )
                }

                PopupType.METHOD -> {
                    SelectMethodPopup(
                        index = methods.indexOfFirst { it.id == selectedMethod.id }
                            .coerceAtLeast(0),
                        selectedMethodId = selectedMethod.id,
                        setSelectedMethod = setSelectedMethod,
                        methods = methods,
                        dismiss = hidePopup
                    )
                }

                PopupType.ACCOUNT -> {
                    SelectAccountPopup(
                        index = accounts.indexOfFirst { it.id == selectedAccount.id }
                            .coerceAtLeast(0),
                        selectedAccountId = selectedAccount.id,
                        setSelectedAccount = setSelectedAccount,
                        accounts = accounts,
                        dismiss = hidePopup
                    )
                }

                PopupType.TYPE -> {
                    SelectTransactionTypePopup(
                        selectedTransactionType = transactionType,
                        setSelectedTransactionType = setTransactionType,
                        dismiss = hidePopup
                    )
                }

                PopupType.CONFIRM_DELETE -> {
                    ConfirmDeletePopup(
                        message = stringResource(id = R.string.delete_transaction_confirmation_label),
                        delete = deleteTransaction,
                        dismiss = hidePopup
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
                    if (transactionId == 0L) {
                        Text(text = stringResource(id = R.string.create_transaction_label))
                    } else {
                        Text(text = stringResource(id = R.string.update_transaction_label))
                    }
                }
            )
        },
        bottomBar = {
            ThreeSlotRoundedBottomBar(
                navigateBack = {
                    keyboardController?.hide()
                    if (enabled && hasChanges(descriptionFieldValue.text)) {
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
                        upsertTransaction(title, descriptionFieldValue.text)
                    }
                },
                extraButtonIcon = if (transactionId > 0) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = Icons.Outlined.Delete.name,
                        )
                    }
                } else null,
                extraButtonAction = if (transactionId > 0 && enabled) {
                    { popupType = PopupType.CONFIRM_DELETE }
                } else null
            )
        }
    ) { oneHandModeBoxHeight, resetOneHandMode ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(
                key = "one-hand-mode-expand-row",
                contentType = "row"
            ) {
                OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
            }
            item(
                key = "title",
                contentType = "type-enabled"
            ) {
                RoundedCornerOutlinedTextField(
                    value = title,
                    onValueChange = {
                        setTitle(it)
                    },
                    initialValue = transaction.title,
                    enabled = enabled,
                    isFocused = isTitleFieldFocused,
                    label = stringResource(id = R.string.title_field_label),
                    placeHolder = stringResource(id = R.string.title_placeholder_label),
                    icon = TablerIcons.Typography,
                    iconDescription = "title icon",
                    isError = !titleValid,
                    errorHint = stringResource(id = R.string.field_required_error_label),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    onDone = {
                        keyboardController?.hide()
                    },
                    interactionSource = titleFieldInteractionSource,
                    boxModifier = Modifier.animateItem()
                )
            }
            if (filteredTransactionTitles.isNotEmpty() && (title.isBlank() || isTitleFieldFocused)) {
                item(
                    key = "auto-complete",
                    contentType = "lazyRow"
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            8.dp,
                            alignment = Alignment.CenterHorizontally
                        )
                    ) {
                        items(
                            items = filteredTransactionTitles,
                            key = { title -> title }
                        ) { title ->
                            OutlinedButton(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    setTitle(title)
                                },
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier.animateItem()
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
            item(
                key = "description",
                contentType = "type-enabled"
            ) {
                RoundedCornerOutlinedTextField(
                    value = descriptionFieldValue,
                    onValueChange = { descriptionFieldValue = it },
                    hasValueChanged = {
                        transaction.id > 0 && transaction.description != descriptionFieldValue.text
                    },
                    enabled = enabled,
                    label = stringResource(id = R.string.description_field_label),
                    placeHolder = stringResource(id = R.string.description_placeholder_label),
                    icon = TablerIcons.FileText,
                    iconDescription = "description icon",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    onDone = {
                        keyboardController?.hide()
                    },
                    boxModifier = Modifier.animateItem()
                )
            }
            item(
                key = "amount",
                contentType = "type-enabled"
            ) {
                RoundedCornerOutlinedTextField(
                    value = amount,
                    onValueChange = setAmount,
                    hasValueChanged = {
                        val newAmount = amount.toDoubleOrNull()
                        newAmount != null && newAmount > 0 && transaction.id > 0 && newAmount != transaction.amount
                    },
                    enabled = enabled,
                    label = stringResource(id = R.string.amount_label),
                    placeHolder = stringResource(id = R.string.transaction_amount_placeholder_label),
                    icon = TablerIcons.CashBanknote,
                    iconDescription = "amount icon",
                    isError = !amountValid,
                    errorHint = stringResource(id = R.string.invalid_amount_error_label),
                    onDone = {
                        keyboardController?.hide()
                    },
                    boxModifier = Modifier.animateItem()
                )
            }
            item(
                key = "transaction-type",
                contentType = "drop-down"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = stringResource(id = transactionType.label),
                    label = stringResource(id = R.string.transaction_type_field_label),
                    hasValueChanged = {
                        transaction.id > 0 && transactionType != transaction.type
                    },
                    placeHolder = "",
                    leadingIcon = transactionType.icon,
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.TYPE
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "date",
                contentType = "drop-down"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = date,
                    hasValueChanged = {
                        val initialDate = transaction.time.toLocalDate()
                        transaction.id > 0 && (!dateTime.toLocalDate()
                            .equals(initialDate))
                    },
                    label = stringResource(id = R.string.date_field_label),
                    placeHolder = "",
                    leadingIcon = TablerIcons.CalendarEvent,
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.DATE
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "time",
                contentType = "drop-down"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = time,
                    hasValueChanged = {
                        val initialDateTime = transaction.time
                        transaction.id > 0 && (dateTime.hour != initialDateTime.hour || dateTime.minute != initialDateTime.minute)
                    },
                    label = stringResource(id = R.string.time_field_label),
                    placeHolder = "",
                    leadingIcon = TablerIcons.Alarm,
                    isError = !timeValid,
                    errorHint = stringResource(id = R.string.future_time_error_label),
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.TIME
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "category",
                contentType = "drop-down"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedCategory.name,
                    hasValueChanged = {
                        transaction.id > 0 && transaction.category.targetId != selectedCategory.id
                    },
                    label = stringResource(id = R.string.category_field_label),
                    placeHolder = stringResource(id = R.string.select_category_placeholder_label),
                    isError = !categoryValid,
                    errorHint = stringResource(id = R.string.field_required_error_label),
                    leadingIcon = selectedCategory.icon,
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.CATEGORY
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "counter-party",
                contentType = "drop-down"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedCounterParty.name,
                    hasValueChanged = {
                        if (transaction.id > 0) {
                            if (transaction.counterParty.targetId > 0 && selectedCounterParty.id == 0L) {
                                true
                            } else if (transaction.counterParty.targetId == 0L && selectedCounterParty.id > 0) {
                                true
                            } else if (transaction.counterParty.targetId == 0L && selectedCounterParty.id == 0L) {
                                false
                            } else {
                                transaction.counterParty.targetId != selectedCounterParty.id
                            }
                        } else {
                            false
                        }
                    },
                    label = stringResource(id = R.string.counter_party_field_label),
                    placeHolder = stringResource(id = R.string.counter_party_placeholder_label),
                    leadingIcon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedCounterParty(CounterParty())
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.COUNTERPARTY
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "method",
                contentType = "drop-down"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedMethod.name,
                    hasValueChanged = {
                        transaction.id > 0 && transaction.method.targetId != selectedMethod.id
                    },
                    label = stringResource(id = R.string.method_field_label),
                    placeHolder = stringResource(id = R.string.select_method_placeholder_label),
                    isError = !methodValid,
                    errorHint = stringResource(id = R.string.field_required_error_label),
                    leadingIcon = Constants.DEFAULT_METHOD_ICON,
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.METHOD
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "account",
                contentType = "drop-down"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = if (selectedAccount.id > 0) "${selectedAccount.name} - ${selectedAccount.formattedBalance}" else "",
                    hasValueChanged = {
                        transaction.id > 0 && transaction.account.targetId != selectedAccount.id
                    },
                    label = stringResource(id = R.string.account_field_label),
                    placeHolder = stringResource(id = R.string.select_account_placeholder_label),
                    isError = !accountValid,
                    errorHint = stringResource(id = accountErrorStringRes),
                    leadingIcon = Constants.DEFAULT_ACCOUNT_ICON,
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.ACCOUNT
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Preview
@Composable
fun UpsertTransactionScreenPreview() {
    UpsertTransactionScreen(
        navigateBack = {},
        setLocale = {},
        setTitle = {},
        setAmount = {},
        setDate = {},
        setTime = {},
        setSelectedCategory = {},
        setSelectedCounterParty = {},
        setSelectedMethod = {},
        setSelectedAccount = {},
        setTransactionType = {},
        upsertTransaction = { _, _ -> },
        deleteTransaction = {},
        filterTransactionTitles = { emptyList() },
        hasChanges = { _ -> false },
        events = emptyList<Event>().asFlow(),
        state = UpsertTransactionScreenState()
    )
}

fun NavGraphBuilder.upsertTransactionScreen(navController: NavController) {
    animatedComposable(
        Routes.UpsertTransaction.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = 0L
                type = NavType.LongType
            },
            navArgument("templateId") {
                defaultValue = 0L
                type = NavType.LongType
            }
        )
    ) {
        val viewModel: UpsertTransactionScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertTransactionScreen(
            navigateBack = {
                navController.popBackStack()
            },
            setLocale = viewModel::setLocale,
            setTitle = viewModel::setTitle,
            setAmount = viewModel::setAmount,
            setDate = viewModel::setDate,
            setTime = viewModel::setTime,
            setSelectedAccount = viewModel::setSelectedAccount,
            setSelectedCategory = viewModel::setSelectedCategory,
            setSelectedCounterParty = viewModel::setSelectedCounterParty,
            setSelectedMethod = viewModel::setSelectedMethod,
            setTransactionType = viewModel::setTransactionType,
            upsertTransaction = viewModel::upsertTransaction,
            deleteTransaction = viewModel::deleteTransaction,
            filterTransactionTitles = viewModel::filterTransactionTitles,
            hasChanges = viewModel::hasChanges,
            events = viewModel.event,
            state = state
        )
    }
}