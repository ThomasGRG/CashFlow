package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TemplateWithTransactionMetadata
import jp.ikigai.cash.flow.TransactionWithMetadata
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.SelectTemplateInfoDTO
import jp.ikigai.cash.flow.data.dto.TransactionScreenFlows
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.dto.TransactionsWithTotalAmount
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.transactions.FiltersState
import jp.ikigai.cash.flow.ui.screenStates.listing.transactions.TransactionsScreenState
import jp.ikigai.cash.flow.utils.combineFiveFlows
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter
import kotlinx.coroutines.Dispatchers
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

class TransactionsScreenViewModel(
    private val database: CashFlowDatabase = Database.database
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

    private val _sortConfigState = MutableStateFlow(
        SortConfigState(sortField = "transactionDateTime")
    )
    val sortConfigState: StateFlow<SortConfigState> = _sortConfigState.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

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
        _event.close()
    }

    private fun getAccountQuery(): Flow<List<AccountWithTransactionMetadata>> {
        return database
            .accountWithTransactionMetadataQueries
            .getAllAccountsSortedByTransactionCountDesc()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    private fun getCategoryQuery(): Flow<List<CategoryWithTransactionMetadata>> {
        return database
            .categoryWithTransactionMetadataQueries
            .getAllCategoriesSortedByTransactionCountDesc()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    private fun getCounterPartyQuery(): Flow<List<CounterPartyWithTransactionMetadata>> {
        return database
            .counterPartyWithTransactionMetadataQueries
            .getAllCounterPartiesSortedByTransactionCountDesc()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    private fun getMethodQuery(): Flow<List<MethodWithTransactionMetadata>> {
        return database
            .methodWithTransactionMetadataQueries
            .getAllMethodsSortedByTransactionCountDesc()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    private fun getTemplateQuery(): Flow<List<TemplateWithTransactionMetadata>> {
        return database
            .templateWithTransactionMetadataQueries
            .getAllTemplatesSortedByTransactionCountDesc()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    private fun loadData() = viewModelScope.launch {
        combineFiveFlows(
            getAccountQuery(),
            getCategoryQuery(),
            getCounterPartyQuery(),
            getMethodQuery(),
            getTemplateQuery()
        ) { accounts, categories, counterParties, methods, templates ->
            TransactionScreenFlows(
                accounts = accounts,
                categories = categories,
                counterParties = counterParties,
                methods = methods,
                templates = templates
            )
        }.collectLatest { transactionScreenFlows ->
            _state.update {
                it.copy(
                    accounts = transactionScreenFlows.accounts.map { account ->
                        val formatter = currencyFormatterMap.getValue(account.currency)
                        account.copy(
                            formattedBalance = formatter.format(account.balance).toString()
                        )
                    },
                    categories = transactionScreenFlows.categories,
                    counterParties = transactionScreenFlows.counterParties,
                    methods = transactionScreenFlows.methods,
                    templates = mapToTemplateDTO(transactionScreenFlows.templates)
                )
            }

            _filtersState.update {
                val selectedAccounts = getSelectedAccounts(
                    transactionScreenFlows.accounts,
                    it.selectedAccounts
                )
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

                val selectedAccountCount = selectedAccounts.filter { entry -> entry.value }.size
                val selectedCategoryCount = selectedCategories.filter { entry -> entry.value }.size
                val selectedCounterPartyCount =
                    selectedCounterParties.filter { entry -> entry.value }.size
                val selectedMethodCount = selectedMethods.filter { entry -> entry.value }.size

                it.copy(
                    selectedAccounts = selectedAccounts,
                    selectedAccountCount = numberFormatter.format(selectedAccountCount).toString(),
                    selectedCategories = selectedCategories,
                    selectedCategoryCount = numberFormatter.format(selectedCategoryCount)
                        .toString(),
                    selectedCounterParties = selectedCounterParties,
                    selectedCounterPartyCount = numberFormatter.format(selectedCounterPartyCount)
                        .toString(),
                    selectedMethods = selectedMethods,
                    selectedMethodCount = numberFormatter.format(selectedMethodCount).toString()
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadBalance() = viewModelScope.launch {
        filtersState.flatMapLatest {
            database
                .accountQueries
                .getTotalBalanceForCurrency(
                    currency = it.selectedCurrency,
                )
                .asFlow()
                .mapToOne(Dispatchers.IO)
        }.collectLatest { totalBalance ->
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
            _sortConfigState
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
        ) { searchText, sortConfig, filters ->
            Triple(searchText, sortConfig, filters)
        }.flatMapLatest { (searchText, sortConfig, filters) ->
            val selectedCounterPartyIds = filters.selectedCounterParties.filter { it.value }.keys
            database
                .transactionWithMetadataQueries
                .getTransactions(
                    searchText = searchText.trim(),
                    startDate = filters.startDate,
                    endDate = filters.endDate,
                    selectedCurrencies = listOf(filters.selectedCurrency),
                    minAmount = filters.filterAmountMin,
                    maxAmount = if (filters.filterAmountMax <= filters.filterAmountMin) Double.MAX_VALUE else filters.filterAmountMax,
                    selectedTypes = filters.selectedTransactionTypes,
                    selectedAccountIds = filters.selectedAccounts.filter { it.value }.keys,
                    selectedCategoryIds = filters.selectedCategories.filter { it.value }.keys,
                    selectedCounterPartyIds = if (filters.includeNoCounterPartyTransactions) {
                        selectedCounterPartyIds + 0L
                    } else {
                        selectedCounterPartyIds
                    },
                    selectedMethodIds = filters.selectedMethods.filter { it.value }.keys,
                    sortField = sortConfig.sortField,
                    sortDirection = sortConfig.sortDirection.name
                )
                .asFlow()
                .mapToList(Dispatchers.IO)
        }.collectLatest { transactions ->
            val incomeTransactions = transactions
                .filter { it.transactionType == TransactionType.CREDIT }
            val income = incomeTransactions.sumOf { it.transactionAmount }

            val expenseTransactions = transactions
                .filter { it.transactionType == TransactionType.DEBIT }
            val expense = expenseTransactions.sumOf { it.transactionAmount }

            _state.update {
                it.copy(
                    transactionsHashCode = transactions.hashCode(),
                    transactions = getTransactionsMap(
                        transactions,
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

    private fun getSelectedAccounts(
        accounts: List<AccountWithTransactionMetadata>,
        selectedAccounts: Map<Long, Boolean>
    ): Map<Long, Boolean> {
        return accounts.associateBy(
            {
                it.accountId
            },
            {
                selectedAccounts.getOrDefault(it.accountId, true)
            }
        )
    }

    private fun getSelectedCategories(
        categories: List<CategoryWithTransactionMetadata>,
        selectedCategories: Map<Long, Boolean>
    ): Map<Long, Boolean> {
        return categories.associateBy(
            {
                it.categoryId
            },
            {
                selectedCategories.getOrDefault(it.categoryId, true)
            }
        )
    }

    private fun getSelectedCounterParties(
        counterParties: List<CounterPartyWithTransactionMetadata>,
        selectedCounterParties: Map<Long, Boolean>
    ): Map<Long, Boolean> {
        val counterPartyMap = counterParties
            .associateBy(
                {
                    it.counterPartyId
                },
                {
                    selectedCounterParties.getOrDefault(it.counterPartyId, true)
                }
            )
            .toMutableMap()
        return counterPartyMap
    }

    private fun getSelectedMethods(
        methods: List<MethodWithTransactionMetadata>,
        selectedMethods: Map<Long, Boolean>
    ): Map<Long, Boolean> {
        return methods.associateBy(
            {
                it.methodId
            },
            {
                selectedMethods.getOrDefault(it.methodId, true)
            }
        )
    }

    private fun mapToTemplateDTO(templates: List<TemplateWithTransactionMetadata>): List<SelectTemplateInfoDTO> {
        return templates.map { template ->
            SelectTemplateInfoDTO(
                id = template.templateId,
                annotatedName = AnnotatedString(template.templateName),
                frequency = numberFormatter.format(template.transactionCount).toString()
            )
        }
    }

    fun canAddTransaction(): Boolean {
        val accountEmpty = database.accountQueries.count().executeAsOne() == 0L
        val categoryEmpty = database.categoryQueries.count().executeAsOne() == 0L
        val methodEmpty = database.methodQueries.count().executeAsOne() == 0L
        val canAddTransaction = !accountEmpty && !categoryEmpty && !methodEmpty
        if (!canAddTransaction) {
            showToastBarForRequiredFields(accountEmpty, methodEmpty, categoryEmpty)
        }
        return canAddTransaction
    }

    private fun showToastBarForRequiredFields(
        accountEmpty: Boolean,
        methodEmpty: Boolean,
        categoryEmpty: Boolean
    ) = viewModelScope.launch {
        if (accountEmpty && methodEmpty && categoryEmpty) {
            _event.send(Event.AccountCategoryMethodRequired)
        } else if (accountEmpty && methodEmpty) {
            _event.send(Event.AccountMethodRequired)
        } else if (categoryEmpty && methodEmpty) {
            _event.send(Event.CategoryMethodRequired)
        } else if (categoryEmpty && accountEmpty) {
            _event.send(Event.AccountCategoryRequired)
        } else if (categoryEmpty) {
            _event.send(Event.CategoryRequired)
        } else if (accountEmpty) {
            _event.send(Event.AccountRequired)
        } else if (methodEmpty) {
            _event.send(Event.MethodRequired)
        }
    }

    private fun getTransactionsMap(
        transactions: List<TransactionWithMetadata>,
        searchText: String
    ): Map<LocalDate, TransactionsWithTotalAmount> {
        val transactionsMap = transactions.groupBy { it.transactionDateTime.toLocalDate() }
        val transactionDetailsMap = mutableMapOf<LocalDate, TransactionsWithTotalAmount>()
        transactionsMap.forEach { (localDate, transactionsList) ->
            val creditTransactions =
                transactionsList.filter { it.transactionType == TransactionType.CREDIT }
            val credit = creditTransactions.sumOf { it.transactionAmount }

            val debitTransactions =
                transactionsList.filter { it.transactionType == TransactionType.DEBIT }
            val debit = debitTransactions.sumOf { it.transactionAmount }

            transactionDetailsMap[localDate] = TransactionsWithTotalAmount(
                transactions = transactionsList.map { getTransactionWithChips(it, searchText) },
                totalAmount = currencyFormatter.format(credit - debit).toString(),
            )
        }
        return transactionDetailsMap
    }

    private fun getTransactionWithChips(
        transaction: TransactionWithMetadata,
        searchText: String
    ): TransactionWithChips {
        val chips: MutableList<ChipInfo> = mutableListOf()

        if (transaction.transactionCounterPartyName != null) {
            chips.add(
                ChipInfo(
                    icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                    value = transaction.transactionCounterPartyName,
                    resId = R.string.placeholder
                )
            )
        }
        chips.add(
            ChipInfo(
                icon = transaction.transactionCategoryIcon!!,
                value = transaction.transactionCategoryName!!,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = Constants.DEFAULT_METHOD_ICON,
                value = transaction.transactionMethodName!!,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = Constants.DEFAULT_ACCOUNT_ICON,
                value = transaction.transactionAccountName!!,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = TablerIcons.Alarm,
                value = transaction.transactionDateTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                resId = R.string.placeholder
            )
        )
        return TransactionWithChips(
            id = transaction.transactionId,
            annotatedTitle = getHighlightedString(transaction.transactionTitle, searchText),
            annotatedDescription = getHighlightedString(
                transaction.transactionDescription,
                searchText
            ),
            amount = currencyFormatter.format(transaction.transactionAmount).toString(),
            typeIcon = transaction.transactionType.icon,
            typeIconColor = transaction.transactionType.color,
            currency = transaction.transactionCurrency,
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

    fun setSelectedAccounts(selectedAccounts: Map<Long, Boolean>) {
        _filtersState.update {
            it.copy(
                selectedAccounts = selectedAccounts,
                selectedAccountCount = numberFormatter.format(
                    selectedAccounts.count { entry -> entry.value }
                ).toString()
            )
        }
    }

    fun setSelectedCategories(selectedCategories: Map<Long, Boolean>) {
        _filtersState.update {
            it.copy(
                selectedCategories = selectedCategories,
                selectedCategoryCount = numberFormatter.format(
                    selectedCategories.count { entry -> entry.value }
                ).toString()
            )
        }
    }

    fun setSelectedCounterParties(
        selectedCounterParties: Map<Long, Boolean>,
        includeNoCounterPartyTransactions: Boolean
    ) {
        _filtersState.update {
            it.copy(
                selectedCounterParties = selectedCounterParties,
                selectedCounterPartyCount = numberFormatter.format(
                    selectedCounterParties.count { entry -> entry.value }
                ).toString(),
                includeNoCounterPartyTransactions = includeNoCounterPartyTransactions
            )
        }
    }

    fun setSelectedMethods(selectedMethods: Map<Long, Boolean>) {
        _filtersState.update {
            it.copy(
                selectedMethods = selectedMethods,
                selectedMethodCount = numberFormatter.format(
                    selectedMethods.count { entry -> entry.value }
                ).toString()
            )
        }
    }

    fun setSelectedTransactionTypes(selectedTransactionTypes: List<TransactionType>) {
        _filtersState.update {
            it.copy(
                selectedTransactionTypes = selectedTransactionTypes
            )
        }
    }

    fun setSortDirection(sortDirection: SortDirection) {
        _sortConfigState.update {
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

    fun cloneTransaction(
        transactionId: Long,
        setCurrentDateTime: Boolean
    ) = viewModelScope.launch {
        val transaction = database.transactionQueries.getById(transactionId).executeAsOne()

        val account = database
            .accountQueries
            .getById(
                transaction.transactionAccountId
            )
            .executeAsOne()

        val dateTime = if (setCurrentDateTime) {
            ZonedDateTime.now(ZoneId.systemDefault())
        } else {
            transaction.transactionDateTime
        }

        database
            .transactionQueries
            .insert(
                transactionTitle = transaction.transactionTitle,
                transactionDescription = transaction.transactionDescription,
                transactionAmount = transaction.transactionAmount,
                transactionCurrency = transaction.transactionCurrency,
                transactionType = transaction.transactionType,
                transactionDateTime = dateTime,
                transactionAccountId = transaction.transactionAccountId,
                transactionCategoryId = transaction.transactionCategoryId,
                transactionCounterPartyId = transaction.transactionCounterPartyId,
                transactionMethodId = transaction.transactionMethodId,
                transactionTemplateId = transaction.transactionTemplateId
            )

        database.accountQueries.updateBalance(
            balance = if (transaction.transactionType == TransactionType.DEBIT) {
                account.balance - transaction.transactionAmount
            } else {
                account.balance + transaction.transactionAmount
            },
            accountId = account.accountId
        )
    }
}