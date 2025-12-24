package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import compose.icons.TablerIcons
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.ui.components.bottombars.UpsertScreenBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.ChooseIconLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ChooseIconSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmDeleteLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmDeleteSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmNavigationLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmNavigationSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ResetIconLandscapeSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ResetIconSheet
import jp.ikigai.cash.flow.ui.components.common.LandscapeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertCategoryScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertCategoryScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertCategoryScreen(
    navigateBack: () -> Unit,
    migrateTransactions: (Long) -> Unit,
    checkNameAlreadyInUse: (String) -> Unit,
    setLocale: (Locale?) -> Unit,
    setName: (String) -> Unit,
    setIcon: (ImageVector) -> Unit,
    upsertCategory: (ImageVector, String) -> Unit,
    deleteCategory: () -> Unit,
    events: Flow<Event>,
    state: UpsertCategoryScreenState,
) {
    val configuration = LocalConfiguration.current
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

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

    val selectedIcon by remember(key1 = state.selectedIcon) {
        mutableStateOf(state.selectedIcon)
    }

    val transactionCount by remember(key1 = state.transactionCount) {
        mutableLongStateOf(state.transactionCount)
    }

    val formattedTransactionCount by remember(key1 = state.formattedTransactionCount) {
        mutableStateOf(state.formattedTransactionCount)
    }

    val categoryId by remember(key1 = state.category) {
        mutableLongStateOf(state.category.categoryId)
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
                        ConfirmNavigationLandscapeSheet(
                            message = stringResource(id = R.string.navigation_confirmation_label),
                            navigate = navigateBack,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.RESET_ICON -> {
                        ResetIconLandscapeSheet(
                            reset = {
                                setIcon(Constants.DEFAULT_CATEGORY_ICON)
                            },
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.WARN_DELETE -> {
                        ConfirmDeleteLandscapeSheet(
                            message = stringResource(id = R.string.category_transactions_deletion_warning_label),
                            delete = deleteCategory,
                            migrate = {
                                migrateTransactions(categoryId)
                            },
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.CONFIRM_DELETE -> {
                        ConfirmDeleteLandscapeSheet(
                            message = stringResource(id = R.string.delete_category_confirmation_label),
                            delete = deleteCategory,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.SELECT_ICON -> {
                        ChooseIconLandscapeSheet(
                            setIcon = setIcon,
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
                        imageVector = selectedIcon,
                        contentDescription = "default category icon",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(
                                RoundedCornerShape(14.dp)
                            )
                            .combinedClickable(
                                enabled = enabled,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    sheetType = SheetType.SELECT_ICON
                                },
                                onLongClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    sheetType = SheetType.RESET_ICON
                                }
                            ),
                        tint = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    UpsertScreenBottomAppBar(
                        title = if (categoryId == 0L) {
                            stringResource(id = R.string.create_category_label)
                        } else {
                            stringResource(id = R.string.update_category_label)
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
                                upsertCategory(selectedIcon, name.trim())
                            }
                        },
                        deleteClick = if (categoryId > 0 && enabled) {
                            {
                                sheetType = if (transactionCount > 0) {
                                    SheetType.WARN_DELETE
                                } else {
                                    SheetType.CONFIRM_DELETE
                                }
                            }
                        } else null,
                        migrateClick = if (transactionCount > 0) {
                            { migrateTransactions(categoryId) }
                        } else null,
                        transactionCount = formattedTransactionCount,
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
                        modifier = Modifier.focusRequester(focusRequester = focusRequester),
                        label = stringResource(id = R.string.name_field_label),
                        placeHolder = stringResource(id = R.string.category_name_placeholder_label),
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

                    SheetType.RESET_ICON -> {
                        ResetIconSheet(
                            reset = {
                                setIcon(Constants.DEFAULT_CATEGORY_ICON)
                            },
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.WARN_DELETE -> {
                        ConfirmDeleteSheet(
                            message = stringResource(id = R.string.category_transactions_deletion_warning_label),
                            delete = deleteCategory,
                            migrate = {
                                migrateTransactions(categoryId)
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
                            message = stringResource(id = R.string.delete_category_confirmation_label),
                            delete = deleteCategory,
                            dismiss = {
                                scope
                                    .launch { sheetState.hide() }
                                    .invokeOnCompletion { sheetType = SheetType.NONE }
                            }
                        )
                    }

                    SheetType.SELECT_ICON -> {
                        ChooseIconSheet(
                            setIcon = setIcon,
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
                        if (categoryId == 0L) {
                            Text(text = stringResource(id = R.string.create_category_label))
                        } else {
                            Text(text = stringResource(id = R.string.update_category_label))
                        }
                    },
                    expandedHeight = expandedHeight,
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                Column {
                    if (transactionCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    migrateTransactions(categoryId)
                                },
                                contentPadding = PaddingValues(0.dp),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.migrate_transactions_chip_label,
                                        formattedTransactionCount
                                    ),
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        }
                    }
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
                                upsertCategory(selectedIcon, name.trim())
                            }
                        },
                        deleteClick = if (categoryId > 0 && enabled) {
                            {
                                sheetType = if (transactionCount > 0) {
                                    SheetType.WARN_DELETE
                                } else {
                                    SheetType.CONFIRM_DELETE
                                }
                            }
                        } else null
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 10.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = "default category icon",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(
                            RoundedCornerShape(14.dp)
                        )
                        .combinedClickable(
                            enabled = enabled,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                sheetType = SheetType.SELECT_ICON
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                sheetType = SheetType.RESET_ICON
                            }
                        ),
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
                    modifier = Modifier.focusRequester(focusRequester = focusRequester),
                    label = stringResource(id = R.string.name_field_label),
                    placeHolder = stringResource(id = R.string.category_name_placeholder_label),
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
            }
        }
    }
}

@Preview
@Composable
fun UpsertCategoryScreenPreview() {
    UpsertCategoryScreen(
        navigateBack = {},
        migrateTransactions = {},
        checkNameAlreadyInUse = {},
        setLocale = {},
        setName = {},
        setIcon = {},
        upsertCategory = { _, _ -> },
        deleteCategory = {},
        events = emptyList<Event>().asFlow(),
        state = UpsertCategoryScreenState()
    )
}

fun NavGraphBuilder.upsertCategoryScreen(navController: NavController) {
    composable(
        route = Routes.UpsertCategory.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = 0L
                type = NavType.LongType
            }
        )
    ) {
        val viewModel: UpsertCategoryScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertCategoryScreen(
            navigateBack = {
                navController.popBackStack()
            },
            migrateTransactions = { id ->
                navController.navigate(Routes.MigrateCategory.getRoute(id)) {
                    launchSingleTop = true
                }
            },
            checkNameAlreadyInUse = viewModel::checkNameAlreadyInUse,
            setLocale = viewModel::setLocale,
            setName = viewModel::setName,
            setIcon = viewModel::setIcon,
            upsertCategory = viewModel::upsertCategory,
            deleteCategory = viewModel::deleteCategory,
            events = viewModel.event,
            state = state
        )
    }
}