package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.Filters
import jp.ikigai.cash.flow.data.dto.TransactionDetailsByDay
import jp.ikigai.cash.flow.data.dto.TransactionScreenFlows
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.listing.TransactionsScreenState
import jp.ikigai.cash.flow.utils.combineEightFlows
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

    private val templateQuery =
        realm.query<TransactionTemplate>().sort("frequency", Sort.DESCENDING)

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
        loadData()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() = viewModelScope.launch {
        combineEightFlows(
            categoryQuery.asFlow(),
            counterPartyQuery.asFlow(),
            itemsQuery.asFlow(),
            methodQuery.asFlow(),
            sourceQuery.asFlow(),
            templateQuery.asFlow(),
            state.flatMapLatest {
                realm.query<Source>("currency==$0", it.selectedCurrency).asFlow()
            },
            getTransactionQuery()
        ) { categoryChanges, counterPartyChanges, itemChanges, methodChanges, sourceChanges, templateChanges, balanceChanges, transactionChanges ->
            TransactionScreenFlows(
                categories = categoryChanges.list,
                counterParties = counterPartyChanges.list,
                methods = methodChanges.list,
                sources = sourceChanges.list,
                items = itemChanges.list,
                templates = templateChanges.list,
                balance = balanceChanges.list.sumOf { source -> source.balance },
                transactions = transactionChanges.list,
            )
        }.collectLatest { transactionScreenFlows ->
            val incomeTransactions =
                transactionScreenFlows.transactions.filter { it.type == TransactionType.CREDIT }
            val income = incomeTransactions.sumOf { it.amount }

            val expenseTransactions =
                transactionScreenFlows.transactions.filter { it.type == TransactionType.DEBIT }
            val expense = expenseTransactions.sumOf { it.amount }
            _state.update {
                it.copy(
                    transactions = getTransactionsMap(transactionScreenFlows.transactions),
                    expenseTransactionsCount = expenseTransactions.size,
                    expense = expense,
                    incomeTransactionsCount = incomeTransactions.size,
                    income = income,
                    loading = false,
                    balance = transactionScreenFlows.balance,
                    templates = transactionScreenFlows.templates,
                    filters = it.filters.copy(
                        categories = transactionScreenFlows.categories,
                        selectedCategories = it.filters.selectedCategories.ifEmpty { transactionScreenFlows.categories.map { category -> category.uuid } },
                        counterParties = transactionScreenFlows.counterParties,
                        selectedCounterParties = it.filters.selectedCounterParties.ifEmpty { transactionScreenFlows.counterParties.map { counterParty -> counterParty.uuid } },
                        items = transactionScreenFlows.items,
                        selectedItems = it.filters.selectedItems.ifEmpty { transactionScreenFlows.items.map { item -> item.uuid } },
                        methods = transactionScreenFlows.methods,
                        selectedMethods = it.filters.selectedMethods.ifEmpty { transactionScreenFlows.methods.map { method -> method.uuid } },
                        sources = transactionScreenFlows.sources,
                        selectedSources = it.filters.selectedSources.ifEmpty { transactionScreenFlows.sources.map { source -> source.uuid } }
                    )
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
            _event.send(Event.CategoryMethodSourceRequired)
        } else if (sourceEmpty && methodEmpty) {
            _event.send(Event.MethodSourceRequired)
        } else if (categoryEmpty && methodEmpty) {
            _event.send(Event.CategoryMethodRequired)
        } else if (categoryEmpty && sourceEmpty) {
            _event.send(Event.CategorySourceRequired)
        } else if (categoryEmpty) {
            _event.send(Event.CategoryRequired)
        } else if (sourceEmpty) {
            _event.send(Event.SourceRequired)
        } else if (methodEmpty) {
            _event.send(Event.MethodRequired)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTransactionQuery(): Flow<ResultsChange<Transaction>> {
        return state.flatMapLatest {
            var queryString =
                "time >= $0 && time <= $1 && currency==$2 && amount >= $3 && amount <= $4 && typeId IN $5 && category.uuid IN $6 && method.uuid IN $7 && source.uuid IN $8"
            queryString += if (it.filters.includeNoCounterPartyTransactions) {
                " && (counterParty == nil || counterParty.uuid IN $9)"
            } else {
                " && counterParty.uuid IN $9"
            }
            queryString += if (it.filters.includeNoItemTransactions) {
                " && (items.@count == 0 || items.item.uuid IN $10)"
            } else {
                " && items.item.uuid IN $10"
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
                it.filters.selectedItems
            ).sort("time", Sort.DESCENDING).asFlow()
        }
    }

    private fun getTransactionsMap(transactions: List<Transaction>): Map<LocalDate, TransactionDetailsByDay> {
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
        return transactionDetailsMap
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