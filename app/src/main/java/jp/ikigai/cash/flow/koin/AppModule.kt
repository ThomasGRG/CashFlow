package jp.ikigai.cash.flow.koin

import jp.ikigai.cash.flow.ui.viewmodels.common.ChooseIconScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.CategoryScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.CounterPartyScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.ItemsScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.MethodScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.SourceScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.listing.TransactionsScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertCategoryScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertCounterPartyScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertMethodScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertSourceScreenViewModel
import jp.ikigai.cash.flow.ui.viewmodels.upsert.UpsertTransactionScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel() { TransactionsScreenViewModel() }
    viewModel() { UpsertTransactionScreenViewModel(get()) }

    viewModel() { CounterPartyScreenViewModel() }
    viewModel() { UpsertCounterPartyScreenViewModel(get()) }

    viewModel() { SourceScreenViewModel() }
    viewModel() { UpsertSourceScreenViewModel(get()) }

    viewModel() { MethodScreenViewModel() }
    viewModel() { UpsertMethodScreenViewModel(get()) }

    viewModel() { CategoryScreenViewModel() }
    viewModel() { UpsertCategoryScreenViewModel(get()) }

    viewModel() { ChooseIconScreenViewModel(get()) }
    viewModel() { ItemsScreenViewModel() }
}