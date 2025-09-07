package jp.ikigai.cash.flow.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
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