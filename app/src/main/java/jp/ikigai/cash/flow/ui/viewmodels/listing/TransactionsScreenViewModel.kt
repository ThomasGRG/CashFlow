package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.Filters
import jp.ikigai.cash.flow.data.dto.TransactionDetailsByDay
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.data.screenStates.listing.TransactionsScreenState
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getEndOfDayInEpochMilli
import jp.ikigai.cash.flow.utils.getStartOfDayInEpochMilli
import jp.ikigai.cash.flow.utils.toLocalDate
import jp.ikigai.cash.flow.utils.toZonedDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionsScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionsScreenState())
    val state: StateFlow<TransactionsScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val sourceQuery = realm.query<Source>().sort("frequency", Sort.DESCENDING)

    private val methodQuery = realm.query<Method>().sort("frequency", Sort.DESCENDING)

    private val itemsQuery = realm.query<Item>().sort("frequency", Sort.DESCENDING)

    private val counterPartyQuery = realm.query<CounterParty>().sort("frequency", Sort.DESCENDING)

    private val categoryQuery = realm.query<Category>().sort("frequency", Sort.DESCENDING)

    init {
        _state.update {
            it.copy(
                startDateString = it.startDate.getDateString(),
                endDateString = it.endDate.getDateString()
            )
        }
        getCategoryFilterData()
        getCounterPartyFilterData()
        getMethodFilterData()
        getSourceFilterData()
        updateBalance()
        getTransactions()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getCategoryFilterData() = viewModelScope.launch {
        categoryQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    filters = it.filters.copy(
                        categories = changes.list,
                        selectedCategories = it.filters.selectedCategories.ifEmpty { changes.list.map { category -> category.uuid } }
                    ),
                )
            }
        }
    }

    private fun getCounterPartyFilterData() = viewModelScope.launch {
        counterPartyQuery.asFlow().collectLatest { changes ->
            val counterPartyUuids =
                changes.list.map { counterParty -> counterParty.uuid }.toMutableList()
            counterPartyUuids.add("")
            _state.update {
                it.copy(
                    filters = it.filters.copy(
                        counterParties = changes.list,
                        selectedCounterParties = it.filters.selectedCounterParties.ifEmpty { counterPartyUuids }
                    ),
                )
            }
        }
    }

    private fun getMethodFilterData() = viewModelScope.launch {
        methodQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    filters = it.filters.copy(
                        methods = changes.list,
                        selectedMethods = it.filters.selectedMethods.ifEmpty { changes.list.map { method -> method.uuid } }
                    ),
                )
            }
        }
    }

    private fun getSourceFilterData() = viewModelScope.launch {
        sourceQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    filters = it.filters.copy(
                        sources = changes.list,
                        selectedSources = it.filters.selectedSources.ifEmpty { changes.list.map { source -> source.uuid } }
                    ),
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun updateBalance() = viewModelScope.launch {
        state.flatMapLatest {
            realm.query<Source>("currency==$0", it.selectedCurrency).asFlow()
        }.collectLatest { changes ->
            _state.update {
                it.copy(
                    balance = changes.list.sumOf { source -> source.balance }
                )
            }
        }
    }

    fun canAddTransaction(): Boolean {
        val sourceEmpty = realm.query<Source>().count().find() == 0L
        val categoryEmpty = realm.query<Category>().count().find() == 0L
        val methodEmpty = realm.query<Method>().count().find() == 0L
        val canAddTransaction = !sourceEmpty && !categoryEmpty && !methodEmpty
        if (!canAddTransaction) {
            showToastBarForRequiredFields(sourceEmpty, methodEmpty, categoryEmpty)
        }
        return canAddTransaction
    }

    private fun showToastBarForRequiredFields(
        sourceEmpty: Boolean,
        methodEmpty: Boolean,
        categoryEmpty: Boolean
    ) = viewModelScope.launch {
        if (sourceEmpty && methodEmpty && categoryEmpty) {
            _event.send(Event.SourceCategoryMethodRequired)
        } else if (sourceEmpty && methodEmpty) {
            _event.send(Event.SourceMethodRequired)
        } else if (categoryEmpty && methodEmpty) {
            _event.send(Event.CategoryMethodRequired)
        } else if (categoryEmpty && sourceEmpty) {
            _event.send(Event.SourceCategoryRequired)
        } else if (categoryEmpty) {
            _event.send(Event.CategoryRequired)
        } else if (sourceEmpty) {
            _event.send(Event.SourceRequired)
        } else if (methodEmpty) {
            _event.send(Event.MethodRequired)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTransactions() = viewModelScope.launch {
        state.flatMapLatest {
            var queryString = "time >= $0 && time <= $1 && currency==$2 && amount >= $3 && amount <= $4 && typeId IN $5 && category.uuid IN $6 && method.uuid IN $7 && source.uuid IN $8"
            queryString += if (it.filters.selectedCounterParties.contains("")) {
                " && (counterParty == nil || counterParty.uuid IN $9)"
            } else {
                " && counterParty.uuid IN $9"
            }
            realm.query<Transaction>(
                queryString,
                it.startDate.getStartOfDayInEpochMilli(),
                it.endDate.getEndOfDayInEpochMilli(),
                it.selectedCurrency,
                it.filters.filterAmountMin,
                if (it.filters.filterAmountMax <= it.filters.filterAmountMin) Double.MAX_VALUE else it.filters.filterAmountMax,
                it.filters.selectedTransactionTypes,
                it.filters.selectedCategories,
                it.filters.selectedMethods,
                it.filters.selectedSources,
                it.filters.selectedCounterParties,
            ).sort("time", Sort.DESCENDING).asFlow()
        }.collectLatest { changes ->
            val transactions = changes.list
            val transactionsMap = transactions.groupBy { it.time.toLocalDate() }
            val transactionDetailsMap = mutableMapOf<LocalDate, TransactionDetailsByDay>()
            transactionsMap.forEach { (localDate, transactionsList) ->
                val creditTransactions =
                    transactionsList.filter { it.type == TransactionType.CREDIT }
                val credit = creditTransactions.sumOf { it.amount }

                val debitTransactions = transactionsList.filter { it.type == TransactionType.DEBIT }
                val debit = debitTransactions.sumOf { it.amount }

                transactionDetailsMap[localDate] = TransactionDetailsByDay(
                    transactions = transactionsList.map { getTransactionWithIcons(it) },
                    totalAmount = credit - debit,
                )
            }
            val incomeTransactions = transactions.filter { it.type == TransactionType.CREDIT }
            val income = incomeTransactions.sumOf { it.amount }

            val expenseTransactions = transactions.filter { it.type == TransactionType.DEBIT }
            val expense = expenseTransactions.sumOf { it.amount }
            _state.update {
                it.copy(
                    transactions = transactionDetailsMap,
                    expenseTransactionsCount = expenseTransactions.size,
                    expense = expense,
                    incomeTransactionsCount = incomeTransactions.size,
                    income = income,
                    loading = false,
                )
            }
        }
    }

    private fun getTransactionWithIcons(transaction: Transaction): TransactionWithIcons {
        val category = transaction.category!!
        val counterParty = transaction.counterParty
        val method = transaction.method!!
        val source = transaction.source!!
        val chips: MutableList<Pair<String, ImageVector>> = mutableListOf()
        if (counterParty != null) {
            chips.add(Pair(counterParty.name, counterParty.icon))
        }
        chips.add(Pair(category.name, category.icon))
        chips.add(Pair(method.name, method.icon))
        chips.add(Pair(source.name, source.icon))
        chips.add(
            Pair(
                transaction.time.toZonedDateTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                TablerIcons.Alarm
            )
        )
        val transactionItemSize = transaction.items.size
        return TransactionWithIcons(
            uuid = transaction.uuid,
            title = transaction.title,
            description = transaction.description,
            amount = transaction.amount,
            typeIcon = transaction.type.icon,
            typeIconColor = transaction.type.color,
            currency = transaction.currency,
            itemCount = transactionItemSize,
            chips = chips
        )
    }

    fun setCurrency(currency: String) {
        _state.update {
            it.copy(
                loading = true,
                selectedCurrency = currency
            )
        }
    }

    fun setStartDateAndEndDate(startDate: LocalDate, endDate: LocalDate) {
        _state.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                startDateString = startDate.getDateString(),
                endDateString = endDate.getDateString(),
                loading = true
            )
        }
    }

    fun setFilters(filters: Filters) {
        _state.update {
            it.copy(
                filters = filters
            )
        }
    }
}