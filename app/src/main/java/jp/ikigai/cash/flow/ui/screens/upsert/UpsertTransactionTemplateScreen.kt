package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.FileText
import compose.icons.tablericons.LetterCase
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmDeleteSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmNavigationSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectAccountSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectCategorySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectCounterPartySheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectMethodSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.SelectTransactionTypeSheet
import jp.ikigai.cash.flow.ui.components.buttons.CustomOutlinedButton
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionTemplateScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertTransactionTemplateScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertTransactionTemplateScreen(
    navigateBack: () -> Unit,
    checkNameAlreadyInUse: (String) -> Unit,
    setLocale: (Locale?) -> Unit,
    setName: (String) -> Unit,
    setTitle: (String) -> Unit,
    setDescription: (String) -> Unit,
    setAmount: (String) -> Unit,
    setSelectedAccount: (AccountWithTransactionMetadata) -> Unit,
    setSelectedCategory: (CategoryWithTransactionMetadata) -> Unit,
    setSelectedCounterParty: (CounterPartyWithTransactionMetadata) -> Unit,
    setSelectedMethod: (MethodWithTransactionMetadata) -> Unit,
    setTransactionType: (TransactionType) -> Unit,
    upsertTransactionTemplate: (String, String, String) -> Unit,
    deleteTransactionTemplate: () -> Unit,
    events: Flow<Event>,
    state: UpsertTransactionTemplateScreenState
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val configuration = LocalConfiguration.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val locale by remember(key1 = configuration) {
        mutableStateOf(
            ConfigurationCompat.getLocales(configuration).get(0)
        )
    }

    LaunchedEffect(key1 = locale) {
        setLocale(locale)
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

    val categories by remember(key1 = state.categories) {
        mutableStateOf(state.categories)
    }

    val counterParties by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties)
    }

    val methods by remember(key1 = state.methods) {
        mutableStateOf(state.methods)
    }

    val transactionTemplateId by remember(key1 = state.transactionTemplate) {
        mutableLongStateOf(state.transactionTemplate.templateId)
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

    val title by remember(key1 = state.title) {
        mutableStateOf(state.title)
    }

    val description by remember(key1 = state.description) {
        mutableStateOf(state.description)
    }

    val amount by remember(key1 = state.displayAmount) {
        mutableStateOf(state.displayAmount)
    }

    val selectedAccount by remember(key1 = state.selectedAccount) {
        mutableStateOf(state.selectedAccount)
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

    val transactionType by remember(key1 = state.type) {
        mutableStateOf(state.type)
    }

    var sheetType by remember {
        mutableStateOf(SheetType.NONE)
    }

    val hasUnsavedChanges by remember(key1 = state.hasUnsavedChanges) {
        mutableStateOf(state.hasUnsavedChanges)
    }

    BackHandler(
        enabled = enabled && hasUnsavedChanges
    ) {
        sheetType = SheetType.CONFIRM_NAVIGATION
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

                SheetType.CATEGORY -> {
                    SelectCategorySheet(
                        index = categories.indexOfFirst { it.categoryId == selectedCategory.categoryId }
                            .coerceAtLeast(0),
                        selectedCategoryId = selectedCategory.categoryId,
                        setSelectedCategory = setSelectedCategory,
                        categories = categories,
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.COUNTERPARTY -> {
                    SelectCounterPartySheet(
                        index = counterParties.indexOfFirst { it.counterPartyId == selectedCounterParty.counterPartyId }
                            .coerceAtLeast(0),
                        selectedCounterPartyId = selectedCounterParty.counterPartyId,
                        setSelectedCounterParty = setSelectedCounterParty,
                        counterParties = counterParties,
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.METHOD -> {
                    SelectMethodSheet(
                        index = methods.indexOfFirst { it.methodId == selectedMethod.methodId }
                            .coerceAtLeast(0),
                        selectedMethodId = selectedMethod.methodId,
                        setSelectedMethod = setSelectedMethod,
                        methods = methods,
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.ACCOUNT -> {
                    SelectAccountSheet(
                        index = accounts.indexOfFirst { it.accountId == selectedAccount.accountId }
                            .coerceAtLeast(0),
                        selectedAccountId = selectedAccount.accountId,
                        setSelectedAccount = setSelectedAccount,
                        accounts = accounts,
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.TYPE -> {
                    SelectTransactionTypeSheet(
                        selectedTransactionType = transactionType,
                        setSelectedTransactionType = { selectedTransactionType ->
                            setTransactionType(selectedTransactionType)
                        },
                        dismiss = {
                            scope
                                .launch { sheetState.hide() }
                                .invokeOnCompletion { sheetType = SheetType.NONE }
                        }
                    )
                }

                SheetType.CONFIRM_DELETE -> {
                    ConfirmDeleteSheet(
                        message = stringResource(id = R.string.delete_template_confirmation_label),
                        delete = deleteTransactionTemplate,
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
        showEmptyPlaceholder = false,
        emptyPlaceholderText = "",
        topBar = {
            TopAppBar(
                title = {
                    if (transactionTemplateId == 0L) {
                        Text(text = stringResource(id = R.string.create_template_label))
                    } else {
                        Text(text = stringResource(id = R.string.update_template_label))
                    }
                }
            )
        },
        bottomBar = {
            ThreeSlotBottomAppBar(
                navigateBack = {
                    keyboardController?.hide()
                    if (enabled && hasUnsavedChanges) {
                        sheetType = SheetType.CONFIRM_NAVIGATION
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
                        upsertTransactionTemplate(
                            name.trim(),
                            title,
                            description
                        )
                    }
                },
                extraButtonIcon = if (transactionTemplateId > 0) {
                    {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = Icons.Outlined.Delete.name,
                        )
                    }
                } else null,
                extraButtonAction = if (transactionTemplateId > 0 && enabled) {
                    {
                        sheetType = SheetType.CONFIRM_DELETE
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
                    value = title,
                    onValueChange = setTitle,
                    enabled = enabled,
                    label = stringResource(id = R.string.title_field_label),
                    placeHolder = stringResource(id = R.string.title_placeholder_label),
                    icon = TablerIcons.LetterCase,
                    iconDescription = "title icon",
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
                key = "description",
                contentType = "type-enabled"
            ) {
                RoundedCornerOutlinedTextField(
                    value = description,
                    onValueChange = setDescription,
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
                    enabled = enabled,
                    label = stringResource(id = R.string.amount_label),
                    placeHolder = stringResource(id = R.string.transaction_amount_placeholder_label),
                    icon = TablerIcons.CashBanknote,
                    iconDescription = "amount icon",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    onDone = {
                        keyboardController?.hide()
                    },
                    boxModifier = Modifier.animateItem()
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
                    leadingIcon = transactionType.icon,
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.TYPE
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
                    value = selectedCategory.categoryName,
                    label = stringResource(id = R.string.category_field_label),
                    placeHolder = stringResource(id = R.string.select_category_placeholder_label),
                    leadingIcon = selectedCategory.icon,
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedCategory(
                            CategoryWithTransactionMetadata(
                                categoryId = 0,
                                categoryName = "",
                                icon = TablerIcons.Archive,
                                transactionCount = 0,
                                lastUsed = null
                            )
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.CATEGORY
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
                    value = selectedCounterParty.counterPartyName,
                    label = stringResource(id = R.string.counter_party_field_label),
                    placeHolder = stringResource(id = R.string.counter_party_placeholder_label),
                    leadingIcon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedCounterParty(
                            CounterPartyWithTransactionMetadata(
                                counterPartyId = 0,
                                counterPartyName = "",
                                transactionCount = 0,
                                lastUsed = null
                            )
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.COUNTERPARTY
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
                    value = selectedMethod.methodName,
                    label = stringResource(id = R.string.method_field_label),
                    placeHolder = stringResource(id = R.string.select_method_placeholder_label),
                    leadingIcon = Constants.DEFAULT_METHOD_ICON,
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedMethod(
                            MethodWithTransactionMetadata(
                                methodId = 0,
                                methodName = "",
                                transactionCount = 0,
                                lastUsed = null
                            )
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.METHOD
                    },
                    modifier = Modifier.animateItem()
                )
            }
            item(
                key = "account",
                contentType = "dropDown"
            ) {
                CustomOutlinedButton(
                    enabled = enabled,
                    value = if (selectedAccount.accountId > 0) "${selectedAccount.accountName} - ${selectedAccount.formattedBalance}" else "",
                    label = stringResource(id = R.string.account_field_label),
                    placeHolder = stringResource(id = R.string.select_account_placeholder_label),
                    leadingIcon = Constants.DEFAULT_ACCOUNT_ICON,
                    trailingIcon = Icons.Filled.Clear,
                    onTrailingIconClick = {
                        setSelectedAccount(
                            AccountWithTransactionMetadata(
                                accountId = 0,
                                accountName = "",
                                balance = 0.0,
                                currency = "INR",
                                formattedBalance = "",
                                transactionCount = 0,
                                lastUsed = null
                            )
                        )
                    },
                    onClick = {
                        resetOneHandMode()
                        sheetType = SheetType.ACCOUNT
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Preview
@Composable
fun UpsertTransactionTemplateScreenPreview() {
    UpsertTransactionTemplateScreen(
        navigateBack = {},
        checkNameAlreadyInUse = {},
        setLocale = {},
        setName = {},
        setTitle = {},
        setDescription = {},
        setAmount = {},
        setSelectedAccount = {},
        setSelectedCategory = {},
        setSelectedCounterParty = {},
        setSelectedMethod = {},
        setTransactionType = {},
        upsertTransactionTemplate = { _, _, _ -> },
        deleteTransactionTemplate = {},
        events = emptyList<Event>().asFlow(),
        state = UpsertTransactionTemplateScreenState()
    )
}

fun NavGraphBuilder.upsertTransactionTemplateScreen(navController: NavController) {
    composable(
        route = Routes.UpsertTemplate.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = 0L
                type = NavType.LongType
            }
        )
    ) {
        val viewModel: UpsertTransactionTemplateScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertTransactionTemplateScreen(
            navigateBack = {
                navController.popBackStack()
            },
            checkNameAlreadyInUse = viewModel::checkNameAlreadyInUse,
            setLocale = viewModel::setLocale,
            setName = viewModel::setName,
            setTitle = viewModel::setTitle,
            setDescription = viewModel::setDescription,
            setAmount = viewModel::setAmount,
            setSelectedAccount = viewModel::setSelectedAccount,
            setSelectedCategory = viewModel::setSelectedCategory,
            setSelectedCounterParty = viewModel::setSelectedCounterParty,
            setSelectedMethod = viewModel::setSelectedMethod,
            setTransactionType = viewModel::setTransactionType,
            upsertTransactionTemplate = viewModel::upsertTransactionTemplate,
            deleteTransactionTemplate = viewModel::deleteTransactionTemplate,
            events = viewModel.event,
            state = state
        )
    }
}