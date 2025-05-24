package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.store.DataStore
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Account_
import jp.ikigai.cash.flow.data.store.entity.Transaction
import jp.ikigai.cash.flow.data.store.entity.Transaction_
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertAccountScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UpsertAccountScreenViewModel(
    savedStateHandle: SavedStateHandle,
    store: BoxStore = DataStore.store
) : ViewModel() {

    private val accountId: Long = checkNotNull(savedStateHandle["id"])

    private var getAccountJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertAccountScreenState())
    val state: StateFlow<UpsertAccountScreenState> = _state.asStateFlow()

    private val accountBox: Box<Account> = store.boxFor()
    private val transactionBox: Box<Transaction> = store.boxFor()

    private val nameAlreadyInUseQuery = accountBox
        .query(Account_.name.equal("", QueryBuilder.StringOrder.CASE_INSENSITIVE))
        .build()

    init {
        if (accountId > 0) {
            getAccountJob = getAccount()
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
        nameAlreadyInUseQuery.close()
    }

    private fun checkTransactionCount() = viewModelScope.launch {
        val transactionCountQuery = transactionBox
            .query(Transaction_.accountId.equal(accountId))
            .build()

        transactionCountQuery.count()
            .let { count ->
                _state.update {
                    it.copy(
                        hasTransactions = count > 0
                    )
                }
            }

        transactionCountQuery.close()
    }

    private fun getAccount() = viewModelScope.launch {
        val getAccountQuery = accountBox.query(Account_.id.equal(accountId)).build()

        getAccountQuery.findUnique()?.let { account ->
            _state.update {
                it.copy(
                    account = account,
                    name = account.name,
                    loading = false,
                    enabled = true
                )
            }
        }

        getAccountQuery.close()
    }

    fun checkNameAlreadyInUse(name: String) = viewModelScope.launch {
        if (name.isNotBlank() && name.trim() != state.value.account.name) {
            nameAlreadyInUseQuery
                .setParameter(Account_.name, name.trim())
                .count()
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
                loading = name.isNotBlank()
            )
        }
    }

    fun upsertSource(
        newName: String,
        newCurrency: String,
        newBalance: Double
    ) = viewModelScope.launch {
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
            getAccountJob?.cancel()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val account = state.value.account

            try {
                accountBox.put(
                    account.copy(
                        name = newName,
                        currency = newCurrency,
                        balance = newBalance
                    )
                )
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

    fun deleteSource() = viewModelScope.launch {
        if (accountId > 0) {
            getAccountJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val transactionsQuery = transactionBox
                .query(Transaction_.accountId.equal(accountId))
                .build()

            val transactions = transactionsQuery.find()

            transactionsQuery.close()

            transactionBox.remove(transactions)

            val deleted = accountBox.remove(accountId)

            if (deleted) {
                _event.send(Event.DeleteSuccess)
            } else {
                _event.send(Event.InternalError)
            }

            _state.update {
                it.copy(
                    loading = false
                )
            }
        }
    }

    fun hasChanges(
        balanceString: String,
        selectedCurrency: String
    ): Boolean {
        val balance = balanceString.toDoubleOrNull()
        val account = state.value.account

        val nameChanged = account.name != state.value.name
        val balanceChanged = balance == null || account.balance != balance
        val currencyChanged = account.currency != selectedCurrency

        return nameChanged || balanceChanged || currencyChanged
    }
}