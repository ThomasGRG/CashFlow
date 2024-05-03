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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.ui.screenStates.listing.CounterPartyScreenState
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.components.cards.InfoCard
import jp.ikigai.cash.flow.ui.components.common.OneHandModeScaffold
import jp.ikigai.cash.flow.ui.components.common.OneHandModeSpacer
import jp.ikigai.cash.flow.ui.viewmodels.listing.CounterPartyScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import jp.ikigai.cash.flow.utils.getNumberFormatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterPartyScreen(
    navigateBack: () -> Unit,
    addNewCounterParty: () -> Unit,
    editCounterParty: (String) -> Unit,
    state: CounterPartyScreenState
) {
    val numberFormatter by remember {
        mutableStateOf(getNumberFormatter())
    }

    val loading by remember(key1 = state.loading) {
        mutableStateOf(state.loading)
    }

    val showEmptyPlaceholder by remember(key1 = state.counterParties) {
        mutableStateOf(state.counterParties.isEmpty())
    }

    val counterPartyCount by remember(key1 = state.counterParties) {
        mutableStateOf(
            numberFormatter
                .format(state.counterParties.size)
                .toString()
        )
    }

    OneHandModeScaffold(
        loading = loading,
        showToastBar = false,
        toastBarText = "",
        onDismissToastBar = {},
        showEmptyPlaceholder = showEmptyPlaceholder,
        emptyPlaceholderText = "You have not added any counter parties.",
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(text = "Counter parties")
                        Text(
                            text = "$counterPartyCount counter parties",
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
                floatingButtonAction = addNewCounterParty,
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
                items = state.counterParties,
                key = { counterParty -> counterParty.uuid }
            ) { counterParty ->
                InfoCard(
                    title = counterParty.name,
                    icon = counterParty.icon,
                    frequency = numberFormatter.format(counterParty.frequency).toString(),
                    onClick = {
                        resetOneHandMode()
                        editCounterParty(counterParty.uuid)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun CounterPartyScreenPreview() {
    CounterPartyScreen(
        navigateBack = {},
        addNewCounterParty = {},
        editCounterParty = {},
        state = CounterPartyScreenState()
    )
}

fun NavGraphBuilder.counterPartyScreen(navController: NavController) {
    animatedComposable(
        Routes.CounterParties.route
    ) {
        val viewModel: CounterPartyScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        CounterPartyScreen(
            navigateBack = {
                navController.popBackStack()
            },
            addNewCounterParty = {
                navController.navigate(Routes.UpsertCounterParty.getRoute()) {
                    launchSingleTop = true
                }
            },
            editCounterParty = { uuid ->
                navController.navigate(Routes.UpsertCounterParty.getRoute(uuid)) {
                    launchSingleTop = true
                }
            },
            state = state
        )
    }
}