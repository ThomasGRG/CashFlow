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
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertAccountScreenState
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

class UpsertAccountScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private val accountId: Long = checkNotNull(savedStateHandle["id"])

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertAccountScreenState())
    val state: StateFlow<UpsertAccountScreenState> = _state.asStateFlow()

    init {
        if (accountId > 0) {
            getAccount()
            checkTransactionCount()
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

    private fun checkTransactionCount() = viewModelScope.launch {
        database
            .accountWithTransactionMetadataQueries
            .getTransactionCount(accountId)
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .collectLatest { transactionCount ->
                _state.update {
                    it.copy(
                        hasTransactions = transactionCount > 0
                    )
                }
            }
    }

    private fun getAccount() = viewModelScope.launch {
        database
            .accountQueries
            .getById(accountId)
            .executeAsOne()
            .let { account ->
                _state.update {
                    it.copy(
                        account = account,
                        name = account.accountName,
                        balance = account.balance.toString(),
                        selectedCurrency = account.currency,
                        loading = false,
                        enabled = true
                    )
                }
            }
    }

    fun checkNameAlreadyInUse(name: String) = viewModelScope.launch {
        if (name.isNotBlank() && name.trim() != state.value.account.accountName) {
            database
                .accountQueries
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
                hasUnsavedChanges = getHasUnsavedChanges(name = name),
                loading = name.isNotBlank()
            )
        }
    }

    fun setBalance(balance: String) {
        _state.update {
            it.copy(
                balance = balance,
                balanceValid = balance.toDoubleOrNull() != null,
                hasUnsavedChanges = getHasUnsavedChanges(balanceString = balance)
            )
        }
    }

    fun setCurrency(currency: String) {
        _state.update {
            it.copy(
                selectedCurrency = currency,
                hasUnsavedChanges = getHasUnsavedChanges(selectedCurrency = currency)
            )
        }
    }

    fun upsertSource(
        newName: String,
        newCurrency: String,
        newBalance: Double?
    ) = viewModelScope.launch {
        if (state.value.loading) {
            return@launch
        }
        if (newName.isBlank()) {
            _state.update {
                it.copy(
                    nameValid = false,
                    nameErrorStringRes = R.string.name_empty_error_label,
                )
            }
            return@launch
        }
        if (newBalance == null) {
            _state.update {
                it.copy(
                    balanceValid = false
                )
            }
            return@launch
        }

        _state.update {
            it.copy(
                loading = true,
                enabled = false
            )
        }

        try {
            if (accountId == 0L) {
                database
                    .accountQueries
                    .insert(
                        accountName = newName,
                        balance = newBalance,
                        currency = newCurrency
                    )
            } else {
                database
                    .accountQueries
                    .update(
                        accountName = newName,
                        balance = newBalance,
                        currency = newCurrency,
                        accountId = accountId
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

    fun deleteSource() = viewModelScope.launch {
        if (accountId > 0) {
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
                        .getTransactionsForAccountId(accountId)
                        .executeAsList()

                    database
                        .transactionQueries
                        .deleteByIds(
                            transactions.map { it.transactionId }
                        )

                    database
                        .accountQueries
                        .delete(accountId)
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

    private fun getHasUnsavedChanges(
        name: String = state.value.name,
        balanceString: String = state.value.balance,
        selectedCurrency: String = state.value.selectedCurrency
    ): Boolean {
        val balance = balanceString.toDoubleOrNull()
        val account = state.value.account

        val nameChanged = account.accountName != name
        val balanceChanged = balance == null || account.balance != balance
        val currencyChanged = account.currency != selectedCurrency

        return nameChanged || balanceChanged || currencyChanged
    }
}