package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import io.objectbox.query.QueryBuilder
import io.realm.kotlin.Realm
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.SelectTemplateInfoDTO
import jp.ikigai.cash.flow.data.dto.TransactionScreenFlows
import jp.ikigai.cash.flow.data.dto.TransactionWithChips
import jp.ikigai.cash.flow.data.dto.TransactionsWithTotalAmount
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.data.store.DataStore
import jp.ikigai.cash.flow.data.store.entity.Account
import jp.ikigai.cash.flow.data.store.entity.Account_
import jp.ikigai.cash.flow.data.store.entity.Category
import jp.ikigai.cash.flow.data.store.entity.Category_
import jp.ikigai.cash.flow.data.store.entity.CounterParty
import jp.ikigai.cash.flow.data.store.entity.CounterParty_
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.data.store.entity.Method_
import jp.ikigai.cash.flow.data.store.entity.Transaction
import jp.ikigai.cash.flow.data.store.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.store.entity.TransactionTemplate_
import jp.ikigai.cash.flow.data.store.entity.Transaction_
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsState
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
import kotlinx.coroutines.flow.onCompletion
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
    store: BoxStore = DataStore.store
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
        SortOptionsState<Transaction>(
            sortField = Transaction_.time
        )
    )
    val sortOptionsState: StateFlow<SortOptionsState<Transaction>> =
        _sortOptionsState.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val accountBox: Box<Account> = store.boxFor()
    private val categoryBox: Box<Category> = store.boxFor()
    private val counterPartyBox: Box<CounterParty> = store.boxFor()
    private val methodBox: Box<Method> = store.boxFor()
    private val templateBox: Box<TransactionTemplate> = store.boxFor()
    private val transactionBox: Box<Transaction> = store.boxFor()

    private val templateQuery = templateBox
        .query()
        .orderDesc(TransactionTemplate_.frequency)
        .build()

    private val accountQuery = accountBox
        .query()
        .orderDesc(Account_.frequency)
        .build()

    private val methodQuery = methodBox
        .query()
        .orderDesc(Method_.frequency)
        .build()

    private val counterPartyQuery = counterPartyBox
        .query()
        .orderDesc(CounterParty_.frequency)
        .build()

    private val categoryQuery = categoryBox
        .query()
        .orderDesc(Category_.frequency)
        .build()

    init {
        deleteRealm()
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
        accountQuery.close()
        categoryQuery.close()
        counterPartyQuery.close()
        methodQuery.close()
        templateQuery.close()
    }

    private fun deleteRealm() = viewModelScope.launch {
        Realm.deleteRealm(Database.config)
        _event.send(Event.SaveSuccess)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() = viewModelScope.launch {
        combineFiveFlows(
            accountQuery.flow(),
            categoryQuery.flow(),
            counterPartyQuery.flow(),
            methodQuery.flow(),
            templateQuery.flow()
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
            val query = accountBox
                .query(
                    Account_.currency.equal(it.selectedCurrency)
                )
                .build()
            query.flow().onCompletion {
                query.close()
            }
        }.collectLatest { accounts ->
            val totalBalance = accounts.sumOf { account -> account.balance }
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
        }.collectLatest { transactions ->
            val incomeTransactions = transactions
                .filter { it.type == TransactionType.CREDIT }
            val income = incomeTransactions.sumOf { it.amount }

            val expenseTransactions = transactions
                .filter { it.type == TransactionType.DEBIT }
            val expense = expenseTransactions.sumOf { it.amount }

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
        accounts: List<Account>,
        selectedAccounts: Map<Long, Boolean>
    ): Map<Long, Boolean> {
        return accounts.associateBy(
            {
                it.id
            },
            {
                selectedAccounts.getOrDefault(it.id, true)
            }
        )
    }

    private fun getSelectedCategories(
        categories: List<Category>,
        selectedCategories: Map<Long, Boolean>
    ): Map<Long, Boolean> {
        return categories.associateBy(
            {
                it.id
            },
            {
                selectedCategories.getOrDefault(it.id, true)
            }
        )
    }

    private fun getSelectedCounterParties(
        counterParties: List<CounterParty>,
        selectedCounterParties: Map<Long, Boolean>
    ): Map<Long, Boolean> {
        val counterPartyMap = counterParties
            .associateBy(
                {
                    it.id
                },
                {
                    selectedCounterParties.getOrDefault(it.id, true)
                }
            )
            .toMutableMap()
        counterPartyMap[0L] = selectedCounterParties.getOrDefault(0L, true)
        return counterPartyMap
    }

    private fun getSelectedMethods(
        methods: List<Method>,
        selectedMethods: Map<Long, Boolean>
    ): Map<Long, Boolean> {
        return methods.associateBy(
            {
                it.id
            },
            {
                selectedMethods.getOrDefault(it.id, true)
            }
        )
    }

    private fun mapToTemplateDTO(templates: List<TransactionTemplate>): List<SelectTemplateInfoDTO> {
        return templates.map { template ->
            SelectTemplateInfoDTO(
                id = template.id,
                annotatedName = AnnotatedString(template.name),
                frequency = template.frequency.toString()
            )
        }
    }

    fun canAddTransaction(): Boolean {
        val accountEmpty = accountBox.isEmpty
        val categoryEmpty = categoryBox.isEmpty
        val methodEmpty = methodBox.isEmpty
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTransactionQuery(
        searchText: String,
        sortOptions: SortOptionsState<Transaction>,
        filters: FiltersState
    ): Flow<MutableList<Transaction>> {
        val transactionQueryBuilder = transactionBox
            .query(
                Transaction_.time.between(
                    filters.startDate.toEpochMilli(),
                    filters.endDate.toEpochMilli()
                )
                    .and(
                        Transaction_.currency.equal(filters.selectedCurrency)
                    )
                    .and(
                        Transaction_.amount.between(
                            filters.filterAmountMin,
                            if (filters.filterAmountMax <= filters.filterAmountMin) Double.MAX_VALUE else filters.filterAmountMax
                        )
                    )
                    .and(
                        Transaction_.type.oneOf(
                            filters.selectedTransactionTypes.toIntArray()
                        )
                    )
                    .and(
                        Transaction_.accountId.oneOf(
                            filters.selectedAccounts.filter { it.value }.keys.toLongArray()
                        )
                    )
                    .and(
                        Transaction_.categoryId.oneOf(
                            filters.selectedCategories.filter { it.value }.keys.toLongArray()
                        )
                    )
                    .and(
                        Transaction_.counterPartyId.oneOf(
                            filters.selectedCounterParties.filter { it.value }.keys.toLongArray()
                        )
                    )
                    .and(
                        Transaction_.methodId.oneOf(
                            filters.selectedMethods.filter { it.value }.keys.toLongArray()
                        )
                    )
            )
        val query = if (searchText.isNotBlank()) {
            transactionQueryBuilder
                .and()
                .apply(
                    Transaction_.title.contains(
                        searchText.trim(),
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    )
                        .or(
                            Transaction_.description.contains(
                                searchText.trim(),
                                QueryBuilder.StringOrder.CASE_INSENSITIVE
                            )
                        )
                )
                .order(sortOptions.sortField, sortOptions.sortFlags)
                .build()
        } else {
            transactionQueryBuilder
                .order(sortOptions.sortField, sortOptions.sortFlags)
                .build()
        }
        return query.flow().onCompletion {
            query.close()
        }
    }

    private fun getTransactionsMap(
        transactions: List<Transaction>,
        searchText: String
    ): Map<LocalDate, TransactionsWithTotalAmount> {
        val transactionsMap = transactions.groupBy { it.time.toLocalDate() }
        val transactionDetailsMap = mutableMapOf<LocalDate, TransactionsWithTotalAmount>()
        transactionsMap.forEach { (localDate, transactionsList) ->
            val creditTransactions =
                transactionsList.filter { it.type == TransactionType.CREDIT }
            val credit = creditTransactions.sumOf { it.amount }

            val debitTransactions = transactionsList.filter { it.type == TransactionType.DEBIT }
            val debit = debitTransactions.sumOf { it.amount }

            transactionDetailsMap[localDate] = TransactionsWithTotalAmount(
                transactions = transactionsList.map { getTransactionWithChips(it, searchText) },
                totalAmount = currencyFormatter.format(credit - debit).toString(),
            )
        }
        return transactionDetailsMap
    }

    private fun getTransactionWithChips(
        transaction: Transaction,
        searchText: String
    ): TransactionWithChips {
        val account = transaction.account.target
        val category = transaction.category.target
        val counterParty = transaction.counterParty.target
        val method = transaction.method.target

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
                icon = Constants.DEFAULT_ACCOUNT_ICON,
                value = account.name,
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
        return TransactionWithChips(
            id = transaction.id,
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

    fun setSelectedCounterParties(selectedCounterParties: Map<Long, Boolean>) {
        _filtersState.update {
            it.copy(
                selectedCounterParties = selectedCounterParties,
                selectedCounterPartyCount = numberFormatter.format(
                    selectedCounterParties.count { entry -> entry.value }
                ).toString()
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

    fun setSelectedTransactionTypes(selectedTransactionTypes: List<Int>) {
        _filtersState.update {
            it.copy(
                selectedTransactionTypes = selectedTransactionTypes
            )
        }
    }

    fun setSortFlags(sortFlags: Int) {
        _sortOptionsState.update {
            it.copy(
                sortFlags = sortFlags
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
        val transactionQuery = transactionBox
            .query(Transaction_.id.equal(transactionId))
            .build()

        transactionQuery.findUnique()?.let { transaction ->
            val clone = Transaction(
                title = transaction.title,
                description = transaction.description,
                amount = transaction.amount,
                type = transaction.type,
                currency = transaction.currency,
                time = if (!setCurrentDateTime) transaction.time else ZonedDateTime.now(ZoneId.systemDefault())
            )
            clone.account.targetId = transaction.account.targetId
            clone.category.targetId = transaction.category.targetId
            clone.counterParty.targetId = transaction.counterParty.targetId
            clone.method.targetId = transaction.method.targetId
            transactionBox.put(clone)
            transaction.account.target.let { account ->
                accountBox.put(
                    account.copy(
                        frequency = account.frequency + 1,
                        lastUsed = clone.time
                    )
                )
            }
            transaction.category.target.let { category ->
                categoryBox.put(
                    category.copy(
                        frequency = category.frequency + 1,
                        lastUsed = clone.time
                    )
                )
            }
            transaction.counterParty.target?.let { counterParty ->
                counterPartyBox.put(
                    counterParty.copy(
                        frequency = counterParty.frequency + 1,
                        lastUsed = clone.time
                    )
                )
            }
            transaction.method.target.let { method ->
                methodBox.put(
                    method.copy(
                        frequency = method.frequency + 1,
                        lastUsed = clone.time
                    )
                )
            }
        }

        transactionQuery.close()
    }
}