package jp.ikigai.cash.flow.ui.screens.listing

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.InfoCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.screenStates.listing.MethodScreenState
import jp.ikigai.cash.flow.ui.viewmodels.listing.MethodScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import jp.ikigai.cash.flow.utils.getNumberFormatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MethodScreen(
    navigateBack: () -> Unit,
    addNewTransactionMethod: () -> Unit,
    editTransactionMethod: (String) -> Unit,
    state: MethodScreenState
) {
    val numberFormatter by remember {
        mutableStateOf(getNumberFormatter())
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val showEmptyPlaceholder by remember(key1 = state.methods) {
        mutableStateOf(state.methods.isEmpty())
    }

    val methodCount by remember(key1 = state.methods) {
        mutableStateOf(
            numberFormatter
                .format(state.methods.size)
                .toString()
        )
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = false,
        toastBarText = "",
        onDismissToastBar = {},
        showEmptyPlaceholder = showEmptyPlaceholder,
        emptyPlaceholderText = stringResource(id = R.string.methods_screen_empty_placeholder_label),
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(text = stringResource(id = R.string.methods_label))
                        Text(
                            text = stringResource(id = R.string.method_count_label, methodCount),
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
                floatingButtonAction = addNewTransactionMethod,
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
                items = state.methods,
                key = { method -> method.uuid }
            ) { method ->
                InfoCard(
                    title = method.name,
                    icon = method.icon,
                    frequency = numberFormatter.format(method.frequency).toString(),
                    onClick = {
                        resetOneHandMode()
                        editTransactionMethod(method.uuid)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun MethodScreenPreview() {
    MethodScreen(
        navigateBack = {},
        addNewTransactionMethod = {},
        editTransactionMethod = {},
        state = MethodScreenState()
    )
}

fun NavGraphBuilder.methodScreen(navController: NavController) {
    animatedComposable(
        Routes.Methods.route
    ) {
        val viewModel: MethodScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        MethodScreen(
            navigateBack = {
                navController.popBackStack()
            },
            addNewTransactionMethod = {
                navController.navigate(Routes.UpsertMethod.getRoute()) {
                    launchSingleTop = true
                }
            },
            editTransactionMethod = { uuid ->
                navController.navigate(Routes.UpsertMethod.getRoute(uuid)) {
                    launchSingleTop = true
                }
            },
            state = state
        )
    }
}