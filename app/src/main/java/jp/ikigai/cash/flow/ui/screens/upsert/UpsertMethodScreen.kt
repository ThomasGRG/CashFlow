package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.navigation.navArgument
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceFloppy
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.data.enums.PopupType
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import jp.ikigai.cash.flow.ui.components.common.WaitDialog
import jp.ikigai.cash.flow.ui.components.popups.ConfirmDeletePopup
import jp.ikigai.cash.flow.ui.components.popups.ResetIconPopup
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertMethodScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertMethodScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import jp.ikigai.cash.flow.utils.getIconForMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpsertMethodScreen(
    navigateBack: () -> Unit,
    migrateTransactions: (String) -> Unit,
    chooseIcon: (String) -> Unit,
    selectedIcon: String?,
    setLocale: (Locale?) -> Unit,
    setName: (String) -> Unit,
    upsertMethod: (ImageVector, String) -> Unit,
    deleteMethod: () -> Unit,
    events: Flow<Event>,
    state: UpsertMethodScreenState,
) {
    val configuration = LocalConfiguration.current
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val locale by remember(key1 = configuration) {
        mutableStateOf(
            ConfigurationCompat.getLocales(configuration).get(0)
        )
    }

    LaunchedEffect(key1 = locale) {
        setLocale(locale)
    }

    var icon by remember(key1 = selectedIcon, key2 = state.method) {
        mutableStateOf(selectedIcon?.getIconForMethod() ?: state.method.icon)
    }

    val focusRequester = remember {
        FocusRequester()
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

    val name by remember(key1 = state.name) {
        mutableStateOf(state.name)
    }

    val nameValid by remember(key1 = state.nameValid) {
        mutableStateOf(state.nameValid)
    }

    val nameErrorStringRes by remember(key1 = state.nameErrorStringRes) {
        mutableIntStateOf(state.nameErrorStringRes)
    }

    val transactionCount by remember(key1 = state.transactionCount) {
        mutableStateOf(state.transactionCount)
    }

    val methodUuid by remember(key1 = state.method) {
        mutableStateOf(state.method.uuid)
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
                PopupType.RESET_ICON -> {
                    ResetIconPopup(
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        },
                        reset = {
                            icon = Constants.DEFAULT_METHOD_ICON
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.WARN_DELETE -> {
                    ConfirmDeletePopup(
                        message = stringResource(id = R.string.method_transactions_deletion_warning_label),
                        delete = deleteMethod,
                        migrate = {
                            migrateTransactions(methodUuid)
                        },
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        }
                    )
                }

                PopupType.CONFIRM_DELETE -> {
                    ConfirmDeletePopup(
                        message = stringResource(id = R.string.delete_method_confirmation_label),
                        dismiss = {
                            hidePopup()
                            popupType = PopupType.NONE
                        },
                        delete = deleteMethod
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
                    if (methodUuid.isBlank()) {
                        Text(text = stringResource(id = R.string.create_method_label))
                    } else {
                        Text(text = stringResource(id = R.string.update_method_label))
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (transactionCount.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilledTonalButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                migrateTransactions(methodUuid)
                            },
                            contentPadding = PaddingValues(0.dp),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.migrate_transactions_chip_label,
                                    transactionCount
                                ),
                                modifier = Modifier.padding(10.dp),
                            )
                        }
                    }
                }
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
                            upsertMethod(icon, name.trim())
                        }
                    },
                    extraButtonIcon = if (methodUuid.isNotBlank()) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = Icons.Outlined.Delete.name,
                            )
                        }
                    } else null,
                    extraButtonAction = if (methodUuid.isNotBlank() && enabled) {
                        {
                            popupType = if (transactionCount.isNotEmpty()) {
                                PopupType.WARN_DELETE
                            } else {
                                PopupType.CONFIRM_DELETE
                            }
                        }
                    } else null
                )
            }
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
                imageVector = icon,
                contentDescription = "default method icon",
                modifier = Modifier
                    .size(120.dp)
                    .combinedClickable(
                        enabled = enabled,
                        onClick = {
                            resetOneHandMode()
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            chooseIcon(icon.name)
                        },
                        onLongClick = {
                            resetOneHandMode()
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            popupType = PopupType.RESET_ICON
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

@Preview
@Composable
fun UpsertMethodScreenPreview() {
    UpsertMethodScreen(
        navigateBack = {},
        migrateTransactions = {},
        chooseIcon = {},
        selectedIcon = null,
        setName = {},
        setLocale = {},
        upsertMethod = { _, _ -> },
        deleteMethod = {},
        events = emptyList<Event>().asFlow(),
        state = UpsertMethodScreenState()
    )
}

fun NavGraphBuilder.upsertMethodScreen(navController: NavController) {
    animatedComposable(
        Routes.UpsertMethod.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = ""
                type = NavType.StringType
            }
        )
    ) {
        val viewModel: UpsertMethodScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertMethodScreen(
            navigateBack = {
                navController.popBackStack()
            },
            migrateTransactions = { uuid ->
                navController.navigate(Routes.MigrateMethod.getRoute(uuid)) {
                    launchSingleTop = true
                }
            },
            chooseIcon = { defaultIcon ->
                navController.navigate(Routes.ChooseIcon.getRoute(defaultIcon)) {
                    launchSingleTop = true
                }
            },
            selectedIcon = it.savedStateHandle.get<String>("icon"),
            setName = viewModel::setName,
            setLocale = viewModel::setLocale,
            upsertMethod = viewModel::upsertMethod,
            deleteMethod = viewModel::deleteMethod,
            events = viewModel.event,
            state = state
        )
    }
}