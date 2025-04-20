package jp.ikigai.cash.flow.ui.screens.upsert

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
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.common.WaitDialog
import jp.ikigai.cash.flow.ui.components.popups.ConfirmDeletePopup
import jp.ikigai.cash.flow.ui.components.popups.DatePickerPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCategoryPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCounterPartyPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectMethodPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectSourcePopup
import jp.ikigai.cash.flow.ui.components.popups.SelectTransactionTypePopup
import jp.ikigai.cash.flow.ui.components.popups.TimePickerPopup
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertTransactionScreenViewModel
import jp.ikigai.cash.flow.utils.TextFieldValueSaver
import jp.ikigai.cash.flow.utils.animatedComposable
import jp.ikigai.cash.flow.utils.toZonedDateTime
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
    setSelectedCategory: (Category) -> Unit,
    setSelectedCounterParty: (CounterParty) -> Unit,
    setSelectedMethod: (Method) -> Unit,
    setSelectedSource: (Source) -> Unit,
    setTransactionType: (TransactionType) -> Unit,
    upsertTransaction: (String, String) -> Unit,
    deleteTransaction: () -> Unit,
    filterTransactionTitles: (String) -> List<String>,
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

    val writeOngoing by remember(key1 = state.writeOngoing) {
        mutableStateOf(state.writeOngoing)
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

    val sources by remember(key1 = state.sources) {
        mutableStateOf(state.sources)
    }

    val selectedSource by remember(key1 = state.selectedSource) {
        mutableStateOf(state.selectedSource)
    }

    val sourceValid by remember(key1 = state.sourceValid) {
        mutableStateOf(state.sourceValid)
    }

    val sourceErrorStringRes by remember(key1 = state.sourceErrorStringRes) {
        mutableIntStateOf(state.sourceErrorStringRes)
    }

    val transactionType by remember(key1 = state.type) {
        mutableStateOf(state.type)
    }

    val transactionUuid by remember(key1 = state.transaction) {
        mutableStateOf(state.transaction.uuid)
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

    var popupType by remember {
        mutableStateOf(PopupType.NONE)
    }

    if (writeOngoing) {
        WaitDialog()
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
                PopupType.DATE -> {
                    DatePickerPopup(
                        date = dateTime,
                        setDate = setDate,
                        selectableDates = selectableDates,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.TIME -> {
                    TimePickerPopup(
                        time = dateTime,
                        updateTime = setTime,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.CATEGORY -> {
                    SelectCategoryPopup(
                        index = categories.indexOfFirst { it.uuid == selectedCategory.uuid }
                            .coerceAtLeast(0),
                        selectedCategoryUUID = selectedCategory.uuid,
                        setSelectedCategory = setSelectedCategory,
                        categories = categories,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.COUNTERPARTY -> {
                    SelectCounterPartyPopup(
                        index = counterParties.indexOfFirst { it.uuid == selectedCounterParty.uuid }
                            .coerceAtLeast(0),
                        selectedCounterPartyUUID = selectedCounterParty.uuid,
                        setSelectedCounterParty = setSelectedCounterParty,
                        counterParties = counterParties,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.METHOD -> {
                    SelectMethodPopup(
                        index = methods.indexOfFirst { it.uuid == selectedMethod.uuid }
                            .coerceAtLeast(0),
                        selectedMethodUUID = selectedMethod.uuid,
                        setSelectedMethod = setSelectedMethod,
                        methods = methods,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.SOURCE -> {
                    SelectSourcePopup(
                        index = sources.indexOfFirst { it.uuid == selectedSource.uuid }
                            .coerceAtLeast(0),
                        selectedSourceUUID = selectedSource.uuid,
                        setSelectedSource = setSelectedSource,
                        sources = sources,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.TYPE -> {
                    SelectTransactionTypePopup(
                        selectedTransactionType = transactionType,
                        setSelectedTransactionType = setTransactionType,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.CONFIRM_DELETE -> {
                    ConfirmDeletePopup(
                        message = stringResource(id = R.string.delete_transaction_confirmation_label),
                        delete = deleteTransaction,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
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
                    if (transactionUuid.isBlank()) {
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
                    navigateBack()
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
                extraButtonIcon = if (transactionUuid.isNotBlank()) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = Icons.Outlined.Delete.name,
                        )
                    }
                } else null,
                extraButtonAction = if (transactionUuid.isNotBlank() && enabled) {
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
                        transaction.uuid.isNotEmpty() && transaction.description != descriptionFieldValue.text
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
                        newAmount != null && newAmount > 0 && transaction.uuid.isNotEmpty() && newAmount != transaction.amount
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
                        transaction.uuid.isNotEmpty() && transactionType != transaction.type
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
                        val initialDate = transaction.time.toZonedDateTime().toLocalDate()
                        transaction.uuid.isNotEmpty() && (!dateTime.toLocalDate()
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
                        val initialDateTime = transaction.time.toZonedDateTime()
                        transaction.uuid.isNotEmpty() && (dateTime.hour != initialDateTime.hour || dateTime.minute != initialDateTime.minute)
                    },
                    label = stringResource(id = R.string.time_field_label),
                    placeHolder = "",
                    leadingIcon = TablerIcons.Alarm,
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
                        transaction.uuid.isNotEmpty() && transaction.category?.uuid != selectedCategory.uuid
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
                        if (transaction.uuid.isNotEmpty()) {
                            if (transaction.counterParty != null && selectedCounterParty.uuid.isEmpty()) {
                                true
                            } else if (transaction.counterParty == null && selectedCounterParty.uuid.isNotEmpty()) {
                                true
                            } else if (transaction.counterParty == null && selectedCounterParty.uuid.isEmpty()) {
                                false
                            } else {
                                transaction.counterParty?.uuid != selectedCounterParty.uuid
                            }
                        } else {
                            false
                        }
                    },
                    label = stringResource(id = R.string.counter_party_field_label),
                    placeHolder = stringResource(id = R.string.counter_party_placeholder_label),
                    leadingIcon = selectedCounterParty.icon,
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
                        transaction.uuid.isNotEmpty() && transaction.method?.uuid != selectedMethod.uuid
                    },
                    label = stringResource(id = R.string.method_field_label),
                    placeHolder = stringResource(id = R.string.select_method_placeholder_label),
                    isError = !methodValid,
                    errorHint = stringResource(id = R.string.field_required_error_label),
                    leadingIcon = selectedMethod.icon,
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.METHOD
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "source",
                contentType = "drop-down"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = if (selectedSource.uuid.isNotEmpty()) "${selectedSource.name} - ${selectedSource.displayBalance}" else "",
                    hasValueChanged = {
                        transaction.uuid.isNotEmpty() && transaction.source?.uuid != selectedSource.uuid
                    },
                    label = stringResource(id = R.string.source_field_label),
                    placeHolder = stringResource(id = R.string.select_source_placeholder_label),
                    isError = !sourceValid,
                    errorHint = stringResource(id = sourceErrorStringRes),
                    leadingIcon = selectedSource.icon,
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.SOURCE
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
        setSelectedSource = {},
        setTransactionType = {},
        upsertTransaction = { _, _ -> },
        deleteTransaction = {},
        filterTransactionTitles = { emptyList() },
        events = emptyList<Event>().asFlow(),
        state = UpsertTransactionScreenState()
    )
}

fun NavGraphBuilder.upsertTransactionScreen(navController: NavController) {
    animatedComposable(
        Routes.UpsertTransaction.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = ""
                type = NavType.StringType
            },
            navArgument("templateId") {
                defaultValue = ""
                type = NavType.StringType
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
            setSelectedCategory = viewModel::setSelectedCategory,
            setSelectedCounterParty = viewModel::setSelectedCounterParty,
            setSelectedMethod = viewModel::setSelectedMethod,
            setSelectedSource = viewModel::setSelectedSource,
            setTransactionType = viewModel::setTransactionType,
            upsertTransaction = viewModel::upsertTransaction,
            deleteTransaction = viewModel::deleteTransaction,
            filterTransactionTitles = viewModel::filterTransactionTitles,
            events = viewModel.event,
            state = state
        )
    }
}