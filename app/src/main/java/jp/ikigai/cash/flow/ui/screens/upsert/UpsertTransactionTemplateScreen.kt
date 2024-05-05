package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import compose.icons.tablericons.ArrowDownCircle
import compose.icons.tablericons.ArrowUpCircle
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.FileText
import compose.icons.tablericons.LetterCase
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
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.buttons.IconToggleRow
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.sheets.CommonSelectionSheet
import jp.ikigai.cash.flow.ui.components.sheets.ConfirmDeleteSheet
import jp.ikigai.cash.flow.ui.components.sheets.SelectItemSheet
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionTemplateScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertTransactionTemplateScreenViewModel
import jp.ikigai.cash.flow.utils.TextFieldValueSaver
import jp.ikigai.cash.flow.utils.animatedComposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpsertTransactionTemplateScreen(
    navigateBack: () -> Unit,
    addItem: (TransactionItem) -> Unit,
    removeItem: (TransactionItem) -> Unit,
    setAmount: (String) -> Unit,
    setTaxAmount: (String) -> Unit,
    setSelectedCategory: (Category) -> Unit,
    setSelectedCounterParty: (CounterParty) -> Unit,
    setSelectedMethod: (Method) -> Unit,
    setSelectedSource: (Source) -> Unit,
    setTransactionType: (TransactionType) -> Unit,
    upsertTransactionTemplate: (String, String, String) -> Unit,
    deleteTransactionTemplate: () -> Unit,
    events: Flow<Event>,
    state: UpsertTransactionTemplateScreenState
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

    var showConfirmDeleteSheet by remember {
        mutableStateOf(false)
    }

    if (showConfirmDeleteSheet) {
        ConfirmDeleteSheet(
            message = "Are you sure you want to delete this template?",
            dismiss = {
                scope.launch {
                    sheetState.hide()
                    showConfirmDeleteSheet = false
                }
            },
            delete = deleteTransactionTemplate,
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
                Event.MinimumTwoFieldsRequired -> "Minimum two fields are required"
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

    val counterParties by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties)
    }

    val items by remember(key1 = state.items) {
        mutableStateOf(state.items)
    }

    val methods by remember(key1 = state.methods) {
        mutableStateOf(state.methods)
    }

    val sources by remember(key1 = state.sources) {
        mutableStateOf(state.sources)
    }

    val transactionTemplateUuid by remember(key1 = state.transactionTemplate.uuid) {
        mutableStateOf(state.transactionTemplate.uuid)
    }

    var nameFieldValue by rememberSaveable(
        state.transactionTemplate,
        saver = TextFieldValueSaver
    ) {
        mutableStateOf(
            TextFieldValue(state.transactionTemplate.name)
        )
    }

    var nameValid by remember {
        mutableStateOf(true)
    }

    var titleFieldValue by rememberSaveable(
        state.transactionTemplate,
        saver = TextFieldValueSaver
    ) {
        mutableStateOf(
            TextFieldValue(state.transactionTemplate.title)
        )
    }

    var descriptionFieldValue by rememberSaveable(
        state.transactionTemplate,
        saver = TextFieldValueSaver
    ) {
        mutableStateOf(
            TextFieldValue(state.transactionTemplate.description)
        )
    }

    val amount by remember(key1 = state.displayAmount) {
        mutableStateOf(state.displayAmount)
    }

    val taxAmount by remember(key1 = state.displayTaxAmount) {
        mutableStateOf(state.displayTaxAmount)
    }

    val selectedCategory by remember(key1 = state.selectedCategory) {
        mutableStateOf(state.selectedCategory)
    }

    val selectedCounterParty by remember(key1 = state.selectedCounterParty) {
        mutableStateOf(state.selectedCounterParty)
    }

    val selectedMethod by remember(key1 = state.selectedMethod) {
        mutableStateOf(state.selectedMethod)
    }

    val selectedSource by remember(key1 = state.selectedSource) {
        mutableStateOf(state.selectedSource)
    }

    var selectedTransactionItem by remember {
        mutableStateOf(TransactionItem())
    }

    val isDebit by remember(key1 = state.type) {
        mutableStateOf(state.type == TransactionType.DEBIT)
    }

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    when (sheetType) {
        SheetType.CATEGORY -> {
            CommonSelectionSheet(
                index = categories.indexOfFirst { it.uuid == selectedCategory.uuid }
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
                index = methods.indexOfFirst { it.uuid == selectedMethod.uuid }.coerceAtLeast(0),
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
                                if (method.uuid == selectedMethod.uuid) {
                                    setSelectedMethod(Method())
                                } else {
                                    setSelectedMethod(method)
                                }
                            }
                        }
                    )
                }
            }
        }

        SheetType.SOURCE -> {
            CommonSelectionSheet(
                index = sources.indexOfFirst { it.uuid == selectedSource.uuid }.coerceAtLeast(0),
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
                                if (source.uuid == selectedSource.uuid) {
                                    setSelectedSource(Source())
                                } else {
                                    setSelectedSource(source)
                                }
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
                templateMode = true,
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

        else -> {}
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = showToastBar,
        toastBarText = toastBarString,
        onDismissToastBar = {},
        showEmptyPlaceholder = false,
        emptyPlaceholderText = "",
        topBar = {
            TopAppBar(
                title = {
                    if (transactionTemplateUuid.isBlank()) {
                        Text(text = "Create template")
                    } else {
                        Text(text = "Update template")
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
                        if (nameFieldValue.text.isNotBlank()) {
                            upsertTransactionTemplate(
                                nameFieldValue.text,
                                titleFieldValue.text,
                                descriptionFieldValue.text
                            )
                        } else {
                            nameValid = false
                        }
                    }
                },
                extraButtonIcon = if (transactionTemplateUuid.isNotBlank()) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = Icons.Outlined.Delete.name,
                        )
                    }
                } else null,
                extraButtonAction = if (transactionTemplateUuid.isNotBlank() && enabled) {
                    {
                        showConfirmDeleteSheet = true
                    }
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
                key = "name",
                contentType = "dropDown"
            ) {
                RoundedCornerOutlinedTextField(
                    value = nameFieldValue,
                    onValueChange = {
                        nameFieldValue = it
                        nameValid = it.text.isNotBlank()
                    },
                    enabled = enabled,
                    label = "Template name",
                    isError = !nameValid,
                    errorHint = "Name cannot be empty",
                    icon = TablerIcons.Typography,
                    iconDescription = "name icon",
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            }
            item(
                key = "title",
                contentType = "dropDown"
            ) {
                RoundedCornerOutlinedTextField(
                    value = titleFieldValue,
                    onValueChange = { titleFieldValue = it },
                    enabled = enabled,
                    label = "Transaction title",
                    icon = TablerIcons.LetterCase,
                    iconDescription = "title icon",
                    onDone = {
                        keyboardController?.hide()
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
                    onDone = {
                        keyboardController?.hide()
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
                    label = "Category",
                    placeHolder = "Select a category",
                    leadingIcon = {
                        Icon(
                            imageVector = selectedCategory.icon,
                            contentDescription = "category icon",
                        )
                    },
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedCategory(Category())
                    },
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
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedCounterParty.name,
                    label = "Counter party",
                    placeHolder = "Select a counter party",
                    leadingIcon = {
                        Icon(
                            imageVector = selectedCounterParty.icon,
                            contentDescription = "counter party icon",
                        )
                    },
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedCounterParty(CounterParty())
                    },
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
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedMethod.name,
                    label = "Method",
                    placeHolder = "Select a method",
                    leadingIcon = {
                        Icon(
                            imageVector = selectedMethod.icon,
                            contentDescription = "method icon",
                        )
                    },
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedMethod(Method())
                    },
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
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedSource.name,
                    label = "Source",
                    placeHolder = "Select a source",
                    leadingIcon = {
                        Icon(
                            imageVector = selectedSource.icon,
                            contentDescription = "source icon",
                        )
                    },
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedSource(Source())
                    },
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
                CustomOutlinedButton(
                    enabled = enabled,
                    value = if (isDebit) "Debit" else "Credit",
                    label = "Transaction type",
                    placeHolder = "Select a transaction type",
                    leadingIcon = {
                        Icon(
                            imageVector = if (isDebit) TablerIcons.ArrowUpCircle else TablerIcons.ArrowDownCircle,
                            contentDescription = "transaction type icon",
                        )
                    },
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
                            .animateItemPlacement()
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
                    value = "${transactionItem.item!!.name}\nQuantity: ${transactionItem.quantity}, Price: ${transactionItem.price}, Unit: ${transactionItem.unit.code}",
                    label = "Item #${index + 1}",
                    placeHolder = "",
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
                        sheetType = SheetType.ITEMS
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
fun UpsertTransactionTemplateScreenPreview() {
    UpsertTransactionTemplateScreen(
        navigateBack = {},
        addItem = {},
        removeItem = {},
        setAmount = {},
        setTaxAmount = {},
        setSelectedCategory = {},
        setSelectedCounterParty = {},
        setSelectedMethod = {},
        setSelectedSource = {},
        setTransactionType = {},
        upsertTransactionTemplate = { _, _, _ -> },
        deleteTransactionTemplate = {},
        events = emptyList<Event>().asFlow(),
        state = UpsertTransactionTemplateScreenState()
    )
}

fun NavGraphBuilder.upsertTransactionTemplateScreen(navController: NavController) {
    animatedComposable(
        Routes.UpsertTemplate.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = ""
                type = NavType.StringType
            }
        )
    ) {
        val viewModel: UpsertTransactionTemplateScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertTransactionTemplateScreen(
            navigateBack = {
                navController.popBackStack()
            },
            addItem = viewModel::addItem,
            removeItem = viewModel::removeItem,
            setAmount = viewModel::setAmount,
            setTaxAmount = viewModel::setTaxAmount,
            setSelectedCategory = viewModel::setSelectedCategory,
            setSelectedCounterParty = viewModel::setSelectedCounterParty,
            setSelectedMethod = viewModel::setSelectedMethod,
            setSelectedSource = viewModel::setSelectedSource,
            setTransactionType = viewModel::setTransactionType,
            upsertTransactionTemplate = viewModel::upsertTransactionTemplate,
            deleteTransactionTemplate = viewModel::deleteTransactionTemplate,
            events = viewModel.event,
            state = state
        )
    }
}