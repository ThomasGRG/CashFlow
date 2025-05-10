package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.SelectTemplateInfoDTO
import jp.ikigai.cash.flow.data.dto.TransactionDetailsByDay
import jp.ikigai.cash.flow.data.dto.TransactionScreenFlows
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsScreenState
import jp.ikigai.cash.flow.ui.screenStates.listing.transactions.FiltersState
import jp.ikigai.cash.flow.ui.screenStates.listing.transactions.TransactionsScreenState
import jp.ikigai.cash.flow.utils.combineFiveFlows
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.toEpochMilli
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class TransactionsScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val datePattern = "dd-LLL-yyyy"

    private var numberFormatter = getNumberFormatter()

    private var currencyFormatterMap = getCurrencyFormatterMap()
    private var currencyFormatter = currencyFormatterMap.getValue("INR")

    private val _state = MutableStateFlow(TransactionsScreenState())
    val state: StateFlow<TransactionsScreenState> = _state.asStateFlow()

    private val _filtersState = MutableStateFlow(FiltersState())
    val filtersState: StateFlow<FiltersState> = _filtersState.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(
        SortOptionsScreenState(Sort.DESCENDING, "time")
    )
    val sortOptionsState: StateFlow<SortOptionsScreenState> = _sortOptionsState.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val templateQuery = realm
        .query<TransactionTemplate>()
        .sort("frequency", Sort.DESCENDING)

    private val sourceQuery = realm
        .query<Source>()
        .sort("frequency", Sort.DESCENDING)

    private val methodQuery = realm
        .query<Method>()
        .sort("frequency", Sort.DESCENDING)

    private val counterPartyQuery = realm
        .query<CounterParty>()
        .sort("frequency", Sort.DESCENDING)

    private val categoryQuery = realm
        .query<Category>()
        .sort("frequency", Sort.DESCENDING)

    init {
        _filtersState.update {
            it.copy(
                startDateString = it.startDate.getDateString(datePattern),
                endDateString = it.endDate.getDateString(datePattern)
            )
        }
        loadData()
        loadBalance()
        loadTransactions()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
        _event.close()
    }

    private fun loadData() = viewModelScope.launch {
        combineFiveFlows(
            categoryQuery.asFlow(),
            counterPartyQuery.asFlow(),
            methodQuery.asFlow(),
            sourceQuery.asFlow(),
            templateQuery.asFlow()
        ) { categoryChanges, counterPartyChanges, methodChanges, sourceChanges, templateChanges ->
            TransactionScreenFlows(
                categories = categoryChanges.list,
                counterParties = counterPartyChanges.list,
                methods = methodChanges.list,
                sources = sourceChanges.list,
                templates = templateChanges.list
            )
        }.collectLatest { transactionScreenFlows ->
            val sources = transactionScreenFlows.sources.toMutableList()

            sources.forEach { source ->
                val formatter = currencyFormatterMap.getValue(source.currency)
                source.displayBalance = formatter.format(source.balance).toString()
            }

            _state.update {
                it.copy(
                    categories = transactionScreenFlows.categories,
                    counterParties = transactionScreenFlows.counterParties,
                    methods = transactionScreenFlows.methods,
                    sources = sources,
                    templates = mapToTemplateDTO(transactionScreenFlows.templates)
                )
            }

            _filtersState.update {
                val selectedCategories = getSelectedCategories(
                    transactionScreenFlows.categories,
                    it.selectedCategories
                )
                val selectedCounterParties = getSelectedCounterParties(
                    transactionScreenFlows.counterParties,
                    it.selectedCounterParties
                )
                val selectedMethods = getSelectedMethods(
                    transactionScreenFlows.methods,
                    it.selectedMethods
                )
                val selectedSources = getSelectedSources(
                    transactionScreenFlows.sources,
                    it.selectedSources
                )

                val selectedCategoryCount = selectedCategories.filter { entry -> entry.value }.size
                val selectedCounterPartyCount =
                    selectedCounterParties.filter { entry -> entry.value }.size
                val selectedMethodCount = selectedMethods.filter { entry -> entry.value }.size
                val selectedSourceCount = selectedSources.filter { entry -> entry.value }.size

                it.copy(
                    selectedCategories = selectedCategories,
                    selectedCategoryCount = numberFormatter.format(selectedCategoryCount)
                        .toString(),
                    selectedCounterParties = selectedCounterParties,
                    selectedCounterPartyCount = numberFormatter.format(selectedCounterPartyCount)
                        .toString(),
                    selectedMethods = selectedMethods,
                    selectedMethodCount = numberFormatter.format(selectedMethodCount).toString(),
                    selectedSources = selectedSources,
                    selectedSourceCount = numberFormatter.format(selectedSourceCount).toString()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadBalance() = viewModelScope.launch {
        filtersState.flatMapLatest {
            realm.query<Source>("currency==$0", it.selectedCurrency).asFlow()
        }.collectLatest { sourceChanges ->
            val totalBalance = sourceChanges.list.sumOf { source -> source.balance }
            _state.update {
                it.copy(
                    balance = currencyFormatter.format(totalBalance).toString()
                )
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun loadTransactions() = viewModelScope.launch {
        combine(
            _searchState
                .onEach {
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
                .debounce(300),
            _sortOptionsState
                .onEach {
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                },
            _filtersState
                .onEach {
                    _state.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
        ) { searchText, sortOptions, filters ->
            Triple(searchText, sortOptions, filters)
        }.flatMapLatest { (searchText, sortOptions, filters) ->
            getTransactionQuery(
                searchText,
                sortOptions,
                filters
            )
        }.collectLatest { transactionChanges ->
            val incomeTransactions = transactionChanges.list
                .filter { it.type == TransactionType.CREDIT }
            val income = incomeTransactions.sumOf { it.amount }

            val expenseTransactions = transactionChanges.list
                .filter { it.type == TransactionType.DEBIT }
            val expense = expenseTransactions.sumOf { it.amount }

            _state.update {
                it.copy(
                    transactionsHashCode = transactionChanges.list.hashCode(),
                    transactions = getTransactionsMap(
                        transactionChanges.list,
                        searchState.value
                    ),
                    expenseTransactionsCount = numberFormatter.format(expenseTransactions.size)
                        .toString(),
                    expense = currencyFormatter.format(expense).toString(),
                    incomeTransactionsCount = numberFormatter.format(incomeTransactions.size)
                        .toString(),
                    income = currencyFormatter.format(income).toString(),
                    loading = false
                )
            }
        }
    }

    private fun getSelectedCategories(
        categories: List<Category>,
        selectedCategories: Map<String, Boolean>
    ): Map<String, Boolean> {
        return categories.associateBy(
            {
                it.uuid
            },
            {
                selectedCategories.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getSelectedCounterParties(
        counterParties: List<CounterParty>,
        selectedCounterParties: Map<String, Boolean>
    ): Map<String, Boolean> {
        return counterParties.associateBy(
            {
                it.uuid
            },
            {
                selectedCounterParties.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getSelectedMethods(
        methods: List<Method>,
        selectedMethods: Map<String, Boolean>
    ): Map<String, Boolean> {
        return methods.associateBy(
            {
                it.uuid
            },
            {
                selectedMethods.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getSelectedSources(
        sources: List<Source>,
        selectedSources: Map<String, Boolean>
    ): Map<String, Boolean> {
        return sources.associateBy(
            {
                it.uuid
            },
            {
                selectedSources.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun mapToTemplateDTO(templates: List<TransactionTemplate>): List<SelectTemplateInfoDTO> {
        return templates.map { template ->
            SelectTemplateInfoDTO(
                uuid = template.uuid,
                annotatedName = AnnotatedString(template.name),
                frequency = template.frequency.toString()
            )
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

    private fun getTransactionQuery(
        searchText: String,
        sortOptions: SortOptionsScreenState,
        filters: FiltersState
    ): Flow<ResultsChange<Transaction>> {
        var queryString =
            "time >= $0 && time <= $1 && currency==$2 && amount >= $3 && amount <= $4 && typeId IN $5 && category.uuid IN $6 && method.uuid IN $7 && source.uuid IN $8"
        if (searchText.isNotBlank()) {
            queryString += " && (title CONTAINS[c] '${searchText.trim()}' || description CONTAINS[c] '${searchText.trim()}')"
        }
        queryString += if (filters.includeNoCounterPartyTransactions) {
            " && (counterParty == nil || counterParty.uuid IN $9)"
        } else {
            " && counterParty.uuid IN $9"
        }
        return realm
            .query<Transaction>(
                queryString,
                filters.startDate.toEpochMilli(),
                filters.endDate.toEpochMilli(),
                filters.selectedCurrency,
                filters.filterAmountMin,
                if (filters.filterAmountMax <= filters.filterAmountMin) Double.MAX_VALUE else filters.filterAmountMax,
                filters.selectedTransactionTypes,
                filters.selectedCategories.filter { selectedCategory -> selectedCategory.value }.keys,
                filters.selectedMethods.filter { selectedMethods -> selectedMethods.value }.keys,
                filters.selectedSources.filter { selectedSources -> selectedSources.value }.keys,
                filters.selectedCounterParties.filter { selectedCounterParties -> selectedCounterParties.value }.keys
            )
            .sort("time", sortOptions.sortDirection)
            .asFlow()
    }

    private fun getTransactionsMap(
        transactions: List<Transaction>,
        searchText: String
    ): Map<LocalDate, TransactionDetailsByDay> {
        val transactionsMap = transactions.groupBy { it.time.toLocalDate() }
        val transactionDetailsMap = mutableMapOf<LocalDate, TransactionDetailsByDay>()
        transactionsMap.forEach { (localDate, transactionsList) ->
            val creditTransactions =
                transactionsList.filter { it.type == TransactionType.CREDIT }
            val credit = creditTransactions.sumOf { it.amount }

            val debitTransactions = transactionsList.filter { it.type == TransactionType.DEBIT }
            val debit = debitTransactions.sumOf { it.amount }

            transactionDetailsMap[localDate] = TransactionDetailsByDay(
                transactions = transactionsList.map { getTransactionWithIcons(it, searchText) },
                totalAmount = currencyFormatter.format(credit - debit).toString(),
            )
        }
        return transactionDetailsMap
    }

    private fun getTransactionWithIcons(
        transaction: Transaction,
        searchText: String
    ): TransactionWithIcons {
        val category = transaction.category!!
        val counterParty = transaction.counterParty
        val method = transaction.method!!
        val source = transaction.source!!
        val chips: MutableList<ChipInfo> = mutableListOf()
        if (counterParty != null) {
            chips.add(
                ChipInfo(
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    value = counterParty.name,
                    resId = R.string.placeholder
                )
            )
        }
        chips.add(
            ChipInfo(
                icon = category.icon,
                value = category.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = Constants.DEFAULT_METHOD_ICON,
                value = method.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = Constants.DEFAULT_SOURCE_ICON,
                value = source.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = TablerIcons.Alarm,
                value = transaction.time.format(DateTimeFormatter.ofPattern("hh:mm a")),
                resId = R.string.placeholder
            )
        )
        return TransactionWithIcons(
            uuid = transaction.uuid,
            annotatedTitle = getHighlightedString(transaction.title, searchText),
            annotatedDescription = getHighlightedString(transaction.description, searchText),
            amount = currencyFormatter.format(transaction.amount).toString(),
            typeIcon = transaction.type.icon,
            typeIconColor = transaction.type.color,
            currency = transaction.currency,
            chips = chips
        )
    }

    fun setCurrency(currency: String) {
        currencyFormatter = currencyFormatterMap.getValue(currency)
        _filtersState.update {
            it.copy(
                selectedCurrency = currency
            )
        }
    }

    fun setStartDateAndEndDate(startDate: ZonedDateTime, endDate: ZonedDateTime) {
        _filtersState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                startDateString = startDate.getDateString(datePattern),
                endDateString = endDate.getDateString(datePattern)
            )
        }
    }

    fun setSelectedCategories(selectedCategories: Map<String, Boolean>) {
        _filtersState.update {
            it.copy(
                selectedCategories = selectedCategories
            )
        }
    }

    fun setSelectedCounterParties(
        includeTransactionsWithNoCounterParty: Boolean,
        selectedCounterParties: Map<String, Boolean>
    ) {
        _filtersState.update {
            it.copy(
                selectedCounterParties = selectedCounterParties,
                includeNoCounterPartyTransactions = includeTransactionsWithNoCounterParty
            )
        }
    }

    fun setSelectedMethods(selectedMethods: Map<String, Boolean>) {
        _filtersState.update {
            it.copy(
                selectedMethods = selectedMethods
            )
        }
    }

    fun setSelectedSources(selectedSources: Map<String, Boolean>) {
        _filtersState.update {
            it.copy(
                selectedSources = selectedSources
            )
        }
    }

    fun setSelectedTransactionTypes(selectedTransactionTypes: List<Int>) {
        _filtersState.update {
            it.copy(
                selectedTransactionTypes = selectedTransactionTypes
            )
        }
    }

    fun setSortDirection(sortDirection: Sort) {
        _sortOptionsState.update {
            it.copy(
                sortDirection = sortDirection
            )
        }
    }

    fun setFilterAmounts(min: Double, max: Double) {
        val filterAmountRange = if (min > 0 && max > 0 && min < max) {
            "$min - $max"
        } else if (min > 0 && max == 0.0) {
            "$min+"
        } else if (min == 0.0 && max > 0) {
            "0 - $max"
        } else {
            "0+"
        }
        _filtersState.update {
            it.copy(
                filterAmountMin = min,
                filterAmountMax = max,
                filterAmountRange = filterAmountRange
            )
        }
    }

    fun setSearchText(searchText: String) {
        _searchState.update {
            searchText
        }
    }

    fun setLocale(locale: Locale?) {
        numberFormatter = getNumberFormatter(locale)
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        currencyFormatter = currencyFormatterMap.getValue(filtersState.value.selectedCurrency)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }

    fun cloneTransaction(transactionUUID: String, setCurrentDateTime: Boolean) =
        viewModelScope.launch {
            realm.write {
                val latestTransaction =
                    query<Transaction>("uuid==$0", transactionUUID).first().find()
                latestTransaction?.let {
                    val currentTime = ZonedDateTime.now(ZoneId.systemDefault())
                    val latestCategory = findLatest(latestTransaction.category!!)?.also {
                        it.frequency += 1
                        it.lastUsed = currentTime
                    }
                    val latestCounterParty = if (latestTransaction.counterParty != null) {
                        findLatest(latestTransaction.counterParty!!)?.also {
                            it.frequency += 1
                            it.lastUsed = currentTime
                        }
                    } else null
                    val latestMethod = findLatest(latestTransaction.method!!)?.also {
                        it.frequency += 1
                        it.lastUsed = currentTime
                    }
                    val latestSource = findLatest(latestTransaction.source!!)?.also {
                        it.frequency += 1
                        it.lastUsed = currentTime
                        if (latestTransaction.type == TransactionType.DEBIT) {
                            it.balance -= latestTransaction.amount
                        } else {
                            it.balance += latestTransaction.amount
                        }
                    }
                    val clone = Transaction().apply {
                        uuid = UUID.randomUUID().toString()
                        title = latestTransaction.title
                        description = latestTransaction.description
                        amount = latestTransaction.amount
                        type = latestTransaction.type
                        currency = latestTransaction.currency
                        if (!setCurrentDateTime) {
                            time = latestTransaction.time
                        }
                        category = latestCategory
                        counterParty = latestCounterParty
                        method = latestMethod
                        source = latestSource
                    }
                    copyToRealm(
                        instance = clone,
                        updatePolicy = UpdatePolicy.ALL
                    )
                }
            }
        }
}