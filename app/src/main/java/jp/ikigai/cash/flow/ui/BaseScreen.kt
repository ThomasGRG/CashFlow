/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import jp.ikigai.cash.flow.data.TransactionsRoute
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
    val backStack = rememberNavBackStack(TransactionsRoute)

    NavDisplay(
        backStack = backStack,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            transactionsScreen(backStack = backStack)
            upsertTransactionScreen(backStack = backStack)

            transactionTemplateScreen(backStack = backStack)
            upsertTransactionTemplateScreen(backStack = backStack)

            accountScreen(backStack = backStack)
            upsertAccountScreen(backStack = backStack)

            categoryScreen(backStack = backStack)
            upsertCategoryScreen(backStack = backStack)
            migrateCategoryScreen(backStack = backStack)

            counterPartyScreen(backStack = backStack)
            upsertCounterPartyScreen(backStack = backStack)
            migrateCounterPartyScreen(backStack = backStack)

            methodScreen(backStack = backStack)
            upsertMethodScreen(backStack = backStack)
            migrateMethodScreen(backStack = backStack)

            chartsScreen(backStack = backStack)

            auditLogsScreen(backStack = backStack)

            settingsScreen(backStack = backStack)

            exportTransactionsScreen(backStack = backStack)

            importBackupScreen(backStack = backStack)
        },
        transitionSpec = {
            ContentTransform(
                targetContentEnter = slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                ),
                initialContentExit = slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    targetOffset = { it / 2 },
                ),
            )
        },
        popTransitionSpec = {
            ContentTransform(
                targetContentEnter = slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    initialOffset = { it / 2 },
                ),
                initialContentExit = slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                ),
            )
        },
        predictivePopTransitionSpec = { swipeEdge ->
            ContentTransform(
                targetContentEnter = fadeIn(
                    initialAlpha = 0.6f,
                ),
                initialContentExit = slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    targetOffset = { it / 3 }
                ),
            )
        },
    )
}