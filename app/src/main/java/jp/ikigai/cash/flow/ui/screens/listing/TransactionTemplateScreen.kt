package jp.ikigai.cash.flow.ui.screens.listing

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.TransactionTemplateCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.screenStates.listing.TransactionTemplateScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionTemplateScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionTemplateScreen(
    navigateBack: () -> Unit,
    addNewTransactionTemplate: () -> Unit,
    editTransactionTemplate: (String) -> Unit,
    events: Flow<Event>,
    state: TransactionTemplateScreenState
) {
    val numberFormatter by remember {
        mutableStateOf(getNumberFormatter())
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
                Event.SourceCategoryMethodRequired -> "At least one category, method and source are required to proceed."
                Event.SourceCategoryRequired -> "At least one category and source are required to proceed."
                Event.SourceMethodRequired -> "At least one method and source are required to proceed."
                Event.CategoryMethodRequired -> "At least one category and method are required to proceed."
                Event.CategoryRequired -> "At least one category is required to proceed."
                Event.MethodRequired -> "At least one method is required to proceed."
                Event.SourceRequired -> "At least one source is required to proceed."
                else -> ""
            }
            showToastBar = true
        }
    }

    LaunchedEffect(showToastBar) {
        if (showToastBar) {
            delay(2000)
            showToastBar = false
        }
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val templates by remember(key1 = state.templates) {
        mutableStateOf(state.templates)
    }

    val showEmptyPlaceholder by remember(key1 = state.templates) {
        mutableStateOf(state.templates.isEmpty())
    }

    val templateCount by remember(key1 = state.templates) {
        mutableStateOf(
            numberFormatter
                .format(state.templates.size)
                .toString()
        )
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = showToastBar,
        toastBarText = toastBarString,
        onDismissToastBar = {
            showToastBar = false
        },
        showEmptyPlaceholder = showEmptyPlaceholder,
        emptyPlaceholderText = stringResource(id = R.string.templates_screen_empty_placeholder_label),
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(text = stringResource(id = R.string.templates_label))
                        Text(
                            text = stringResource(
                                id = R.string.templates_count_label,
                                templateCount
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.alpha(0.8f)
                        )
                    }
                }
            )
        },
        bottomBar = {
            ThreeSlotRoundedBottomBar(
                navigateBack = navigateBack,
                floatingButtonAction = addNewTransactionTemplate,
                floatingButtonIcon = {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = Icons.Filled.Add.name)
                },
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
            items(
                items = templates,
                key = { template -> template.uuid }
            ) { transactionTemplateWithIcons ->
                TransactionTemplateCard(
                    transactionTemplateWithIcons = transactionTemplateWithIcons,
                    modifier = Modifier.animateItemPlacement(),
                    amount = numberFormatter.format(transactionTemplateWithIcons.amount).toString(),
                    onClick = {
                        resetOneHandMode()
                        editTransactionTemplate(transactionTemplateWithIcons.uuid)
                    },
                    onLongClick = {
                        resetOneHandMode()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun TransactionTemplateScreenPreview() {
    TransactionTemplateScreen(
        navigateBack = {},
        addNewTransactionTemplate = {},
        editTransactionTemplate = {},
        events = emptyList<Event>().asFlow(),
        state = TransactionTemplateScreenState()
    )
}

fun NavGraphBuilder.transactionTemplateScreen(navController: NavController) {
    animatedComposable(
        Routes.Templates.route
    ) {
        val viewModel: TransactionTemplateScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        TransactionTemplateScreen(
            navigateBack = {
                navController.popBackStack()
            },
            addNewTransactionTemplate = {
                if (viewModel.canAddTransaction()) {
                    navController.navigate(Routes.UpsertTemplate.getRoute()) {
                        launchSingleTop = true
                    }
                }
            },
            editTransactionTemplate = { uuid ->
                navController.navigate(Routes.UpsertTemplate.getRoute(uuid)) {
                    launchSingleTop = true
                }
            },
            events = viewModel.event,
            state = state
        )
    }
}