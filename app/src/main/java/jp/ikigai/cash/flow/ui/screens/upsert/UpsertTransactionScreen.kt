package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionScreenState
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.IconToggleRow
import jp.ikigai.cash.flow.ui.components.buttons.ToggleButton
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.sheets.AutoCompleteTextFieldBottomSheet
import jp.ikigai.cash.flow.ui.components.sheets.CommonSelectionSheet
import jp.ikigai.cash.flow.ui.components.sheets.ConfirmDeleteSheet
import jp.ikigai.cash.flow.ui.components.sheets.DatePickerBottomSheet
import jp.ikigai.cash.flow.ui.components.sheets.SelectItemSheet
import jp.ikigai.cash.flow.ui.components.sheets.TimePickerBottomSheet
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertTransactionScreenViewModel
import jp.ikigai.cash.flow.utils.TextFieldValueSaver
import jp.ikigai.cash.flow.utils.animatedComposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

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

    var showConfirmDeleteSheet by remember {
        mutableStateOf(false)
    }

    if (showConfirmDeleteSheet) {
        ConfirmDeleteSheet(
            message = "Are you sure you want to delete this transaction?",
            dismiss = {
                scope.launch {
                    sheetState.hide()
                    showConfirmDeleteSheet = false
                }
            },
            delete = deleteTransaction,
            sheetState = sheetState
        )
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val enabled by remember(key1 = state.enabled) {
        mutableStateOf(state.enabled)
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
                Event.DeleteSuccess -> "Deleted successfully"
                Event.InternalError -> "Internal error"
                Event.NotEnoughBalance -> "Insufficient balance to save this transaction"
                else -> ""
            }
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

    val titles by remember(key1 = state.titles) {
        mutableStateOf(state.titles)
    }

    val isDebit by remember(key1 = state.type) {
        mutableStateOf(state.type == TransactionType.DEBIT)
    }

    val transactionUuid by remember(key1 = state.transaction.uuid) {
        mutableStateOf(state.transaction.uuid)
    }

    var titleFieldValue by rememberSaveable(state.transaction, saver = TextFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(state.transaction.title)
        )
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

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    when (sheetType) {
        SheetType.DATE -> {
            DatePickerBottomSheet(
                date = dateTime,
                selectableDates = selectableDates,
                setDate = setDate,
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                sheetState = sheetState
            )
        }

        SheetType.TIME -> {
            TimePickerBottomSheet(
                time = dateTime,
                updateTime = setTime,
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                sheetState = sheetState
            )
        }

        SheetType.CATEGORY -> {
            CommonSelectionSheet(
                index = categories.indexOfFirst { it.uuid == selectedCategory.uuid },
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                maxHeight = sheetMaxHeight,
                sheetState = sheetState
            ) {
                items(
                    items = categories,
                    key = { category -> category.uuid }
                ) { category ->
                    IconToggleRow(
                        label = category.name,
                        icon = category.icon,
                        selected = category.uuid == selectedCategory.uuid,
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                sheetType = SheetType.NONE
                                setSelectedCategory(category)
                            }
                        }
                    )
                }
            }
        }

        SheetType.COUNTERPARTY -> {
            CommonSelectionSheet(
                index = counterParties.indexOfFirst { it.uuid == selectedCounterParty.uuid }
                    .coerceAtLeast(0),
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                maxHeight = sheetMaxHeight,
                sheetState = sheetState
            ) {
                items(
                    items = counterParties,
                    key = { counterParty -> counterParty.uuid }
                ) { counterParty ->
                    IconToggleRow(
                        label = counterParty.name,
                        icon = counterParty.icon,
                        selected = counterParty.uuid == selectedCounterParty.uuid,
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                sheetType = SheetType.NONE
                                if (counterParty.uuid == selectedCounterParty.uuid) {
                                    setSelectedCounterParty(CounterParty())
                                } else {
                                    setSelectedCounterParty(counterParty)
                                }
                            }
                        }
                    )

                }
            }
        }

        SheetType.METHOD -> {
            CommonSelectionSheet(
                index = methods.indexOfFirst { it.uuid == selectedMethod.uuid },
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                maxHeight = sheetMaxHeight,
                sheetState = sheetState
            ) {
                items(
                    items = methods,
                    key = { method -> method.uuid }
                ) { method ->
                    IconToggleRow(
                        label = method.name,
                        icon = method.icon,
                        selected = method.uuid == selectedMethod.uuid,
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                sheetType = SheetType.NONE
                                setSelectedMethod(method)
                            }
                        }
                    )
                }
            }
        }

        SheetType.SOURCE -> {
            CommonSelectionSheet(
                index = sources.indexOfFirst { it.uuid == selectedSource.uuid },
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                maxHeight = sheetMaxHeight,
                sheetState = sheetState
            ) {
                items(
                    items = sources,
                    key = { source -> source.uuid }
                ) { source ->
                    IconToggleRow(
                        label = "${source.name} (${source.balance} ${source.currency})",
                        icon = source.icon,
                        selected = source.uuid == selectedSource.uuid,
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                sheetType = SheetType.NONE
                                setSelectedSource(source)
                            }
                        }
                    )
                }
            }
        }

        SheetType.TYPE -> {
            CommonSelectionSheet(
                index = if (isDebit) 0 else 1,
                dismiss = {
                    scope.launch {
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                maxHeight = sheetMaxHeight,
                sheetState = sheetState
            ) {
                item(
                    key = "debit",
                    contentType = "row"
                ) {
                    IconToggleRow(
                        label = "Debit",
                        icon = TablerIcons.ArrowUpCircle,
                        selected = isDebit,
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                sheetType = SheetType.NONE
                                setTransactionType(TransactionType.DEBIT)
                            }
                        }
                    )
                }
                item(
                    key = "credit",
                    contentType = "row"
                ) {
                    IconToggleRow(
                        label = "Credit",
                        icon = TablerIcons.ArrowDownCircle,
                        selected = !isDebit,
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                sheetType = SheetType.NONE
                                setTransactionType(TransactionType.CREDIT)
                            }
                        }
                    )
                }
            }
        }

        SheetType.ITEMS -> {
            SelectItemSheet(
                selectedTransactionItem = selectedTransactionItem,
                items = items,
                addItem = addItem,
                dismiss = {
                    scope.launch {
                        keyboardController?.hide()
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                maxHeight = 300.0,
                sheetState = sheetState
            )
        }

        SheetType.AUTO_COMPLETE -> {
            AutoCompleteTextFieldBottomSheet(
                fieldValue = titleFieldValue,
                setFieldValue = {
                    scope.launch {
                        keyboardController?.hide()
                        sheetState.hide()
                        sheetType = SheetType.NONE
                        titleFieldValue = it
                    }
                },
                enabled = enabled,
                icon = TablerIcons.Typography,
                iconDescription = "title icon",
                iconTint = MaterialTheme.colorScheme.onBackground,
                label = "Title",
                dismiss = {
                    scope.launch {
                        keyboardController?.hide()
                        sheetState.hide()
                        sheetType = SheetType.NONE
                    }
                },
                sheetState = sheetState
            ) { setTitleFromAutoComplete ->
                items(
                    items = titles,
                    key = { title -> title }
                ) { title ->
                    ToggleButton(
                        label = title,
                        selected = false,
                        toggle = {
                            setTitleFromAutoComplete(title)
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }

        else -> {}
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
                    if (transactionUuid.isBlank()) {
                        Text(text = "Create transaction")
                    } else {
                        Text(text = "Update transaction")
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
                        upsertTransaction(titleFieldValue.text, descriptionFieldValue.text)
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
                    { showConfirmDeleteSheet = true }
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
                RoundedCornerOutlinedTextField(
                    expanded = sheetType == SheetType.AUTO_COMPLETE,
                    enabled = enabled,
                    value = titleFieldValue,
                    label = "Title",
                    icon = TablerIcons.Typography,
                    iconDescription = "title icon",
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.AUTO_COMPLETE
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
                    label = "Description",
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
                    label = "Amount",
                    icon = TablerIcons.CashBanknote,
                    iconDescription = "amount icon",
                    isError = !amountValid,
                    errorHint = "Enter a number > 0",
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            }
            item(
                key = "date",
                contentType = "dropDown"
            ) {
                RoundedCornerOutlinedTextField(
                    expanded = sheetType == SheetType.DATE,
                    enabled = enabled,
                    value = date,
                    label = "Date",
                    icon = TablerIcons.CalendarEvent,
                    iconDescription = "date icon",
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.DATE
                    }
                )
            }
            item(
                key = "time",
                contentType = "dropDown"
            ) {
                RoundedCornerOutlinedTextField(
                    expanded = sheetType == SheetType.TIME,
                    enabled = enabled,
                    value = time,
                    label = "Time",
                    icon = TablerIcons.Alarm,
                    iconDescription = "time icon",
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.TIME
                    }
                )
            }
            item(
                key = "category",
                contentType = "dropDown"
            ) {
                RoundedCornerOutlinedTextField(
                    expanded = sheetType == SheetType.CATEGORY,
                    enabled = enabled,
                    value = selectedCategory.name,
                    label = "Category",
                    icon = selectedCategory.icon,
                    iconDescription = "category icon",
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.CATEGORY
                    }
                )
            }
            item(
                key = "counterParty",
                contentType = "dropDown"
            ) {
                RoundedCornerOutlinedTextField(
                    expanded = sheetType == SheetType.COUNTERPARTY,
                    enabled = enabled,
                    value = selectedCounterParty.name,
                    label = "Counter party",
                    icon = selectedCounterParty.icon,
                    iconDescription = "counter party icon",
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.COUNTERPARTY
                    }
                )
            }
            item(
                key = "method",
                contentType = "dropDown"
            ) {
                RoundedCornerOutlinedTextField(
                    expanded = sheetType == SheetType.METHOD,
                    enabled = enabled,
                    value = selectedMethod.name,
                    label = "Method",
                    icon = selectedMethod.icon,
                    iconDescription = "method icon",
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.METHOD
                    }
                )
            }
            item(
                key = "source",
                contentType = "dropDown"
            ) {
                RoundedCornerOutlinedTextField(
                    expanded = sheetType == SheetType.SOURCE,
                    enabled = enabled,
                    value = selectedSource.name,
                    label = "Source",
                    icon = selectedSource.icon,
                    iconDescription = "source icon",
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.SOURCE
                    }
                )
            }
            item(
                key = "transactionType",
                contentType = "dropDown"
            ) {
                RoundedCornerOutlinedTextField(
                    expanded = sheetType == SheetType.TYPE,
                    enabled = enabled,
                    value = if (isDebit) "Debit" else "Credit",
                    label = "Transaction type",
                    icon = if (isDebit) TablerIcons.ArrowUpCircle else TablerIcons.ArrowDownCircle,
                    iconDescription = "type icon",
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.TYPE
                    }
                )
            }
            if (itemHeaderVisible) {
                item(
                    key = "itemHeader",
                    contentType = "header"
                ) {
                    Text(
                        text = "Items",
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    RoundedCornerOutlinedTextField(
                        expanded = false,
                        enabled = enabled,
                        value = "${transactionItem.item!!.name}\nQuantity: ${transactionItem.quantity}, Price: ${transactionItem.price}, Unit: ${transactionItem.unit.code}",
                        label = "Item #$index",
                        icon = TablerIcons.Stack,
                        iconDescription = "item icon",
                        onClick = {
                            resetOneHandMode()
                            selectedTransactionItem = transactionItem
                            sheetType = SheetType.ITEMS
                        }
                    )
                    IconButton(
                        onClick = {
                            if (enabled) {
                                resetOneHandMode()
                                removeItem(transactionItem)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "remove item",
                        )
                    }
                }
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
                        label = "Tax",
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
                            sheetType = SheetType.ITEMS
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "Add item",
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