package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import compose.icons.TablerIcons
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.FileText
import compose.icons.tablericons.LetterCase
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.enums.ItemUnit
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.cards.UpsertTemplateItemCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.popups.AddItemsPopup
import jp.ikigai.cash.flow.ui.components.popups.ChangeItemPopup
import jp.ikigai.cash.flow.ui.components.popups.ConfirmDeletePopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCategoryPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectCounterPartyPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectMethodPopup
import jp.ikigai.cash.flow.ui.components.popups.SelectSourcePopup
import jp.ikigai.cash.flow.ui.components.popups.SelectTransactionTypePopup
import jp.ikigai.cash.flow.ui.components.popups.SelectUnitPopup
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionTemplateScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertTransactionTemplateScreenViewModel
import jp.ikigai.cash.flow.utils.TextFieldValueSaver
import jp.ikigai.cash.flow.utils.animatedComposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertTransactionTemplateScreen(
    navigateBack: () -> Unit,
    addItems: (List<Item>) -> Unit,
    changeItem: (Item, Int) -> Unit,
    getChangeItemFilteredList: (String) -> List<Item>,
    updateTransactionItemUnit: (ItemUnit, Int) -> Unit,
    updateTemplateItemPrice: (String, Int) -> Unit,
    updateTemplateItemQuantity: (String, Int) -> Unit,
    removeItem: (Int) -> Unit,
    setName: (String) -> Unit,
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

    val counterParties by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties)
    }

    val addItemsFilteredList by remember(key1 = state.addItemsFilteredList) {
        mutableStateOf(state.addItemsFilteredList)
    }

    val templateItems by remember(key1 = state.templateItems) {
        mutableStateOf(state.templateItems)
    }

    val methods by remember(key1 = state.methods) {
        mutableStateOf(state.methods)
    }

    val sources by remember(key1 = state.sources) {
        mutableStateOf(state.sources)
    }

    val transactionTemplateUuid by remember(key1 = state.transactionTemplate) {
        mutableStateOf(state.transactionTemplate.uuid)
    }

    val name by remember(key1 = state.name) {
        mutableStateOf(state.name)
    }

    val nameValid by remember(key1 = state.nameValid) {
        mutableStateOf(state.nameValid)
    }

    val nameErrorStringRes by remember(key1 = state.nameErrorStringRes) {
        mutableIntStateOf(state.nameErrorStringRes)
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

    val amountEnabled by remember(key1 = enabled, key2 = state.type, key3 = templateItems) {
        mutableStateOf(
            enabled && (state.type == TransactionType.CREDIT || templateItems.isEmpty())
        )
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

    var selectedTemplateItemIndex by remember {
        mutableIntStateOf(-1)
    }

    val itemHeaderVisible by remember(key1 = state.templateItems) {
        mutableStateOf(state.templateItems.isNotEmpty())
    }

    val transactionType by remember(key1 = state.type) {
        mutableStateOf(state.type)
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
            if (currentEvent == Event.SaveSuccess || currentEvent == Event.DeleteSuccess) {
                navigateBack()
            }
        },
        showBottomPopup = popupType != PopupType.NONE,
        bottomPopupContent = { hidePopup ->
            when (popupType) {
                PopupType.CATEGORY -> {
                    SelectCategoryPopup(
                        index = categories.indexOfFirst { it.uuid == selectedCategory.uuid }
                            .coerceAtLeast(0),
                        selectedCategoryUUID = selectedCategory.uuid,
                        setSelectedCategory = { category ->
                            if (category.uuid == selectedCategory.uuid) {
                                setSelectedCategory(Category())
                            } else {
                                setSelectedCategory(category)
                            }
                        },
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
                        setSelectedMethod = { method ->
                            if (method.uuid == selectedMethod.uuid) {
                                setSelectedMethod(Method())
                            } else {
                                setSelectedMethod(method)
                            }
                        },
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
                        setSelectedSource = { source ->
                            if (source.uuid == selectedSource.uuid) {
                                setSelectedSource(Source())
                            } else {
                                setSelectedSource(source)
                            }
                        },
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
                        setSelectedTransactionType = { selectedTransactionType ->
                            setTransactionType(selectedTransactionType)
                        },
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.ADD_ITEMS -> {
                    AddItemsPopup(
                        items = addItemsFilteredList,
                        addItems = addItems,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.CHANGE_ITEM -> {
                    ChangeItemPopup(
                        selectedItemUUID = templateItems[selectedTemplateItemIndex].item.uuid,
                        setSelectedItem = {
                            changeItem(it, selectedTemplateItemIndex)
                        },
                        getItems = getChangeItemFilteredList,
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.ITEM_UNIT -> {
                    SelectUnitPopup(
                        selectedUnit = templateItems[selectedTemplateItemIndex].unit,
                        updateUnit = {
                            updateTransactionItemUnit(it, selectedTemplateItemIndex)
                        },
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.CONFIRM_DELETE -> {
                    ConfirmDeletePopup(
                        message = stringResource(id = R.string.delete_template_confirmation_label),
                        delete = deleteTransactionTemplate,
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
                    if (transactionTemplateUuid.isBlank()) {
                        Text(text = stringResource(id = R.string.create_template_label))
                    } else {
                        Text(text = stringResource(id = R.string.update_template_label))
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
                        upsertTransactionTemplate(
                            name.trim(),
                            titleFieldValue.text,
                            descriptionFieldValue.text
                        )
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
                        popupType = PopupType.CONFIRM_DELETE
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
                contentType = "type-enabled"
            ) {
                RoundedCornerOutlinedTextField(
                    value = name,
                    onValueChange = setName,
                    enabled = enabled,
                    label = stringResource(id = R.string.template_name_field_label),
                    placeHolder = stringResource(id = R.string.template_name_placeholder_label),
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
                    },
                    boxModifier = Modifier.animateItem()
                )
            }
            item(
                key = "title",
                contentType = "type-enabled"
            ) {
                RoundedCornerOutlinedTextField(
                    value = titleFieldValue,
                    onValueChange = { titleFieldValue = it },
                    enabled = enabled,
                    label = stringResource(id = R.string.title_field_label),
                    placeHolder = stringResource(id = R.string.title_placeholder_label),
                    icon = TablerIcons.LetterCase,
                    iconDescription = "title icon",
                    onDone = {
                        keyboardController?.hide()
                    },
                    boxModifier = Modifier.animateItem()
                )
            }
            item(
                key = "description",
                contentType = "type-enabled"
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
                    enabled = amountEnabled,
                    label = stringResource(id = R.string.amount_label),
                    placeHolder = stringResource(id = R.string.transaction_amount_placeholder_label),
                    icon = TablerIcons.CashBanknote,
                    iconDescription = "amount icon",
                    onDone = {
                        keyboardController?.hide()
                    },
                    boxModifier = Modifier.animateItem()
                )
            }
            if (transactionType == TransactionType.DEBIT) {
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
                        },
                        boxModifier = Modifier.animateItem()
                    )
                }
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
                    leadingIcon = transactionType.icon,
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.TYPE
                    },
                    modifier = Modifier.animateItem()
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
                    placeHolder = stringResource(id = R.string.select_category_placeholder_label),
                    leadingIcon = selectedCategory.icon,
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedCategory(Category())
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.CATEGORY
                    },
                    modifier = Modifier.animateItem()
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
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = selectedMethod.name,
                    label = stringResource(id = R.string.method_field_label),
                    placeHolder = stringResource(id = R.string.select_method_placeholder_label),
                    leadingIcon = selectedMethod.icon,
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedMethod(Method())
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.METHOD
                    },
                    modifier = Modifier.animateItem()
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
                    placeHolder = stringResource(id = R.string.select_source_placeholder_label),
                    leadingIcon = selectedSource.icon,
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedSource(Source())
                    },
                    onClick = {
                        resetOneHandMode()
                        popupType = PopupType.SOURCE
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (transactionType == TransactionType.DEBIT && itemHeaderVisible) {
                item(
                    key = "itemHeader",
                    contentType = "header"
                ) {
                    Text(
                        text = stringResource(id = R.string.items_label),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .animateItem(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                itemsIndexed(
                    items = templateItems,
                    key = { _, templateItem -> templateItem.item.uuid }
                ) { index, templateItem ->
                    UpsertTemplateItemCard(
                        modifier = Modifier.animateItem(),
                        index = index,
                        enabled = enabled,
                        data = templateItem,
                        onItemClick = {
                            selectedTemplateItemIndex = index
                            resetOneHandMode()
                            popupType = PopupType.CHANGE_ITEM
                        },
                        onUnitClick = {
                            selectedTemplateItemIndex = index
                            resetOneHandMode()
                            popupType = PopupType.ITEM_UNIT
                        },
                        updatePrice = updateTemplateItemPrice,
                        updateQuantity = updateTemplateItemQuantity,
                        remove = removeItem
                    )
                }
            }
            if (transactionType == TransactionType.DEBIT) {
                item(
                    key = "add-item",
                    contentType = "button"
                ) {
                    OutlinedButton(
                        onClick = {
                            popupType = PopupType.ADD_ITEMS
                        },
                        enabled = enabled && addItemsFilteredList.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .animateItem(),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(text = stringResource(id = R.string.add_item_button_label))
                    }
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
        addItems = {},
        changeItem = { _, _ -> },
        getChangeItemFilteredList = { _ -> emptyList() },
        updateTemplateItemPrice = { _, _ -> },
        updateTransactionItemUnit = { _, _ -> },
        updateTemplateItemQuantity = { _, _ -> },
        removeItem = {},
        setName = {},
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
            addItems = viewModel::addItems,
            changeItem = viewModel::updateItem,
            getChangeItemFilteredList = viewModel::getChangeItemFilteredList,
            updateTransactionItemUnit = viewModel::updateUnit,
            updateTemplateItemPrice = viewModel::updateTemplateItemPrice,
            updateTemplateItemQuantity = viewModel::updateTemplateItemQuantity,
            removeItem = viewModel::removeItem,
            setName = viewModel::setName,
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