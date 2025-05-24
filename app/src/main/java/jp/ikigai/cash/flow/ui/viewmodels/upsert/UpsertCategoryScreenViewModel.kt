package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.compose.ui.graphics.vector.ImageVector
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
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.Category_
import jp.ikigai.cash.flow.data.store.entity.Transaction
import jp.ikigai.cash.flow.data.store.entity.Transaction_
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertCategoryScreenState
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

class UpsertCategoryScreenViewModel(
    savedStateHandle: SavedStateHandle,
    store: BoxStore = DataStore.store
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()

    private val categoryId: Long = checkNotNull(savedStateHandle["id"])

    private var getCategoryJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertCategoryScreenState())
    val state: StateFlow<UpsertCategoryScreenState> = _state.asStateFlow()

    private val accountBox: Box<Account> = store.boxFor()
    private val categoryBox: Box<Category> = store.boxFor()
    private val transactionBox: Box<Transaction> = store.boxFor()

    private val nameAlreadyInUseQuery = categoryBox
        .query(Category_.name.equal("", QueryBuilder.StringOrder.CASE_INSENSITIVE))
        .build()

    init {
        if (categoryId > 0) {
            getCategoryJob = getCategory()
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
            .query(Transaction_.categoryId.equal(categoryId))
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

    private fun getCategory() = viewModelScope.launch {
        val getCategoryQuery = categoryBox.query(Category_.id.equal(categoryId)).build()

        getCategoryQuery.findUnique()?.let { category ->
            _state.update {
                it.copy(
                    category = category,
                    name = category.name,
                    selectedIcon = category.icon,
                    loading = false,
                    enabled = true
                )
            }
        }

        getCategoryQuery.close()
    }

    fun checkNameAlreadyInUse(name: String) = viewModelScope.launch {
        if (name.isNotBlank() && name.trim() != state.value.category.name) {
            nameAlreadyInUseQuery
                .setParameter(Category_.name, name.trim())
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

    fun setIcon(icon: ImageVector) {
        _state.update {
            it.copy(
                selectedIcon = icon
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

    fun upsertCategory(newIcon: ImageVector, newName: String) = viewModelScope.launch {
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
            getCategoryJob?.cancel() // otherwise enabled will be set to true after saving and the flow updates
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val category = state.value.category

            try {
                categoryBox.put(
                    category.copy(
                        name = newName,
                        icon = newIcon
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

    fun deleteCategory() = viewModelScope.launch {
        if (categoryId > 0) {
            getCategoryJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val transactionsQuery = transactionBox
                .query(Transaction_.categoryId.equal(categoryId))
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

            val deleted = categoryBox.remove(categoryId)

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