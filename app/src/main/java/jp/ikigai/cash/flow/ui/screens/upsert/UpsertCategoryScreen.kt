package jp.ikigai.cash.flow.ui.screens.upsert

import androidx.activity.compose.BackHandler
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
import jp.ikigai.cash.flow.ui.components.popups.ChooseIconPopup
import jp.ikigai.cash.flow.ui.components.popups.ConfirmDeletePopup
import jp.ikigai.cash.flow.ui.components.popups.ConfirmNavigationPopup
import jp.ikigai.cash.flow.ui.components.popups.ResetIconPopup
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertCategoryScreenState
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertCategoryScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UpsertCategoryScreen(
    navigateBack: () -> Unit,
    migrateTransactions: (String) -> Unit,
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
        mutableStateOf(state.transactionCount)
    }

    val categoryUuid by remember(key1 = state.category) {
        mutableStateOf(state.category.uuid)
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

    BackHandler {
        if (state.category.name != state.name || state.category.icon != selectedIcon) {
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

                PopupType.RESET_ICON -> {
                    ResetIconPopup(
                        dismiss = hidePopup,
                        reset = {
                            setIcon(Constants.DEFAULT_CATEGORY_ICON)
                        }
                    )
                }

                PopupType.WARN_DELETE -> {
                    ConfirmDeletePopup(
                        message = stringResource(id = R.string.category_transactions_deletion_warning_label),
                        delete = deleteCategory,
                        migrate = {
                            migrateTransactions(categoryUuid)
                        },
                        dismiss = hidePopup
                    )
                }

                PopupType.CONFIRM_DELETE -> {
                    ConfirmDeletePopup(
                        message = stringResource(id = R.string.delete_category_confirmation_label),
                        dismiss = hidePopup,
                        delete = deleteCategory
                    )
                }

                PopupType.SELECT_ICON -> {
                    ChooseIconPopup(
                        dismiss = hidePopup,
                        setIcon = setIcon
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
                    if (categoryUuid.isBlank()) {
                        Text(text = stringResource(id = R.string.create_category_label))
                    } else {
                        Text(text = stringResource(id = R.string.update_category_label))
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
                                migrateTransactions(categoryUuid)
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
                        if (state.category.name != state.name || state.category.icon != selectedIcon) {
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
                            upsertCategory(selectedIcon, name.trim())
                        }
                    },
                    extraButtonIcon = if (categoryUuid.isNotBlank()) {
                        {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = Icons.Outlined.Delete.name,
                            )
                        }
                    } else null,
                    extraButtonAction = if (categoryUuid.isNotBlank() && enabled) {
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
                imageVector = selectedIcon,
                contentDescription = "default category icon",
                modifier = Modifier
                    .size(120.dp)
                    .combinedClickable(
                        enabled = enabled,
                        onClick = {
                            resetOneHandMode()
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            popupType = PopupType.SELECT_ICON
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

@Preview
@Composable
fun UpsertCategoryScreenPreview() {
    UpsertCategoryScreen(
        navigateBack = {},
        migrateTransactions = {},
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
    animatedComposable(
        route = Routes.UpsertCategory.route,
        arguments = listOf(
            navArgument("id") {
                defaultValue = ""
                type = NavType.StringType
            }
        )
    ) {
        val viewModel: UpsertCategoryScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        UpsertCategoryScreen(
            navigateBack = {
                navController.popBackStack()
            },
            migrateTransactions = { uuid ->
                navController.navigate(Routes.MigrateCategory.getRoute(uuid)) {
                    launchSingleTop = true
                }
            },
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