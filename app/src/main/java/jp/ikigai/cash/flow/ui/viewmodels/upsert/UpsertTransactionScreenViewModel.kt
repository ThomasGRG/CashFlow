package jp.ikigai.cash.flow.ui.viewmodels.upsert

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.UpsertTransactionFlows
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
import jp.ikigai.cash.flow.data.store.entity.TransactionTitle
import jp.ikigai.cash.flow.data.store.entity.TransactionTitle_
import jp.ikigai.cash.flow.data.store.entity.Transaction_
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionScreenState
import jp.ikigai.cash.flow.utils.combineFiveFlows
import jp.ikigai.cash.flow.utils.combineSixFlows
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getTimeString
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class UpsertTransactionScreenViewModel(
    savedStateHandle: SavedStateHandle,
    store: BoxStore = DataStore.store
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
    private var previousAccount: Account? = null

    private var previousCategory: Category? = null
    private var previousCounterparty: CounterParty? = null
    private var previousMethod: Method? = null

    private val _state = MutableStateFlow(UpsertTransactionScreenState())
    val state: StateFlow<UpsertTransactionScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val accountBox: Box<Account> = store.boxFor()
    private val categoryBox: Box<Category> = store.boxFor()
    private val counterPartyBox: Box<CounterParty> = store.boxFor()
    private val methodBox: Box<Method> = store.boxFor()
    private val transactionBox: Box<Transaction> = store.boxFor()
    private val templateBox: Box<TransactionTemplate> = store.boxFor()
    private val titleBox: Box<TransactionTitle> = store.boxFor()

    private val accountQuery = accountBox
        .query()
        .orderDesc(Account_.frequency)
        .build()

    private val categoryQuery = categoryBox
        .query()
        .orderDesc(Category_.frequency)
        .build()

    private val counterPartyQuery = counterPartyBox
        .query()
        .orderDesc(CounterParty_.frequency)
        .build()

    private val methodQuery = methodBox
        .query()
        .orderDesc(Method_.frequency)
        .build()

    private val templateQuery = templateBox
        .query(TransactionTemplate_.id.equal(0L))
        .build()

    private val transactionQuery = transactionBox
        .query(Transaction_.id.equal(0L))
        .build()

    private val transactionTitleQuery = titleBox
        .query()
        .orderDesc(TransactionTitle_.frequency)
        .build()

    init {
        scheduleNextUpdate()
        loadDataJob = loadData()
    }

    override fun onCleared() {
        super.onCleared()
        _event.close()
        handler.removeCallbacks(updateCurrentTime)
        accountQuery.close()
        categoryQuery.close()
        counterPartyQuery.close()
        methodQuery.close()
        templateQuery.close()
        transactionQuery.close()
        transactionTitleQuery.close()
    }

    private fun scheduleNextUpdate() {
        val now = System.currentTimeMillis()
        val nextMinute = ((now / 60000) + 1) * 60000
        val delay = nextMinute - now
        handler.postDelayed(updateCurrentTime, delay)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() = viewModelScope.launch {
        val flows = if (transactionId > 0) {
            combineSixFlows(
                accountQuery.flow(),
                categoryQuery.flow(),
                counterPartyQuery.flow(),
                methodQuery.flow(),
                transactionTitleQuery.flow(),
                transactionQuery.setParameter(Transaction_.id, transactionId).flow()
            ) { accounts, categories, counterParties, methods, titles, transactions ->
                UpsertTransactionFlows(
                    accounts = accounts,
                    categories = categories,
                    counterParties = counterParties,
                    methods = methods,
                    transactionTitles = titles,
                    transaction = transactions.first(),
                    transactionTemplate = null
                )
            }
        } else if (templateId > 0) {
            combineSixFlows(
                accountQuery.flow(),
                categoryQuery.flow(),
                counterPartyQuery.flow(),
                methodQuery.flow(),
                transactionTitleQuery.flow(),
                templateQuery.setParameter(TransactionTemplate_.id, templateId).flow()
            ) { accounts, categories, counterParties, methods, titles, templates ->
                UpsertTransactionFlows(
                    accounts = accounts,
                    categories = categories,
                    counterParties = counterParties,
                    methods = methods,
                    transactionTitles = titles,
                    transaction = null,
                    transactionTemplate = templates.first()
                )
            }
        } else {
            combineFiveFlows(
                accountQuery.flow(),
                categoryQuery.flow(),
                counterPartyQuery.flow(),
                methodQuery.flow(),
                transactionTitleQuery.flow()
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
        previousAccount = transaction.account.target

        val currency = transaction.account.target.currency
        val currentBalance = transaction.account.target.balance
        previousBalance = if (transaction.type == TransactionType.DEBIT) {
            currentBalance + transaction.amount
        } else {
            currentBalance - transaction.amount
        }

        previousCategory = transaction.category.target
        previousCounterparty = transaction.counterParty.target
        previousMethod = transaction.method.target

        _state.update {
            val dateTime = transaction.time
            val account = transaction.account.target.copy(
                formattedBalance = currencyFormatterMap.getValue(currency).format(currentBalance)
                    .toString()
            )
            it.copy(
                accounts = getAccountsWithFormattedBalance(upsertTransactionFlows.accounts),
                selectedAccount = account,
                categories = upsertTransactionFlows.categories,
                selectedCategory = transaction.category.target,
                counterParties = upsertTransactionFlows.counterParties,
                selectedCounterParty = transaction.counterParty.target ?: it.selectedCounterParty,
                methods = upsertTransactionFlows.methods,
                selectedMethod = transaction.method.target,
                transactionTitles = upsertTransactionFlows.transactionTitles,
                transaction = transaction,
                title = transaction.title,
                dateTime = dateTime,
                dateString = dateTime.getDateString(datePattern),
                timeString = dateTime.getTimeString(),
                amount = transaction.amount,
                displayAmount = transaction.amount.toString(),
                type = transaction.type,
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
                account.id == transactionTemplate.account.targetId
            } ?: it.selectedAccount
            it.copy(
                accounts = accounts,
                selectedAccount = selectedAccount,
                categories = upsertTransactionFlows.categories,
                selectedCategory = transactionTemplate.category.target ?: it.selectedCategory,
                counterParties = upsertTransactionFlows.counterParties,
                selectedCounterParty = transactionTemplate.counterParty.target
                    ?: it.selectedCounterParty,
                methods = upsertTransactionFlows.methods,
                selectedMethod = transactionTemplate.method.target ?: it.selectedMethod,
                transactionTitles = upsertTransactionFlows.transactionTitles,
                transaction = it.transaction.copy(
                    title = transactionTemplate.title,
                    description = transactionTemplate.description
                ),
                dateString = it.dateTime.getDateString(datePattern),
                timeString = it.dateTime.getTimeString(),
                title = transactionTemplate.title,
                amount = transactionTemplate.amount,
                displayAmount = if (transactionTemplate.amount > 0) transactionTemplate.amount.toString() else "",
                type = transactionTemplate.type,
                loading = false,
                enabled = true
            )
        }
    }

    private fun getAccountsWithFormattedBalance(accounts: List<Account>): List<Account> {
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
        val categoryValid = state.value.selectedCategory.id > 0
        val methodValid = state.value.selectedMethod.id > 0

        val selectedAccount = state.value.selectedAccount
        var accountValid = true
        var accountErrorStringRes = R.string.field_required_error_label
        if (selectedAccount.id > 0) {
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
        account: Account
    ) {
        var accountValid = true
        var accountErrorStringRes = R.string.field_required_error_label
        if (type == TransactionType.DEBIT) {
            if (amount > 0.0 && account.id > 0 && amount > account.balance) {
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

            val transaction = state.value.transaction
            val selectedAccount = state.value.selectedAccount
            val selectedCategory = state.value.selectedCategory
            val selectedCounterParty = state.value.selectedCounterParty
            val selectedMethod = state.value.selectedMethod
            val selectedType = state.value.type
            val transactionAmount = state.value.amount

            val time = ZonedDateTime.now(ZoneId.systemDefault())

            createOrUpdateTransactionTitle(newTitle, time)

            if (templateId > 0) {
                updateTemplate(time)
            }

            if (transactionId > 0) {
                val accountBalance = if (selectedAccount.id == previousAccount?.id) {
                    previousBalance
                } else {
                    selectedAccount.balance
                }
                updateAccount(
                    account = selectedAccount,
                    balance = if (selectedType == TransactionType.DEBIT) {
                        accountBalance - transactionAmount
                    } else {
                        accountBalance + transactionAmount
                    },
                    frequency = selectedAccount.frequency,
                    time = time
                )
                if (selectedAccount.id != previousAccount?.id) {
                    previousAccount?.let { account ->
                        updateAccount(
                            account = account,
                            balance = previousBalance,
                            frequency = account.frequency - 1
                        )
                    }
                }
                previousCategory?.let { oldCategory ->
                    if (selectedCategory.id != oldCategory.id) {
                        updateCategory(
                            category = selectedCategory,
                            frequency = selectedCategory.frequency + 1,
                            time = time
                        )
                        updateCategory(
                            category = oldCategory,
                            frequency = oldCategory.frequency - 1
                        )
                    }
                }
                previousCounterparty?.let { oldCounterParty ->
                    if (selectedCounterParty.id != oldCounterParty.id) {
                        updateCounterParty(
                            counterParty = selectedCounterParty,
                            frequency = selectedCounterParty.frequency + 1,
                            time = time
                        )
                        updateCounterParty(
                            counterParty = oldCounterParty,
                            frequency = oldCounterParty.frequency - 1
                        )
                    }
                }
                previousMethod?.let { oldMethod ->
                    if (selectedMethod.id != oldMethod.id) {
                        updateMethod(
                            method = selectedMethod,
                            frequency = selectedMethod.frequency + 1,
                            time = time
                        )
                        updateMethod(
                            method = oldMethod,
                            frequency = oldMethod.frequency - 1
                        )
                    }
                }
            } else {
                updateCategory(
                    category = selectedCategory,
                    frequency = selectedCategory.frequency + 1,
                    time = time
                )
                if (selectedCounterParty.id > 0) {
                    updateCounterParty(
                        counterParty = selectedCounterParty,
                        frequency = selectedCounterParty.frequency + 1,
                        time = time
                    )
                }
                updateMethod(
                    method = selectedMethod,
                    frequency = selectedMethod.frequency + 1,
                    time = time
                )
                updateAccount(
                    account = selectedAccount,
                    balance = if (selectedType == TransactionType.DEBIT) {
                        selectedAccount.balance - transactionAmount
                    } else {
                        selectedAccount.balance + transactionAmount
                    },
                    frequency = selectedAccount.frequency + 1,
                    time = time
                )
            }
            updateTransaction(
                transaction = transaction,
                newTitle = newTitle,
                newDescription = newDescription,
                account = selectedAccount,
                category = selectedCategory,
                counterParty = selectedCounterParty,
                method = selectedMethod
            )
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

            val transaction = state.value.transaction
            val account = transaction.account.target
            val category = transaction.category.target
            val counterParty = transaction.counterParty.target
            val method = transaction.method.target

            updateCategory(
                category = category,
                frequency = category.frequency - 1
            )
            if (counterParty != null) {
                updateCounterParty(
                    counterParty = counterParty,
                    frequency = counterParty.frequency - 1
                )
            }
            updateMethod(
                method = method,
                frequency = method.frequency - 1,
            )
            updateAccount(
                account = account,
                balance = previousBalance,
                frequency = account.frequency - 1
            )

            updateOrDeleteTransactionTitle(transaction.title)

            val deleted = transactionBox.remove(transactionId)

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

    private suspend fun updateTransaction(
        transaction: Transaction,
        newTitle: String,
        newDescription: String,
        account: Account,
        category: Category,
        counterParty: CounterParty,
        method: Method
    ) {
        val transactionToUpsert = transaction.copy(
            title = newTitle,
            description = newDescription,
            amount = state.value.amount,
            time = state.value.dateTime,
            type = state.value.type,
            currency = account.currency
        )

        transactionToUpsert.account.target = account
        transactionToUpsert.category.target = category
        transactionToUpsert.counterParty.target = counterParty
        transactionToUpsert.method.target = method

        try {
            transactionBox.put(transactionToUpsert)
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

    private fun updateTemplate(time: ZonedDateTime) {
        val getTemplateQuery = templateBox
            .query(TransactionTemplate_.id.equal(templateId))
            .build()

        getTemplateQuery.findUnique()
            ?.let {
                templateBox.put(
                    it.copy(
                        frequency = it.frequency + 1,
                        lastUsed = time
                    )
                )
            }

        getTemplateQuery.close()
    }

    private fun createOrUpdateTransactionTitle(
        title: String,
        time: ZonedDateTime,
        frequency: Int? = null
    ) {
        val getTransactionTitleQuery = titleBox
            .query(TransactionTitle_.title.equal(title))
            .build()

        getTransactionTitleQuery.findUnique().let { transactionTitle ->
            if (transactionTitle != null) {
                titleBox.put(
                    transactionTitle.copy(
                        frequency = frequency ?: (transactionTitle.frequency + 1),
                        lastUsed = time
                    )
                )
            } else {
                titleBox.put(
                    TransactionTitle(
                        title = title,
                        frequency = 1,
                        lastUsed = time
                    )
                )
            }
        }

        getTransactionTitleQuery.close()
    }

    private fun updateOrDeleteTransactionTitle(title: String) {
        val getTransactionsQuery = transactionBox
            .query(
                Transaction_.title.equal(title)
                    .and(
                        Transaction_.id.notEqual(transactionId)
                    )
            )
            .orderDesc(Transaction_.time)
            .build()

        getTransactionsQuery.find().let { transactions ->
            if (transactions.isNotEmpty()) {
                createOrUpdateTransactionTitle(title, transactions[0].time, transactions.size)
            } else {
                val transactionTitleQuery = titleBox
                    .query(TransactionTitle_.title.equal(title))
                    .build()

                transactionTitleQuery.remove()

                transactionTitleQuery.close()
            }
        }

        getTransactionsQuery.close()
    }

    private fun updateCategory(
        category: Category,
        frequency: Int,
        time: ZonedDateTime? = null
    ) {
        val lastUsedQuery = transactionBox
            .query(Transaction_.categoryId.equal(category.id))
            .orderDesc(Transaction_.time)
            .build()

        val lastUsed = time ?: lastUsedQuery.findFirst()?.time

        lastUsedQuery.close()

        categoryBox.put(
            category.copy(
                frequency = frequency,
                lastUsed = lastUsed ?: ZonedDateTime.ofInstant(
                    Instant.EPOCH,
                    ZoneId.systemDefault()
                )
            )
        )
    }

    private fun updateCounterParty(
        counterParty: CounterParty,
        frequency: Int,
        time: ZonedDateTime? = null
    ) {
        val lastUsedQuery = transactionBox
            .query(Transaction_.counterPartyId.equal(counterParty.id))
            .orderDesc(Transaction_.time)
            .build()

        val lastUsed = time ?: lastUsedQuery.findFirst()?.time

        lastUsedQuery.close()

        counterPartyBox.put(
            counterParty.copy(
                frequency = frequency,
                lastUsed = lastUsed ?: ZonedDateTime.ofInstant(
                    Instant.EPOCH,
                    ZoneId.systemDefault()
                )
            )
        )
    }

    private fun updateMethod(
        method: Method,
        frequency: Int,
        time: ZonedDateTime? = null
    ) {
        val lastUsedQuery = transactionBox
            .query(Transaction_.methodId.equal(method.id))
            .orderDesc(Transaction_.time)
            .build()

        val lastUsed = time ?: lastUsedQuery.findFirst()?.time

        lastUsedQuery.close()

        methodBox.put(
            method.copy(
                frequency = frequency,
                lastUsed = lastUsed ?: ZonedDateTime.ofInstant(
                    Instant.EPOCH,
                    ZoneId.systemDefault()
                )
            )
        )
    }

    private fun updateAccount(
        account: Account,
        balance: Double,
        frequency: Int,
        time: ZonedDateTime? = null
    ) {
        val lastUsedQuery = transactionBox
            .query(Transaction_.accountId.equal(account.id))
            .orderDesc(Transaction_.time)
            .build()

        val lastUsed = time ?: lastUsedQuery.findFirst()?.time

        lastUsedQuery.close()

        accountBox.put(
            account.copy(
                balance = balance,
                frequency = frequency,
                lastUsed = lastUsed ?: ZonedDateTime.ofInstant(
                    Instant.EPOCH,
                    ZoneId.systemDefault()
                )
            )
        )
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

    fun setSelectedAccount(account: Account) {
        _state.update { screenState ->
            hasSufficientBalance(
                screenState.amount,
                screenState.type,
                account
            )
            screenState.copy(
                selectedAccount = account,
                transaction = screenState.transaction.copy(
                    currency = account.currency
                )
            )
        }
    }

    fun setSelectedCategory(category: Category) {
        _state.update {
            it.copy(
                selectedCategory = category,
                categoryValid = true
            )
        }
    }

    fun setSelectedCounterParty(counterParty: CounterParty) {
        _state.update {
            it.copy(
                selectedCounterParty = counterParty,
            )
        }
    }

    fun setSelectedMethod(method: Method) {
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
            state.value.transactionTitles.map { it.title }
        } else {
            state.value.transactionTitles.map { it.title }
                .filter { it.contains(searchString.trim(), ignoreCase = true) }
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

        val transactionDateTime = transaction.time
        val selectedDateTime = state.value.dateTime

        val yearChanged = transactionDateTime.year != selectedDateTime.year
        val monthChanged = transactionDateTime.month.value != selectedDateTime.month.value
        val dayChanged = transactionDateTime.dayOfMonth != selectedDateTime.dayOfMonth
        val dateChanged = yearChanged || monthChanged || dayChanged

        val hourChanged = transactionDateTime.hour != selectedDateTime.hour
        val minuteChanged = transactionDateTime.minute != selectedDateTime.minute
        val timeChanged = hourChanged || minuteChanged

        val titleChanged = transaction.title != state.value.title
        val descriptionChanged = transaction.description != description
        val amountChanged = transaction.amount != state.value.amount
        val accountChanged = transaction.account.targetId != selectedAccount.id
        val categoryChanged = transaction.category.targetId != selectedCategory.id
        val counterPartyChanged = transaction.counterParty.targetId != selectedCounterParty.id
        val methodChanged = transaction.method.targetId != selectedMethod.id
        val typeChanged = transaction.type.id != selectedType.id

        return titleChanged || descriptionChanged || amountChanged || dateChanged || timeChanged || categoryChanged || counterPartyChanged || methodChanged || accountChanged || typeChanged
    }
}