package jp.ikigai.cash.flow.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import jp.ikigai.cash.flow.data.Routes
import jp.ikigai.cash.flow.ui.screens.common.chooseIconScreen
import jp.ikigai.cash.flow.ui.screens.listing.categoryScreen
import jp.ikigai.cash.flow.ui.screens.listing.counterPartyScreen
import jp.ikigai.cash.flow.ui.screens.listing.itemScreen
import jp.ikigai.cash.flow.ui.screens.listing.methodScreen
import jp.ikigai.cash.flow.ui.screens.listing.sourceScreen
import jp.ikigai.cash.flow.ui.screens.listing.transactionTemplateScreen
import jp.ikigai.cash.flow.ui.screens.listing.transactionsScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertCategoryScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertCounterPartyScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertMethodScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertSourceScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertTransactionScreen
import jp.ikigai.cash.flow.ui.screens.upsert.upsertTransactionTemplateScreen

@Composable
fun BaseScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Transactions.route,
        modifier = Modifier.fillMaxSize()
    ) {
        transactionsScreen(navController = navController)
        upsertTransactionScreen(navController = navController)

        transactionTemplateScreen(navController = navController)
        upsertTransactionTemplateScreen(navController = navController)

        chooseIconScreen(navController = navController)

        itemScreen(navController = navController)

        categoryScreen(navController = navController)
        upsertCategoryScreen(navController = navController)

        counterPartyScreen(navController = navController)
        upsertCounterPartyScreen(navController = navController)

        methodScreen(navController = navController)
        upsertMethodScreen(navController = navController)

        sourceScreen(navController = navController)
        upsertSourceScreen(navController = navController)
    }
}