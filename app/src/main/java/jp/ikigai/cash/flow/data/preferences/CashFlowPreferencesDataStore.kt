package jp.ikigai.cash.flow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class CashFlowPreferencesDataStore(context: Context) {
    private val cashFlowPreferences: DataStore<CashFlowPreferences> =
        MultiProcessDataStoreFactory.create(
            serializer = CashFlowPreferencesSerializer(),
            produceFile = {
                File("${context.cacheDir.path}/cashflow.preferences_pb")
            },
            corruptionHandler = ReplaceFileCorruptionHandler {
                CashFlowPreferences()
            }
        )

    suspend fun restoreDefaults(selectedScreens: Map<Int, Boolean>) {
        cashFlowPreferences.updateData {
            it.copy(
                accountsScreenSortField = if (selectedScreens[R.string.accounts_screen_label] == true) {
                    "transactionCount"
                } else {
                    it.accountsScreenSortField
                },
                accountsScreenSortDirection = if (selectedScreens[R.string.accounts_screen_label] == true) {
                    SortDirection.DESC
                } else {
                    it.accountsScreenSortDirection
                },
                categoriesScreenSortField = if (selectedScreens[R.string.categories_screen_label] == true) {
                    "transactionCount"
                } else {
                    it.categoriesScreenSortField
                },
                categoriesScreenSortDirection = if (selectedScreens[R.string.categories_screen_label] == true) {
                    SortDirection.DESC
                } else {
                    it.categoriesScreenSortDirection
                },
                counterPartiesScreenSortField = if (selectedScreens[R.string.counter_parties_screen_label] == true) {
                    "transactionCount"
                } else {
                    it.counterPartiesScreenSortField
                },
                counterPartiesScreenSortDirection = if (selectedScreens[R.string.counter_parties_screen_label] == true) {
                    SortDirection.DESC
                } else {
                    it.categoriesScreenSortDirection
                },
                methodsScreenSortField = if (selectedScreens[R.string.methods_screen_label] == true) {
                    "transactionCount"
                } else {
                    it.methodsScreenSortField
                },
                methodsScreenSortDirection = if (selectedScreens[R.string.methods_screen_label] == true) {
                    SortDirection.DESC
                } else {
                    it.methodsScreenSortDirection
                },
                templatesScreenSortField = if (selectedScreens[R.string.templates_screen_label] == true) {
                    "transactionCount"
                } else {
                    it.templatesScreenSortField
                },
                templatesScreenSortDirection = if (selectedScreens[R.string.templates_screen_label] == true) {
                    SortDirection.DESC
                } else {
                    it.templatesScreenSortDirection
                },
                transactionsScreenSortField = if (selectedScreens[R.string.transactions_screen_label] == true) {
                    "transactionDateTime"
                } else {
                    it.transactionsScreenSortField
                },
                transactionsScreenSortDirection = if (selectedScreens[R.string.transactions_screen_label] == true) {
                    SortDirection.DESC
                } else {
                    it.transactionsScreenSortDirection
                },
            )
        }
    }

    fun getAccountsScreenSortConfig(): Flow<SortConfigState> {
        return cashFlowPreferences.data.map { preferences ->
            SortConfigState(
                sortField = preferences.accountsScreenSortField,
                sortDirection = preferences.accountsScreenSortDirection,
            )
        }
    }

    suspend fun saveAccountsScreenSortConfig(sortConfigState: SortConfigState) {
        cashFlowPreferences.updateData {
            it.copy(
                accountsScreenSortField = sortConfigState.sortField,
                accountsScreenSortDirection = sortConfigState.sortDirection,
            )
        }
    }

    fun getCategoriesScreenSortConfig(): Flow<SortConfigState> {
        return cashFlowPreferences.data.map { preferences ->
            SortConfigState(
                sortField = preferences.categoriesScreenSortField,
                sortDirection = preferences.categoriesScreenSortDirection,
            )
        }
    }

    suspend fun saveCategoriesScreenSortConfig(sortConfigState: SortConfigState) {
        cashFlowPreferences.updateData {
            it.copy(
                categoriesScreenSortField = sortConfigState.sortField,
                categoriesScreenSortDirection = sortConfigState.sortDirection,
            )
        }
    }

    fun getCounterPartiesScreenSortConfig(): Flow<SortConfigState> {
        return cashFlowPreferences.data.map { preferences ->
            SortConfigState(
                sortField = preferences.counterPartiesScreenSortField,
                sortDirection = preferences.counterPartiesScreenSortDirection,
            )
        }
    }

    suspend fun saveCounterPartiesScreenSortConfig(sortConfigState: SortConfigState) {
        cashFlowPreferences.updateData {
            it.copy(
                counterPartiesScreenSortField = sortConfigState.sortField,
                counterPartiesScreenSortDirection = sortConfigState.sortDirection,
            )
        }
    }

    fun getMethodsScreenSortConfig(): Flow<SortConfigState> {
        return cashFlowPreferences.data.map { preferences ->
            SortConfigState(
                sortField = preferences.methodsScreenSortField,
                sortDirection = preferences.methodsScreenSortDirection,
            )
        }
    }

    suspend fun saveMethodsScreenSortConfig(sortConfigState: SortConfigState) {
        cashFlowPreferences.updateData {
            it.copy(
                methodsScreenSortField = sortConfigState.sortField,
                methodsScreenSortDirection = sortConfigState.sortDirection,
            )
        }
    }

    fun getTemplatesScreenSortConfig(): Flow<SortConfigState> {
        return cashFlowPreferences.data.map { preferences ->
            SortConfigState(
                sortField = preferences.templatesScreenSortField,
                sortDirection = preferences.templatesScreenSortDirection,
            )
        }
    }

    suspend fun saveTemplatesScreenSortConfig(sortConfigState: SortConfigState) {
        cashFlowPreferences.updateData {
            it.copy(
                templatesScreenSortField = sortConfigState.sortField,
                templatesScreenSortDirection = sortConfigState.sortDirection,
            )
        }
    }

    fun getTransactionsScreenSortConfig(): Flow<SortConfigState> {
        return cashFlowPreferences.data.map { preferences ->
            SortConfigState(
                sortField = preferences.transactionsScreenSortField,
                sortDirection = preferences.transactionsScreenSortDirection,
            )
        }
    }

    suspend fun saveTransactionsScreenSortConfig(sortConfigState: SortConfigState) {
        cashFlowPreferences.updateData {
            it.copy(
                transactionsScreenSortField = sortConfigState.sortField,
                transactionsScreenSortDirection = sortConfigState.sortDirection,
            )
        }
    }
}