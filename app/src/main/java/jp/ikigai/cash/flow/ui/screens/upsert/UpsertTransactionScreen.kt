package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import compose.icons.tablericons.ArrowDownCircle
import compose.icons.tablericons.ArrowUpCircle
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.FileText
import compose.icons.tablericons.Stack
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.popups.ConfirmDeletePopup
import jp.ikigai.cash.flow.ui.components.popups.DatePickerPopup
import jp.ikigai.cash.flow.ui.components.popups.SearchTextFieldPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCategoryPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCounterPartyPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectItemPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectMethodPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectSourcePopup
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertTransactionScreen(
    navigateBack: () -> Unit,
    setAmount: (String) -> Unit,
    setTaxAmount: (String) -> Unit,
    setDate: (ZonedDateTime) -> Unit,
    setTime: (ZonedDateTime) -> Unit,
    addItem: (TransactionItem) -> Unit,
    removeItem: (TransactionItem) -> Unit,
    setSelectedCategory: (Category) -> Unit,
    setSelectedCounterParty: (CounterParty) -> Unit,
    setSelectedMethod: (Method) -> Unit,
    setSelectedSource: (Source) -> Unit,
    setTransactionType: (TransactionType) -> Unit,
    upsertTransaction: (String, String) -> Unit,
    deleteTransaction: () -> Unit,
    events: Flow<Event>,
    state: UpsertTransactionScreenState
) {
    val keyboardController = LocalSoftwareKeyboardController.current

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

    val transactionItems by remember(key1 = state.transactionItems) {
        mutableStateOf(state.transactionItems.values.toList())
    }

    val itemHeaderVisible by remember(key1 = state.transactionItems) {
        derivedStateOf { state.transactionItems.isNotEmpty() }
    }

    val categories by remember(key1 = state.categories) {
        mutableStateOf(state.categories)
    }

    val selectedCategory by remember(key1 = state.selectedCategory) {
        mutableStateOf(state.selectedCategory)
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

    val sources by remember(key1 = state.sources) {
        mutableStateOf(state.sources)
    }

    val selectedSource by remember(key1 = state.selectedSource) {
        mutableStateOf(state.selectedSource)
    }

    val items by remember(key1 = state.items) {
        mutableStateOf(state.items)
    }

    var selectedTransactionItem by remember {
        mutableStateOf(TransactionItem())
    }

    val transactionTitles by remember(key1 = state.transactionTitles) {
        mutableStateOf(state.transactionTitles)
    }

    val transactionType by remember(key1 = state.type) {
        mutableStateOf(state.type)
    }

    val transactionUuid by remember(key1 = state.transaction) {
        mutableStateOf(state.transaction.uuid)
    }

    var title by rememberSaveable(state.transaction) {
        mutableStateOf(state.transaction.title)
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

    val taxAmount by remember(key1 = state.displayTaxAmount) {
        mutableStateOf(state.displayTaxAmount)
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

    OneHandModeScaffold(
        loading = loading,
        showToastBar = showToastBar,
        toastBarText = currentEvent?.let {
            stringResource(id = it.message)
        } ?: "",
        onDismissToastBar = {
            showToastBar = false
            if (currentEvent == Event.SaveSuccess) {
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
                        index = categories.indexOfFirst { it.uuid == selectedCategory.uuid },
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
                        index = methods.indexOfFirst { it.uuid == selectedMethod.uuid },
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
                        index = sources.indexOfFirst { it.uuid == selectedSource.uuid },
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

                PopupType.ITEMS -> {
                    SelectItemPopup(
                        index = items.indexOfFirst { it.uuid == selectedTransactionItem.item?.uuid }
                            .coerceAtLeast(0),
                        selectedTransactionItem = selectedTransactionItem,
                        items = items,
                        addItem = addItem,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.SEARCH_TEXT_FIELD -> {
                    SearchTextFieldPopup(
                        value = title,
                        setValue = { value ->
                            title = value
                        },
                        enabled = enabled,
                        icon = TablerIcons.Typography,
                        iconDescription = "title icon",
                        label = stringResource(id = R.string.title_field_label),
                        placeholder = stringResource(id = R.string.title_placeholder_label),
                        titles = transactionTitles,
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(
                key = "one-hand-mode-expand-row",
                contentType = "row"
            ) {
                OneHandModeSpacer(oneHandModeBoxHeight = oneHandModeBoxHeight)
            }
            item(
                key = "title",
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = title,
                    label = stringResource(id = R.string.title_field_label),
                    placeHolder = stringResource(id = R.string.title_placeholder_label),
                    leadingIcon = {
                        Icon(
                            imageVector = TablerIcons.Typography,
                            contentDescription = "title icon",
                        )
                    },
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        title = ""
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.SEARCH_TEXT_FIELD
                    }
                )
            }
            item(
                key = "description",
                contentType = "textField"
            ) {
                RoundedCornerOutlinedTextField(
                    value = descriptionFieldValue,
                    onValueChange = { descriptionFieldValue = it },
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
                    }
                )
            }
            item(
                key = "amount",
                contentType = "textField"
            ) {
                RoundedCornerOutlinedTextField(
                    value = amount,
                    onValueChange = setAmount,
                    enabled = enabled && !itemHeaderVisible,
                    label = stringResource(id = R.string.amount_label),
                    placeHolder = stringResource(id = R.string.transaction_amount_placeholder_label),
                    icon = TablerIcons.CashBanknote,
                    iconDescription = "amount icon",
                    isError = !amountValid,
                    errorHint = stringResource(id = R.string.invalid_amount_error_label),
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            }
            item(
                key = "date",
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = date,
                    label = stringResource(id = R.string.date_field_label),
                    placeHolder = "",
                    leadingIcon = {
                        Icon(
                            imageVector = TablerIcons.CalendarEvent,
                            contentDescription = "date icon",
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.DATE
                    }
                )
            }
            item(
                key = "time",
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = time,
                    label = stringResource(id = R.string.time_field_label),
                    placeHolder = "",
                    leadingIcon = {
                        Icon(
                            imageVector = TablerIcons.Alarm,
                            contentDescription = "time icon",
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.TIME
                    }
                )
            }
            item(
                key = "category",
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedCategory.name,
                    label = stringResource(id = R.string.category_field_label),
                    placeHolder = "",
                    leadingIcon = {
                        Icon(
                            imageVector = selectedCategory.icon,
                            contentDescription = "category icon",
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.CATEGORY
                    }
                )
            }
            item(
                key = "counterParty",
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedCounterParty.name,
                    label = stringResource(id = R.string.counter_party_field_label),
                    placeHolder = stringResource(id = R.string.counter_party_placeholder_label),
                    leadingIcon = {
                        Icon(
                            imageVector = selectedCounterParty.icon,
                            contentDescription = "counter party icon",
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.COUNTERPARTY
                    }
                )
            }
            item(
                key = "method",
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedMethod.name,
                    label = stringResource(id = R.string.method_field_label),
                    placeHolder = "",
                    leadingIcon = {
                        Icon(
                            imageVector = selectedMethod.icon,
                            contentDescription = "method icon",
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.METHOD
                    }
                )
            }
            item(
                key = "source",
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedSource.name,
                    label = stringResource(id = R.string.source_field_label),
                    placeHolder = "",
                    leadingIcon = {
                        Icon(
                            imageVector = selectedSource.icon,
                            contentDescription = "source icon",
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.SOURCE
                    }
                )
            }
            item(
                key = "transactionType",
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = stringResource(id = transactionType.label),
                    label = stringResource(id = R.string.transaction_type_field_label),
                    placeHolder = "",
                    leadingIcon = {
                        Icon(
                            imageVector = if (transactionType == TransactionType.DEBIT) TablerIcons.ArrowUpCircle else TablerIcons.ArrowDownCircle,
                            contentDescription = "type icon",
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.TYPE
                    }
                )
            }
            if (itemHeaderVisible) {
                item(
                    key = "itemHeader",
                    contentType = "header"
                ) {
                    Text(
                        text = stringResource(id = R.string.items_label),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            itemsIndexed(
                items = transactionItems,
                key = { _, transactionItem -> transactionItem.item!!.uuid }
            ) { index, transactionItem ->
                CustomOutlinedButton(
                    enabled = enabled,
                    value = stringResource(
                        id = R.string.item_quantity_price_unit_label,
                        transactionItem.item!!.name,
                        transactionItem.quantity.toString(),
                        transactionItem.price.toString(),
                        stringResource(id = transactionItem.unit.code)
                    ),
                    label = stringResource(id = R.string.item_field_label, index + 1),
                    placeHolder = "",
                    isError = transactionItem.price == 0.0,
                    errorHint = stringResource(id = R.string.invalid_price_error_label),
                    leadingIcon = {
                        Icon(
                            imageVector = TablerIcons.Stack,
                            contentDescription = "item icon",
                        )
                    },
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        resetOneHandMode()
                        removeItem(transactionItem)
                    },
                    onClick = {
                        resetOneHandMode()
                        selectedTransactionItem = transactionItem
                        popupType = PopupType.ITEMS
                    }
                )
            }
            if (itemHeaderVisible) {
                item(
                    key = "taxAmount",
                    contentType = "textField"
                ) {
                    RoundedCornerOutlinedTextField(
                        value = taxAmount,
                        onValueChange = setTaxAmount,
                        enabled = enabled,
                        label = stringResource(id = R.string.tax_field_label),
                        placeHolder = stringResource(id = R.string.tax_amount_placeholder_label),
                        icon = TablerIcons.CashBanknote,
                        iconDescription = "tax amount icon",
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                }
            }
            item(
                key = "addItemButton",
                contentType = "button"
            ) {
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {
                        if (enabled) {
                            popupType = PopupType.ITEMS
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = stringResource(id = R.string.add_item_button_label),
                        onValueChange = {},
                        enabled = enabled,
                        readOnly = true,
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun UpsertTransactionScreenPreview() {
    UpsertTransactionScreen(
        navigateBack = {},
        setAmount = {},
        setTaxAmount = {},
        setDate = {},
        setTime = {},
        addItem = {},
        removeItem = {},
        setSelectedCategory = {},
        setSelectedCounterParty = {},
        setSelectedMethod = {},
        setSelectedSource = {},
        setTransactionType = {},
        upsertTransaction = { _, _ -> },
        deleteTransaction = {},
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
            setAmount = viewModel::setAmount,
            setTaxAmount = viewModel::setTaxAmount,
            setDate = viewModel::setDate,
            setTime = viewModel::setTime,
            addItem = viewModel::addItem,
            removeItem = viewModel::removeItem,
            setSelectedCategory = viewModel::setSelectedCategory,
            setSelectedCounterParty = viewModel::setSelectedCounterParty,
            setSelectedMethod = viewModel::setSelectedMethod,
            setSelectedSource = viewModel::setSelectedSource,
            setTransactionType = viewModel::setTransactionType,
            upsertTransaction = viewModel::upsertTransaction,
            deleteTransaction = viewModel::deleteTransaction,
            events = viewModel.event,
            state = state
        )
    }
}