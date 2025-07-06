package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.compose.ui.graphics.vector.ImageVector
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
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertCategoryScreenState
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

class UpsertCategoryScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()

    private val categoryId: Long = checkNotNull(savedStateHandle["id"])

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertCategoryScreenState())
    val state: StateFlow<UpsertCategoryScreenState> = _state.asStateFlow()

    init {
        if (categoryId > 0) {
            getCategory()
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
            .categoryWithTransactionMetadataQueries
            .getTransactionCount(categoryId)
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

    private fun getCategory() = viewModelScope.launch {
        database
            .categoryQueries
            .getById(categoryId)
            .executeAsOne()
            .let { category ->
                _state.update {
                    it.copy(
                        category = category,
                        name = category.categoryName,
                        selectedIcon = category.icon,
                        loading = false,
                        enabled = true
                    )
                }
            }
    }

    fun checkNameAlreadyInUse(name: String) = viewModelScope.launch {
        if (name.isNotBlank() && name.trim() != state.value.category.categoryName) {
            database
                .categoryQueries
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
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            try {
                if (categoryId == 0L) {
                    database
                        .categoryQueries
                        .insert(
                            categoryName = newName,
                            icon = newIcon
                        )
                } else {
                    database
                        .categoryQueries
                        .update(
                            categoryName = newName,
                            icon = newIcon,
                            categoryId = categoryId
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

    fun deleteCategory() = viewModelScope.launch {
        if (categoryId > 0) {
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
                        .getTransactionsForCategoryId(categoryId)
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
                        .categoryQueries
                        .delete(categoryId)
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