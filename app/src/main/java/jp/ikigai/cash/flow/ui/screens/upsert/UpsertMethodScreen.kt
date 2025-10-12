package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.SheetType
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotBottomAppBar
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmDeleteSheet
import jp.ikigai.cash.flow.ui.components.bottomsheets.ConfirmNavigationSheet
import jp.ikigai.cash.flow.ui.components.common.LandscapeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertMethodScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertMethodScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertMethodScreen(
    navigateBack: () -> Unit,
    migrateTransactions: (Long) -> Unit,
    checkNameAlreadyInUse: (String) -> Unit,
    setLocale: (Locale?) -> Unit,
    setName: (String) -> Unit,
    upsertMethod: (String) -> Unit,
    deleteMethod: () -> Unit,
    events: Flow<Event>,
    state: UpsertMethodScreenState,
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

    val transactionCount by remember(key1 = state.transactionCount) {
        mutableLongStateOf(state.transactionCount)
    }

    val formattedTransactionCount by remember(key1 = state.formattedTransactionCount) {
        mutableStateOf(state.formattedTransactionCount)
    }

    val methodId by remember(key1 = state.method) {
        mutableLongStateOf(state.method.methodId)
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

                    SheetType.WARN_DELETE -> {
                        ConfirmDeleteSheet(
                            message = stringResource(id = R.string.method_transactions_deletion_warning_label),
                            delete = deleteMethod,
                            migrate = {
                                migrateTransactions(methodId)
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
                            message = stringResource(id = R.string.delete_method_confirmation_label),
                            delete = deleteMethod,
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
                        imageVector = Constants.DEFAULT_METHOD_ICON,
                        contentDescription = "default method icon",
                        modifier = Modifier
                            .size(120.dp),
                        tint = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (transactionCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    migrateTransactions(methodId)
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
                    ThreeSlotBottomAppBar(
                        title = if (methodId == 0L) {
                            stringResource(id = R.string.create_method_label)
                        } else {
                            stringResource(id = R.string.update_method_label)
                        },
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
                                upsertMethod(name.trim())
                            }
                        },
                        extraButtonIcon = if (methodId > 0) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = Icons.Outlined.Delete.name,
                                )
                            }
                        } else null,
                        extraButtonAction = if (methodId > 0 && enabled) {
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
            },
            secondColContent = {
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
                    RoundedCornerOutlinedTextField(
                        enabled = enabled,
                        value = name,
                        onValueChange = setName,
                        modifier = Modifier.focusRequester(focusRequester = focusRequester),
                        label = stringResource(id = R.string.name_field_label),
                        placeHolder = stringResource(id = R.string.method_name_placeholder_label),
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

                    SheetType.WARN_DELETE -> {
                        ConfirmDeleteSheet(
                            message = stringResource(id = R.string.method_transactions_deletion_warning_label),
                            delete = deleteMethod,
                            migrate = {
                                migrateTransactions(methodId)
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
                            message = stringResource(id = R.string.delete_method_confirmation_label),
                            delete = deleteMethod,
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
                        if (methodId == 0L) {
                            Text(text = stringResource(id = R.string.create_method_label))
                        } else {
                            Text(text = stringResource(id = R.string.update_method_label))
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
                                    migrateTransactions(methodId)
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
                                upsertMethod(name.trim())
                            }
                        },
                        extraButtonIcon = if (methodId > 0) {
                            {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = Icons.Outlined.Delete.name,
                                )
                            }
                        } else null,
                        extraButtonAction = if (methodId > 0 && enabled) {
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
                    imageVector = Constants.DEFAULT_METHOD_ICON,
                    contentDescription = "default method icon",
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
                    modifier = Modifier.focusRequester(focusRequester = focusRequester),
                    label = stringResource(id = R.string.name_field_label),
                    placeHolder = stringResource(id = R.string.method_name_placeholder_label),
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
fun UpsertMethodScreenPreview() {
    UpsertMethodScreen(
        navigateBack = {},
        migrateTransactions = {},
        checkNameAlreadyInUse = {},
        setName = {},
        setLocale = {},
        upsertMethod = {},
        deleteMethod = {},
        events = emptyList<Event>().asFlow(),
        state = UpsertMethodScreenState()
    )
}

fun NavGraphBuilder.upsertMethodScreen(navController: NavController) {
    composable(
        route = Routes.UpsertMethod.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = 0L
                type = NavType.LongType
            }
        )
    ) {
        val viewModel: UpsertMethodScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertMethodScreen(
            navigateBack = {
                navController.popBackStack()
            },
            migrateTransactions = { id ->
                navController.navigate(Routes.MigrateMethod.getRoute(id)) {
                    launchSingleTop = true
                }
            },
            checkNameAlreadyInUse = viewModel::checkNameAlreadyInUse,
            setName = viewModel::setName,
            setLocale = viewModel::setLocale,
            upsertMethod = viewModel::upsertMethod,
            deleteMethod = viewModel::deleteMethod,
            events = viewModel.event,
            state = state
        )
    }
}