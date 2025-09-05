package jp.ikigai.cash.flow.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.ui.screens.charts.chartsScreen
import jp.ikigai.cash.flow.ui.screens.common.exportTransactionsScreen
import jp.ikigai.cash.flow.ui.screens.common.importBackupScreen
import jp.ikigai.cash.flow.ui.screens.common.settingsScreen
import jp.ikigai.cash.flow.ui.screens.listing.accountScreen
import jp.ikigai.cash.flow.ui.screens.listing.auditLogsScreen
import jp.ikigai.cash.flow.ui.screens.listing.categoryScreen
import jp.ikigai.cash.flow.ui.screens.listing.counterPartyScreen
import jp.ikigai.cash.flow.ui.screens.listing.methodScreen
import jp.ikigai.cash.flow.ui.screens.listing.transactionTemplateScreen
import jp.ikigai.cash.flow.ui.screens.listing.transactionsScreen
import jp.ikigai.cash.flow.ui.screens.migration.migrateCategoryScreen
import jp.ikigai.cash.flow.ui.screens.migration.migrateCounterPartyScreen
import jp.ikigai.cash.flow.ui.screens.migration.migrateMethodScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertAccountScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertCategoryScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertCounterPartyScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertMethodScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertTransactionScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertTransactionTemplateScreen

@Composable
fun BaseScreen() {
    val navController = rememberNavController()

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = Routes.Transactions.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                animationSpec = tween(Constants.TWEEN_DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                animationSpec = tween(Constants.TWEEN_DURATION)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(Constants.TWEEN_DURATION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(Constants.TWEEN_DURATION)
            )
        }
    ) {
        auditLogsScreen(navController = navController)

        settingsScreen(navController = navController)

        exportTransactionsScreen(navController = navController)

        importBackupScreen(navController = navController)

        chartsScreen(navController = navController)

        transactionsScreen(navController = navController)
        upsertTransactionScreen(navController = navController)

        transactionTemplateScreen(navController = navController)
        upsertTransactionTemplateScreen(navController = navController)

        categoryScreen(navController = navController)
        upsertCategoryScreen(navController = navController)
        migrateCategoryScreen(navController = navController)

        counterPartyScreen(navController = navController)
        upsertCounterPartyScreen(navController = navController)
        migrateCounterPartyScreen(navController = navController)

        methodScreen(navController = navController)
        upsertMethodScreen(navController = navController)
        migrateMethodScreen(navController = navController)

        accountScreen(navController = navController)
        upsertAccountScreen(navController = navController)
    }
}