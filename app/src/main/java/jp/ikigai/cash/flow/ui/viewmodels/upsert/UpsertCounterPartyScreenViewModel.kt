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
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertCounterPartyScreenState
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

class UpsertCounterPartyScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()

    private val counterPartyId: Long = checkNotNull(savedStateHandle["id"])

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertCounterPartyScreenState())
    val state: StateFlow<UpsertCounterPartyScreenState> = _state.asStateFlow()

    init {
        if (counterPartyId > 0) {
            getCounterParty()
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
            .counterPartyWithTransactionMetadataQueries
            .getTransactionCount(counterPartyId)
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

    private fun getCounterParty() = viewModelScope.launch {
        database
            .counterPartyQueries
            .getById(counterPartyId)
            .executeAsOne()
            .let { counterParty ->
                _state.update {
                    it.copy(
                        counterParty = counterParty,
                        name = counterParty.counterPartyName,
                        loading = false,
                        enabled = true
                    )
                }
            }
    }

    fun checkNameAlreadyInUse(name: String) = viewModelScope.launch {
        if (name.isNotBlank() && name.trim() != state.value.counterParty.counterPartyName) {
            database
                .counterPartyQueries
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
                hasUnsavedChanges = it.counterParty.counterPartyName != name,
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

    fun upsertCounterParty(newName: String) = viewModelScope.launch {
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
                if (counterPartyId == 0L) {
                    database
                        .counterPartyQueries
                        .insert(
                            counterPartyName = newName
                        )
                } else {
                    database
                        .counterPartyQueries
                        .update(
                            counterPartyName = newName,
                            counterPartyId = counterPartyId
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

    fun deleteCounterParty() = viewModelScope.launch {
        if (counterPartyId > 0) {
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
                        .getTransactionsForCounterPartyId(counterPartyId)
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
                        .counterPartyQueries
                        .delete(counterPartyId)
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