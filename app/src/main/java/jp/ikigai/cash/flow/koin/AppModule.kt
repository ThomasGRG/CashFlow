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

package jp.ikigai.cash.flow.koin

import jp.ikigai.cash.flow.data.preferences.CashFlowPreferencesDataStore
import jp.ikigai.cash.flow.ui.viewmodels.charts.ChartsScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.common.ExportTransactionsScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.common.ImportBackupScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.AccountScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.AuditLogsScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.CategoryScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.CounterPartyScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.MethodScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionTemplateScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionsScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.migration.MigrateCategoryScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.migration.MigrateCounterPartyScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.migration.MigrateMethodScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertAccountScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertCategoryScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertCounterPartyScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertMethodScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertTransactionScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertTransactionTemplateScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CashFlowPreferencesDataStore(get()) }

    viewModel() { AuditLogsScreenViewModel() }

    viewModel() { ChartsScreenViewModel(get()) }

    viewModel() { TransactionsScreenViewModel(get()) }
    viewModel() { UpsertTransactionScreenViewModel(get()) }

    viewModel() { CounterPartyScreenViewModel(get()) }
    viewModel() { UpsertCounterPartyScreenViewModel(get()) }
    viewModel() { MigrateCounterPartyScreenViewModel(get()) }

    viewModel() { AccountScreenViewModel(get()) }
    viewModel() { UpsertAccountScreenViewModel(get()) }

    viewModel() { MethodScreenViewModel(get()) }
    viewModel() { UpsertMethodScreenViewModel(get()) }
    viewModel() { MigrateMethodScreenViewModel(get()) }

    viewModel() { CategoryScreenViewModel(get()) }
    viewModel() { UpsertCategoryScreenViewModel(get()) }
    viewModel() { MigrateCategoryScreenViewModel(get()) }

    viewModel() { ExportTransactionsScreenViewModel() }
    viewModel() { ImportBackupScreenViewModel() }

    viewModel() { TransactionTemplateScreenViewModel(get()) }
    viewModel() { UpsertTransactionTemplateScreenViewModel(get()) }
}