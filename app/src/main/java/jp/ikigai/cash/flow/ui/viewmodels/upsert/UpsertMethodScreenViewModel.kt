package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.data.store.DataStore
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.data.store.entity.Method_
import jp.ikigai.cash.flow.data.store.entity.Transaction
import jp.ikigai.cash.flow.data.store.entity.Transaction_
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertMethodScreenState
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class UpsertMethodScreenViewModel(
    savedStateHandle: SavedStateHandle,
    store: BoxStore = DataStore.store
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()

    private val methodId: Long = checkNotNull(savedStateHandle["id"])

    private var getMethodJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertMethodScreenState())
    val state: StateFlow<UpsertMethodScreenState> = _state.asStateFlow()

    private val accountBox: Box<Account> = store.boxFor()
    private val methodBox: Box<Method> = store.boxFor()
    private val transactionBox: Box<Transaction> = store.boxFor()

    private val nameAlreadyInUseQuery = methodBox
        .query(Method_.name.equal("", QueryBuilder.StringOrder.CASE_INSENSITIVE))
        .build()

    init {
        if (methodId > 0) {
            getMethodJob = getMethod()
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
        nameAlreadyInUseQuery.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTransactionCount() = viewModelScope.launch {
        val transactionCountQuery = transactionBox
            .query(Transaction_.methodId.equal(methodId))
            .build()

        transactionCountQuery
            .flow()
            .onCompletion {
                transactionCountQuery.close()
            }
            .collectLatest { transactions ->
                _state.update {
                    it.copy(
                        transactionCount = transactions.size,
                        formattedTransactionCount = numberFormatter.format(transactions.size)
                            .toString()
                    )
                }
            }
    }

    private fun getMethod() = viewModelScope.launch {
        val getMethodQuery = methodBox.query(Method_.id.equal(methodId)).build()

        getMethodQuery.findUnique()?.let { method ->
            _state.update {
                it.copy(
                    method = method,
                    name = method.name,
                    loading = false,
                    enabled = true
                )
            }
        }

        getMethodQuery.close()
    }

    fun checkNameAlreadyInUse(name: String) = viewModelScope.launch {
        if (name.isNotBlank() && name.trim() != state.value.method.name) {
            nameAlreadyInUseQuery
                .setParameter(Method_.name, name.trim())
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
            getMethodJob?.cancel()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val method = state.value.method

            try {
                methodBox.put(
                    method.copy(
                        name = newName
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

    fun deleteMethod() = viewModelScope.launch {
        if (methodId > 0) {
            getMethodJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val transactionsQuery = transactionBox
                .query(Transaction_.methodId.equal(methodId))
                .build()

            val transactions = transactionsQuery.find()

            transactionsQuery.close()

            val accounts: MutableList<Account> = mutableListOf()

            transactions
                .groupBy { transaction -> transaction.account.target }
                .forEach { (account, transactions) ->
                    var newBalance = account.balance
                    transactions.forEach { transaction ->
                        if (transaction.type == TransactionType.CREDIT) {
                            newBalance -= transaction.amount
                        } else {
                            newBalance += transaction.amount
                        }
                    }
                    accounts.add(
                        account.copy(
                            balance = newBalance
                        )
                    )
                }

            transactionBox.remove(transactions)

            accountBox.put(accounts)

            val deleted = methodBox.remove(methodId)

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
}