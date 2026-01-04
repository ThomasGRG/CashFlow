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

package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertMethodScreenState
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class UpsertMethodScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()

    private val methodId: Long = checkNotNull(savedStateHandle["id"])

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertMethodScreenState())
    val state: StateFlow<UpsertMethodScreenState> = _state.asStateFlow()

    init {
        if (methodId > 0) {
            getMethod()
            getTransactionCount()
        } else {
            _state.update {
                it.copy(
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _event.close()
    }

    private fun getTransactionCount() = viewModelScope.launch {
        database
            .methodWithTransactionMetadataQueries
            .getTransactionCount(methodId)
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .collectLatest { transactionCount ->
                _state.update {
                    it.copy(
                        transactionCount = transactionCount,
                        formattedTransactionCount = numberFormatter.format(transactionCount)
                            .toString()
                    )
                }
            }
    }

    private fun getMethod() = viewModelScope.launch {
        database
            .methodQueries
            .getById(methodId)
            .executeAsOne()
            .let { method ->
                _state.update {
                    it.copy(
                        method = method,
                        name = method.methodName,
                        loading = false,
                        enabled = true
                    )
                }
            }
    }

    fun checkNameAlreadyInUse(name: String) = viewModelScope.launch {
        if (name.isNotBlank() && name.trim() != state.value.method.methodName) {
            database
                .methodQueries
                .checkNameAlreadyInUse(
                    name.trim()
                )
                .executeAsOne()
                .let { count ->
                    _state.update {
                        it.copy(
                            nameValid = if (count > 0) false else it.nameValid,
                            nameErrorStringRes = if (count > 0) R.string.name_in_use_label else R.string.name_empty_error_label,
                            loading = false
                        )
                    }
                }
        } else {
            _state.update {
                it.copy(
                    loading = false
                )
            }
        }
    }

    fun setName(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameValid = name.isNotBlank(),
                nameErrorStringRes = R.string.name_empty_error_label,
                hasUnsavedChanges = it.method.methodName != name,
                loading = name.isNotBlank()
            )
        }
    }

    fun setLocale(locale: Locale?) {
        numberFormatter = getNumberFormatter(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }

    fun upsertMethod(newName: String) = viewModelScope.launch {
        if (newName.isBlank()) {
            _state.update {
                it.copy(
                    nameValid = false,
                    nameErrorStringRes = R.string.name_empty_error_label,
                )
            }
            return@launch
        }
        if (state.value.nameValid && !state.value.loading) {
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            try {
                if (methodId == 0L) {
                    database
                        .methodQueries
                        .insert(
                            methodName = newName
                        )
                } else {
                    database
                        .methodQueries
                        .update(
                            methodName = newName,
                            methodId = methodId
                        )
                }
                _event.send(Event.SaveSuccess)
            } catch (e: Exception) {
                _event.send(Event.InternalError)
            }

            _state.update {
                it.copy(
                    loading = false
                )
            }
        }
    }

    fun deleteMethod() = viewModelScope.launch {
        if (methodId > 0) {
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            try {
                database.transaction {
                    val transactions = database
                        .transactionQueries
                        .getTransactionsForMethodId(methodId)
                        .executeAsList()

                    val accountsMap = database
                        .accountQueries
                        .getByIds(
                            transactions
                                .map { it.transactionAccountId }
                                .distinct()
                        )
                        .executeAsList()
                        .associateBy { it.accountId }

                    transactions
                        .groupBy { transaction -> accountsMap.getValue(transaction.transactionAccountId) }
                        .forEach { (account, transactions) ->
                            var newBalance = account.balance
                            transactions.forEach { transaction ->
                                if (transaction.transactionType == TransactionType.CREDIT) {
                                    newBalance -= transaction.transactionAmount
                                } else {
                                    newBalance += transaction.transactionAmount
                                }
                            }
                            database
                                .accountQueries
                                .updateBalance(
                                    balance = newBalance,
                                    accountId = account.accountId
                                )
                        }

                    database
                        .transactionQueries
                        .deleteByIds(
                            transactions.map { it.transactionId }
                        )

                    database
                        .methodQueries
                        .delete(methodId)
                }
                _event.send(Event.DeleteSuccess)
            } catch (e: Exception) {
                _event.send(Event.InternalError)
            }

            _state.update {
                it.copy(
                    loading = false
                )
            }
        }
    }
}