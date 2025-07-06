package jp.ikigai.cash.flow.ui.viewmodels.upsert

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.Transaction
import jp.ikigai.cash.flow.TransactionTemplate
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.UpsertTransactionFlows
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionScreenState
import jp.ikigai.cash.flow.utils.combineFiveFlows
import jp.ikigai.cash.flow.utils.combineSixFlows
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class UpsertTransactionScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private val datePattern = "EEEE, dd-LLL-yyyy"

    private val transactionId: Long = checkNotNull(savedStateHandle["id"])
    private val templateId: Long = checkNotNull(savedStateHandle["templateId"])

    private var currencyFormatterMap = getCurrencyFormatterMap()

    private var currentDateTime: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

    private val handler = Handler(Looper.getMainLooper())

    private val updateCurrentTime = Runnable {
        currentDateTime = ZonedDateTime.now(ZoneId.systemDefault())
        scheduleNextUpdate()
        _state.update {
            it.copy(
                timeValid = isTimeValid(selectedDateTime = it.dateTime)
            )
        }
    }

    private var loadDataJob: Job? = null

    private var previousBalance = 0.0
    private var previousAccount: AccountWithTransactionMetadata? = null

    private val _state = MutableStateFlow(UpsertTransactionScreenState())
    val state: StateFlow<UpsertTransactionScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    init {
        scheduleNextUpdate()
        loadDataJob = loadData()
    }

    override fun onCleared() {
        super.onCleared()
        _event.close()
        handler.removeCallbacks(updateCurrentTime)
    }

    private fun scheduleNextUpdate() {
        val now = System.currentTimeMillis()
        val nextMinute = ((now / 60000) + 1) * 60000
        val delay = nextMinute - now
        handler.postDelayed(updateCurrentTime, delay)
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

    private fun getTransactionTitleQuery(): Flow<List<String>> {
        return database
            .transactionQueries
            .getTitles()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    private fun loadData() = viewModelScope.launch {
        val flows = if (transactionId > 0) {
            combineSixFlows(
                getAccountQuery(),
                getCategoryQuery(),
                getCounterPartyQuery(),
                getMethodQuery(),
                getTransactionTitleQuery(),
                database
                    .transactionQueries
                    .getById(transactionId)
                    .asFlow()
                    .mapToOne(Dispatchers.IO)
            ) { accounts, categories, counterParties, methods, titles, transaction ->
                UpsertTransactionFlows(
                    accounts = accounts,
                    categories = categories,
                    counterParties = counterParties,
                    methods = methods,
                    transactionTitles = titles,
                    transaction = transaction,
                    transactionTemplate = null
                )
            }
        } else if (templateId > 0) {
            combineSixFlows(
                getAccountQuery(),
                getCategoryQuery(),
                getCounterPartyQuery(),
                getMethodQuery(),
                getTransactionTitleQuery(),
                database
                    .transactionTemplateQueries
                    .getById(templateId)
                    .asFlow()
                    .mapToOne(Dispatchers.IO)
            ) { accounts, categories, counterParties, methods, titles, template ->
                UpsertTransactionFlows(
                    accounts = accounts,
                    categories = categories,
                    counterParties = counterParties,
                    methods = methods,
                    transactionTitles = titles,
                    transaction = null,
                    transactionTemplate = template
                )
            }
        } else {
            combineFiveFlows(
                getAccountQuery(),
                getCategoryQuery(),
                getCounterPartyQuery(),
                getMethodQuery(),
                getTransactionTitleQuery()
            ) { accounts, categories, counterParties, methods, titles ->
                UpsertTransactionFlows(
                    accounts = accounts,
                    categories = categories,
                    counterParties = counterParties,
                    methods = methods,
                    transactionTitles = titles,
                    transaction = null,
                    transactionTemplate = null,
                )
            }
        }
        flows.collectLatest { upsertTransactionFlows ->
            if (upsertTransactionFlows.transaction == null && upsertTransactionFlows.transactionTemplate == null) {
                _state.update {
                    it.copy(
                        accounts = getAccountsWithFormattedBalance(upsertTransactionFlows.accounts),
                        categories = upsertTransactionFlows.categories,
                        counterParties = upsertTransactionFlows.counterParties,
                        methods = upsertTransactionFlows.methods,
                        transactionTitles = upsertTransactionFlows.transactionTitles,
                        dateString = it.dateTime.getDateString(datePattern),
                        timeString = it.dateTime.getTimeString(),
                        loading = false,
                        enabled = true
                    )
                }
            } else if (upsertTransactionFlows.transaction != null) {
                loadDataFromTransaction(upsertTransactionFlows.transaction, upsertTransactionFlows)
            } else if (upsertTransactionFlows.transactionTemplate != null) {
                loadDataFromTemplate(
                    upsertTransactionFlows.transactionTemplate,
                    upsertTransactionFlows
                )
            }
        }
    }

    private fun loadDataFromTransaction(
        transaction: Transaction,
        upsertTransactionFlows: UpsertTransactionFlows
    ) {
        _state.update {
            val accounts = getAccountsWithFormattedBalance(upsertTransactionFlows.accounts)

            val selectedAccount = accounts.find { account ->
                account.accountId == transaction.transactionAccountId
            } ?: it.selectedAccount

            previousAccount = selectedAccount

            val currentBalance = selectedAccount.balance
            previousBalance = if (transaction.transactionType == TransactionType.DEBIT) {
                currentBalance + transaction.transactionAmount
            } else {
                currentBalance - transaction.transactionAmount
            }

            val selectedCategory = upsertTransactionFlows
                .categories
                .find { category ->
                    category.categoryId == transaction.transactionCategoryId
                } ?: it.selectedCategory

            val selectedCounterParty = upsertTransactionFlows
                .counterParties
                .find { counterParty ->
                    counterParty.counterPartyId == transaction.transactionCounterPartyId
                } ?: it.selectedCounterParty

            val selectedMethod = upsertTransactionFlows
                .methods
                .find { method ->
                    method.methodId == transaction.transactionMethodId
                } ?: it.selectedMethod

            val dateTime = transaction.transactionDateTime

            it.copy(
                accounts = accounts,
                selectedAccount = selectedAccount,
                categories = upsertTransactionFlows.categories,
                selectedCategory = selectedCategory,
                counterParties = upsertTransactionFlows.counterParties,
                selectedCounterParty = selectedCounterParty,
                methods = upsertTransactionFlows.methods,
                selectedMethod = selectedMethod,
                transactionTitles = upsertTransactionFlows.transactionTitles,
                transaction = transaction,
                title = transaction.transactionTitle,
                dateTime = dateTime,
                dateString = dateTime.getDateString(datePattern),
                timeString = dateTime.getTimeString(),
                amount = transaction.transactionAmount,
                displayAmount = transaction.transactionAmount.toString(),
                type = transaction.transactionType,
                loading = false,
                enabled = true
            )
        }
    }

    private fun loadDataFromTemplate(
        transactionTemplate: TransactionTemplate,
        upsertTransactionFlows: UpsertTransactionFlows
    ) {
        _state.update {
            val accounts = getAccountsWithFormattedBalance(upsertTransactionFlows.accounts)

            val selectedAccount = accounts.find { account ->
                account.accountId == transactionTemplate.templateAccountId
            } ?: it.selectedAccount

            val selectedCategory = upsertTransactionFlows
                .categories
                .find { category ->
                    category.categoryId == transactionTemplate.templateCategoryId
                } ?: it.selectedCategory

            val selectedCounterParty = upsertTransactionFlows
                .counterParties
                .find { counterParty ->
                    counterParty.counterPartyId == transactionTemplate.templateCounterPartyId
                } ?: it.selectedCounterParty

            val selectedMethod = upsertTransactionFlows
                .methods
                .find { method ->
                    method.methodId == transactionTemplate.templateMethodId
                } ?: it.selectedMethod

            it.copy(
                accounts = accounts,
                selectedAccount = selectedAccount,
                categories = upsertTransactionFlows.categories,
                selectedCategory = selectedCategory,
                counterParties = upsertTransactionFlows.counterParties,
                selectedCounterParty = selectedCounterParty,
                methods = upsertTransactionFlows.methods,
                selectedMethod = selectedMethod,
                transactionTitles = upsertTransactionFlows.transactionTitles,
                transaction = it.transaction.copy(
                    transactionTitle = transactionTemplate.templateTitle,
                    transactionDescription = transactionTemplate.templateDescription
                ),
                dateString = it.dateTime.getDateString(datePattern),
                timeString = it.dateTime.getTimeString(),
                title = transactionTemplate.templateTitle,
                amount = transactionTemplate.templateAmount,
                displayAmount = if (transactionTemplate.templateAmount > 0) transactionTemplate.templateAmount.toString() else "",
                type = transactionTemplate.templateType,
                loading = false,
                enabled = true
            )
        }
    }

    private fun getAccountsWithFormattedBalance(accounts: List<AccountWithTransactionMetadata>): List<AccountWithTransactionMetadata> {
        return accounts.map { account ->
            val formatter = currencyFormatterMap.getValue(account.currency)
            account.copy(
                formattedBalance = formatter.format(account.balance).toString()
            )
        }
    }

    private fun isFormValid(): Boolean {
        val amount = state.value.amount
        val amountValid = amount > 0.0

        val titleValid = state.value.title.isNotBlank()
        val categoryValid = state.value.selectedCategory.categoryId > 0
        val methodValid = state.value.selectedMethod.methodId > 0

        val selectedAccount = state.value.selectedAccount
        var accountValid = true
        var accountErrorStringRes = R.string.field_required_error_label
        if (selectedAccount.accountId > 0) {
            if (state.value.type == TransactionType.DEBIT && amount > selectedAccount.balance) {
                accountValid = false
                accountErrorStringRes = R.string.not_enough_balance_error_label
            }
        } else {
            accountValid = false
        }

        val timeValid = isTimeValid(selectedDateTime = state.value.dateTime)

        _state.update {
            it.copy(
                accountValid = accountValid,
                amountValid = amountValid,
                categoryValid = categoryValid,
                methodValid = methodValid,
                accountErrorStringRes = accountErrorStringRes,
                timeValid = timeValid,
                titleValid = titleValid
            )
        }

        return amountValid && categoryValid && methodValid && accountValid && titleValid && timeValid
    }

    private fun hasSufficientBalance(
        amount: Double,
        type: TransactionType,
        account: AccountWithTransactionMetadata
    ) {
        var accountValid = true
        var accountErrorStringRes = R.string.field_required_error_label
        if (type == TransactionType.DEBIT) {
            if (amount > 0.0 && account.accountId > 0 && amount > account.balance) {
                accountValid = false
                accountErrorStringRes = R.string.not_enough_balance_error_label
            }
        }
        _state.update {
            it.copy(
                accountValid = accountValid,
                accountErrorStringRes = accountErrorStringRes
            )
        }
    }

    fun upsertTransaction(newTitle: String, newDescription: String) = viewModelScope.launch {
        val formValid = isFormValid()
        if (formValid) {
            loadDataJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val selectedAccount = state.value.selectedAccount
            val selectedCategory = state.value.selectedCategory
            val selectedCounterParty = state.value.selectedCounterParty
            val selectedMethod = state.value.selectedMethod

            val selectedType = state.value.type

            val transactionAmount = state.value.amount
            val dateTime = state.value.dateTime

            try {
                if (transactionId == 0L) {
                    database
                        .transactionQueries
                        .insert(
                            transactionTitle = newTitle,
                            transactionDescription = newDescription,
                            transactionAmount = transactionAmount,
                            transactionCurrency = selectedAccount.currency,
                            transactionType = selectedType,
                            transactionDateTime = dateTime,
                            transactionAccountId = selectedAccount.accountId,
                            transactionCategoryId = selectedCategory.categoryId,
                            transactionCounterPartyId = selectedCounterParty.counterPartyId,
                            transactionMethodId = selectedMethod.methodId,
                            transactionTemplateId = templateId
                        )

                    database
                        .accountQueries
                        .updateBalance(
                            balance = if (selectedType == TransactionType.DEBIT) {
                                selectedAccount.balance - transactionAmount
                            } else {
                                selectedAccount.balance + transactionAmount
                            },
                            accountId = selectedAccount.accountId
                        )
                } else {
                    database
                        .transactionQueries
                        .update(
                            transactionTitle = newTitle,
                            transactionDescription = newDescription,
                            transactionAmount = transactionAmount,
                            transactionCurrency = selectedAccount.currency,
                            transactionType = selectedType,
                            transactionDateTime = dateTime,
                            transactionAccountId = selectedAccount.accountId,
                            transactionCategoryId = selectedCategory.categoryId,
                            transactionCounterPartyId = selectedCounterParty.counterPartyId,
                            transactionMethodId = selectedMethod.methodId,
                            transactionId = transactionId
                        )

                    val accountBalance =
                        if (selectedAccount.accountId == previousAccount?.accountId) {
                            previousBalance
                        } else {
                            selectedAccount.balance
                        }
                    database
                        .accountQueries
                        .updateBalance(
                            balance = if (selectedType == TransactionType.DEBIT) {
                                accountBalance - transactionAmount
                            } else {
                                accountBalance + transactionAmount
                            },
                            accountId = selectedAccount.accountId
                        )
                    if (selectedAccount.accountId != previousAccount?.accountId) {
                        previousAccount?.let { (accountId) ->
                            database
                                .accountQueries
                                .updateBalance(
                                    balance = previousBalance,
                                    accountId = accountId
                                )
                        }
                    }
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

    fun deleteTransaction() = viewModelScope.launch {
        if (transactionId > 0) {
            loadDataJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            try {
                database
                    .transactionQueries
                    .delete(transactionId)

                previousAccount?.let { (accountId) ->
                    database
                        .accountQueries
                        .updateBalance(
                            balance = previousBalance,
                            accountId = accountId
                        )
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

    fun setAmount(amountString: String) {
        _state.update { screenState ->
            var newAmount = 0.0
            var amountValid = false
            var displayAmount = ""
            if (amountString.isNotBlank()) {
                val amt = amountString.toDoubleOrNull()
                if (amt != null) {
                    newAmount = amt
                    amountValid = amt > 0
                    displayAmount = amountString
                }
            }
            hasSufficientBalance(
                amount = newAmount,
                type = screenState.type,
                account = screenState.selectedAccount
            )
            screenState.copy(
                amount = newAmount,
                amountValid = amountValid,
                displayAmount = displayAmount
            )
        }
    }

    fun setTitle(title: String) {
        _state.update {
            it.copy(
                title = title,
                titleValid = title.isNotBlank()
            )
        }
    }

    fun setDate(date: ZonedDateTime) {
        _state.update {
            val newDateTime = it.dateTime.withYear(date.year)
                .withMonth(date.month.value)
                .withDayOfMonth(date.dayOfMonth)
            it.copy(
                dateTime = newDateTime,
                dateString = newDateTime.getDateString(datePattern),
                timeString = newDateTime.getTimeString(),
                timeValid = isTimeValid(selectedDateTime = newDateTime)
            )
        }
    }

    fun setTime(time: ZonedDateTime) {
        _state.update {
            it.copy(
                dateTime = time,
                dateString = time.getDateString(datePattern),
                timeString = time.getTimeString(),
                timeValid = isTimeValid(selectedDateTime = time)
            )
        }
    }

    private fun isTimeValid(selectedDateTime: ZonedDateTime): Boolean {
        val currentYear = currentDateTime.year
        val currentMonth = currentDateTime.month.value
        val currentDay = currentDateTime.dayOfMonth
        return if (currentYear == selectedDateTime.year && currentMonth == selectedDateTime.month.value && currentDay == selectedDateTime.dayOfMonth) {
            val currentHour = currentDateTime.hour
            val currentMinute = currentDateTime.minute
            return if (selectedDateTime.hour == currentHour) {
                selectedDateTime.minute <= currentMinute
            } else if (selectedDateTime.hour > currentHour) {
                false
            } else {
                true
            }
        } else {
            true
        }
    }

    fun setSelectedAccount(account: AccountWithTransactionMetadata) {
        _state.update { screenState ->
            hasSufficientBalance(
                screenState.amount,
                screenState.type,
                account
            )
            screenState.copy(
                selectedAccount = account,
                transaction = screenState.transaction.copy(
                    transactionCurrency = account.currency
                )
            )
        }
    }

    fun setSelectedCategory(category: CategoryWithTransactionMetadata) {
        _state.update {
            it.copy(
                selectedCategory = category,
                categoryValid = true
            )
        }
    }

    fun setSelectedCounterParty(counterParty: CounterPartyWithTransactionMetadata) {
        _state.update {
            it.copy(
                selectedCounterParty = counterParty,
            )
        }
    }

    fun setSelectedMethod(method: MethodWithTransactionMetadata) {
        _state.update {
            it.copy(
                selectedMethod = method,
                methodValid = true
            )
        }
    }

    fun setTransactionType(transactionType: TransactionType) {
        _state.update { screenState ->
            hasSufficientBalance(
                screenState.amount,
                transactionType,
                screenState.selectedAccount
            )
            screenState.copy(
                type = transactionType
            )
        }
    }

    fun filterTransactionTitles(searchString: String): List<String> {
        return if (searchString.isBlank()) {
            state.value.transactionTitles
        } else {
            state.value.transactionTitles.filter {
                it.contains(
                    searchString.trim(),
                    ignoreCase = true
                )
            }
        }
    }

    fun setLocale(locale: Locale?) {
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }

    fun hasChanges(description: String): Boolean {
        val transaction = state.value.transaction
        val selectedAccount = state.value.selectedAccount
        val selectedCategory = state.value.selectedCategory
        val selectedCounterParty = state.value.selectedCounterParty
        val selectedMethod = state.value.selectedMethod
        val selectedType = state.value.type

        val transactionDateTime = transaction.transactionDateTime
        val selectedDateTime = state.value.dateTime

        val yearChanged = transactionDateTime.year != selectedDateTime.year
        val monthChanged = transactionDateTime.month.value != selectedDateTime.month.value
        val dayChanged = transactionDateTime.dayOfMonth != selectedDateTime.dayOfMonth
        val dateChanged = yearChanged || monthChanged || dayChanged

        val hourChanged = transactionDateTime.hour != selectedDateTime.hour
        val minuteChanged = transactionDateTime.minute != selectedDateTime.minute
        val timeChanged = hourChanged || minuteChanged

        val titleChanged = transaction.transactionTitle != state.value.title
        val descriptionChanged = transaction.transactionDescription != description
        val amountChanged = transaction.transactionAmount != state.value.amount
        val accountChanged = transaction.transactionAccountId != selectedAccount.accountId
        val categoryChanged = transaction.transactionCategoryId != selectedCategory.categoryId
        val counterPartyChanged =
            transaction.transactionCounterPartyId != selectedCounterParty.counterPartyId
        val methodChanged = transaction.transactionMethodId != selectedMethod.methodId
        val typeChanged = transaction.transactionType.id != selectedType.id

        return titleChanged || descriptionChanged || amountChanged || dateChanged || timeChanged || categoryChanged || counterPartyChanged || methodChanged || accountChanged || typeChanged
    }
}