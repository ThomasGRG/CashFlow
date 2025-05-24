package jp.ikigai.cash.flow.koin

import jp.ikigai.cash.flow.ui.viewmodels.common.ExportTransactionsScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.common.ImportBackupScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.common.SettingsScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.AccountScreenViewModel
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
    viewModel() { TransactionsScreenViewModel() }
    viewModel() { UpsertTransactionScreenViewModel(get()) }

    viewModel() { CounterPartyScreenViewModel() }
    viewModel() { UpsertCounterPartyScreenViewModel(get()) }
    viewModel() { MigrateCounterPartyScreenViewModel(get()) }

    viewModel() { AccountScreenViewModel() }
    viewModel() { UpsertAccountScreenViewModel(get()) }

    viewModel() { MethodScreenViewModel() }
    viewModel() { UpsertMethodScreenViewModel(get()) }
    viewModel() { MigrateMethodScreenViewModel(get()) }

    viewModel() { CategoryScreenViewModel() }
    viewModel() { UpsertCategoryScreenViewModel(get()) }
    viewModel() { MigrateCategoryScreenViewModel(get()) }

    viewModel() { SettingsScreenViewModel() }
    viewModel() { ExportTransactionsScreenViewModel() }
    viewModel() { ImportBackupScreenViewModel() }

    viewModel() { TransactionTemplateScreenViewModel() }
    viewModel() { UpsertTransactionTemplateScreenViewModel(get()) }
}