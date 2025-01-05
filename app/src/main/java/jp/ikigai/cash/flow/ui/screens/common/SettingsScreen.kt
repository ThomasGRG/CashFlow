package jp.ikigai.cash.flow.ui.screens.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.ui.components.bottombars.ThreeSlotRoundedBottomBar
import jp.ikigai.cash.flow.ui.screenStates.common.SettingsScreenState
import jp.ikigai.cash.flow.ui.viewmodels.common.SettingsScreenViewModel
import jp.ikigai.cash.flow.utils.animatedComposable
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    reCount: () -> Unit,
    state: SettingsScreenState
) {
    val haptics = LocalHapticFeedback.current

    val loading by remember(state.loading) {
        mutableStateOf(state.loading)
    }

    if (loading) {
        BasicAlertDialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false
            ),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.please_wait_dialog_label),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .animateContentSize()
            .navigationBarsPadding()
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings_label))
                }
            )
        },
        bottomBar = {
            ThreeSlotRoundedBottomBar(
                navigateBack = navigateBack,
                enabled = true
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    reCount()
                },
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp)
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) {
                Text(text = stringResource(R.string.re_count_button_label))
            }
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        navigateBack = {},
        reCount = {},
        state = SettingsScreenState()
    )
}

fun NavGraphBuilder.settingsScreen(navController: NavController) {
    animatedComposable(
        route = Routes.Settings.route
    ) {
        val viewModel: SettingsScreenViewModel = koinViewModel()
        val state by viewModel.state.collectAsState()

        SettingsScreen(
            navigateBack = {
                navController.popBackStack()
            },
            reCount = viewModel::reCount,
            state = state
        )
    }
}