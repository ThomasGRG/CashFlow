package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionScreenState
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getTimeString
import jp.ikigai.cash.flow.utils.toEpochMilli
import jp.ikigai.cash.flow.utils.toZonedDateTime
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
import java.util.UUID

class UpsertTransactionScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val transactionUuid: String = checkNotNull(savedStateHandle["id"])

    private var getTransactionJob: Job? = null
    private var getTransactionTitleJob: Job? = null
    private var getCategoryJob: Job? = null
    private var getCounterPartyJob: Job? = null
    private var getMethodJob: Job? = null
    private var getSourceJob: Job? = null
    private var getItemJob: Job? = null

    private var previousBalance = 0.0

    private val _state = MutableStateFlow(UpsertTransactionScreenState())
    val state: StateFlow<UpsertTransactionScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val sourceQuery = realm.query<Source>().sort("frequency", Sort.DESCENDING)

    private val methodQuery = realm.query<Method>().sort("frequency", Sort.DESCENDING)

    private val itemsQuery = realm.query<Item>().sort("frequency", Sort.DESCENDING)

    private val counterPartyQuery = realm.query<CounterParty>().sort("frequency", Sort.DESCENDING)

    private val categoryQuery = realm.query<Category>().sort("frequency", Sort.DESCENDING)

    private val transactionTitleQuery = realm.query<Transaction>("title != $0", "").distinct("title")

    init {
        getTransactionTitleJob = getTransactionTitles()
        getCategoryJob = getCategories()
        getCounterPartyJob = getCounterParties()
        getItemJob = getItems()
        getMethodJob = getMethods()
        getSourceJob = getSources()
        if (transactionUuid.isNotBlank()) {
            getTransactionJob = getTransaction()
        }
        _state.update {
            it.copy(
                dateString = it.dateTime.getDateString(),
                timeString = it.dateTime.getTimeString(),
                loading = false,
                enabled = true
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun getTransactionTitles() = viewModelScope.launch {
        transactionTitleQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    titles = changes.list.map { transaction -> transaction.title }
                )
            }
        }
    }

    private fun getCategories() = viewModelScope.launch {
        categoryQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    categories = changes.list,
                    selectedCategory = if (transactionUuid.isBlank()) changes.list.first() else it.selectedCategory
                )
            }
        }
    }

    private fun getCounterParties() = viewModelScope.launch {
        counterPartyQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    counterParties = changes.list
                )
            }
        }
    }

    private fun getMethods() = viewModelScope.launch {
        methodQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    methods = changes.list,
                    selectedMethod = if (transactionUuid.isBlank()) changes.list.first() else it.selectedMethod
                )
            }
        }
    }

    private fun getSources() = viewModelScope.launch {
        sourceQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    sources = changes.list,
                    selectedSource = if (transactionUuid.isBlank()) changes.list.first() else it.selectedSource
                )
            }
        }
    }

    private fun getItems() = viewModelScope.launch {
        itemsQuery.asFlow().collectLatest { changes ->
            _state.update {
                it.copy(
                    items = changes.list
                )
            }
        }
    }

    private fun getTransaction() = viewModelScope.launch {
        realm.query<Transaction>("uuid==$0", transactionUuid).asFlow().collectLatest { changes ->
            val transaction = changes.list.first()
            val category = transaction.category!!
            val counterParty = transaction.counterParty ?: CounterParty()
            val method = transaction.method!!
            val source = transaction.source!!
            val dateTime = transaction.time.toZonedDateTime()
            previousBalance =
                if (transaction.type == TransactionType.DEBIT) source.balance + transaction.amount else source.balance - transaction.amount
            _state.update {
                it.copy(
                    transaction = transaction,
                    dateTime = dateTime,
                    dateString = dateTime.getDateString(),
                    timeString = dateTime.getTimeString(),
                    amount = transaction.amount,
                    taxAmount = transaction.taxAmount,
                    displayAmount = transaction.amount.toString(),
                    displayTaxAmount = transaction.taxAmount.toString(),
                    type = transaction.type,
                    selectedCategory = category,
                    selectedCounterParty = counterParty,
                    selectedMethod = method,
                    selectedSource = source,
                    transactionItems = transaction.items.associateBy { transactionItem -> transactionItem.item!! },
                    enabled = true,
                    loading = false
                )
            }
        }
    }

    private fun isFormValid(): Boolean {
        val amount = state.value.amount
        val amountValid = amount > 0.0

        _state.update {
            it.copy(
                amountValid = amountValid,
            )
        }

        val selectedSource = state.value.selectedSource

        if (amountValid && selectedSource.uuid.isNotBlank() && state.value.type == TransactionType.DEBIT && amount > selectedSource.balance) {
            viewModelScope.launch {
                _event.send(Event.NotEnoughBalance)
            }
            return false
        }
        return amountValid
    }

    private fun hasSufficientBalance() {
        val amount = state.value.amount
        val selectedSource = state.value.selectedSource
        if (amount > 0.0 && selectedSource.uuid.isNotBlank() && state.value.type == TransactionType.DEBIT && amount > selectedSource.balance) {
            viewModelScope.launch {
                _event.send(Event.NotEnoughBalance)
            }
        }
    }

    fun upsertTransaction(newTitle: String, newDescription: String) = viewModelScope.launch {
        val formValid = isFormValid()
        if (formValid) {
            getSourceJob?.cancelAndJoin()
            getTransactionTitleJob?.cancelAndJoin()
            getMethodJob?.cancelAndJoin()
            getItemJob?.cancelAndJoin()
            getCounterPartyJob?.cancelAndJoin()
            getCategoryJob?.cancelAndJoin()
            getTransactionJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val transaction = state.value.transaction
            val selectedCategory = state.value.selectedCategory
            val selectedCounterParty = state.value.selectedCounterParty
            val selectedMethod = state.value.selectedMethod
            val selectedSource = state.value.selectedSource

            val time = ZonedDateTime.now(ZoneId.of("UTC")).toEpochMilli()

            val transactionItems: List<TransactionItem> =
                state.value.transactionItems.values.toList()

            updateItems(state.value.transactionItems, time, transaction.currency)

            if (transactionUuid.isNotBlank()) {
                updateSource(
                    source = selectedSource,
                    amount = state.value.amount,
                    frequency = selectedSource.frequency,
                    time = time,
                    type = state.value.type
                )
            } else {
                updateCategory(
                    category = selectedCategory,
                    frequency = selectedCategory.frequency + 1, time = time
                )
                if (selectedCounterParty.uuid.isNotBlank()) {
                    updateCounterParty(
                        counterParty = selectedCounterParty,
                        frequency = selectedCounterParty.frequency + 1, time = time
                    )
                }
                updateMethod(
                    method = selectedMethod,
                    frequency = selectedMethod.frequency + 1,
                    time = time
                )
                updateSource(
                    source = selectedSource,
                    amount = state.value.amount,
                    frequency = selectedSource.frequency + 1,
                    time = time,
                    type = state.value.type
                )
            }
            updateTransaction(
                transaction = transaction,
                newTitle = newTitle,
                newDescription = newDescription,
                category = selectedCategory,
                counterParty = selectedCounterParty,
                method = selectedMethod,
                source = selectedSource,
                transactionItems = transactionItems
            )
        }
    }

    fun deleteTransaction() = viewModelScope.launch {
        if (transactionUuid.isNotBlank()) {
            getSourceJob?.cancelAndJoin()
            getMethodJob?.cancelAndJoin()
            getItemJob?.cancelAndJoin()
            getTransactionTitleJob?.cancelAndJoin()
            getCounterPartyJob?.cancelAndJoin()
            getCategoryJob?.cancelAndJoin()
            getTransactionJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val transaction = state.value.transaction
            val selectedCategory = state.value.selectedCategory
            val selectedCounterParty = state.value.selectedCounterParty
            val selectedMethod = state.value.selectedMethod
            val selectedSource = state.value.selectedSource

            updateCategory(
                category = selectedCategory,
                frequency = selectedCategory.frequency - 1
            )
            if (selectedCounterParty.uuid.isNotBlank()) {
                updateCounterParty(
                    counterParty = selectedCounterParty,
                    frequency = selectedCounterParty.frequency - 1
                )
            }
            updateMethod(
                method = selectedMethod,
                frequency = selectedMethod.frequency - 1,
            )
            realm.write {
                findLatest(selectedSource)?.also {
                    it.frequency = it.frequency - 1
                    it.balance = previousBalance
                }
            }
            realm.write {
                findLatest(transaction)?.also {
                    delete(it)
                }
            }
            _state.update {
                it.copy(
                    loading = false
                )
            }
            _event.send(Event.DeleteSuccess)
        }
    }

    private suspend fun updateTransaction(
        transaction: Transaction,
        newTitle: String,
        newDescription: String,
        category: Category,
        counterParty: CounterParty,
        method: Method,
        source: Source,
        transactionItems: List<TransactionItem>
    ) {
        val result = realm.write {
            val latestCategory = findLatest(category)
            val latestCounterParty = if (counterParty.uuid.isNotBlank()) findLatest(counterParty) else null
            val latestMethod = findLatest(method)
            val latestSource = findLatest(source)
            val latestTransactionItems = transactionItems.map {
                val latestItem = findLatest(it.item!!)
                TransactionItem(
                    item = latestItem,
                    price = it.price,
                    quantity = it.quantity,
                    unit = it.unit
                )
            }.toRealmList()
            if (transactionUuid.isBlank()) {
                copyToRealm(
                    instance = transaction.apply {
                        this.uuid = UUID.randomUUID().toString()
                        this.title = newTitle
                        this.description = newDescription
                        this.amount = state.value.amount
                        this.taxAmount = state.value.taxAmount
                        this.time = state.value.dateTime.toEpochMilli()
                        this.type = state.value.type
                        this.category = latestCategory
                        this.method = latestMethod
                        this.source = latestSource
                        this.counterParty = latestCounterParty
                        this.items = latestTransactionItems
                    },
                    updatePolicy = UpdatePolicy.ALL
                )
            } else {
                findLatest(transaction)?.also {
                    it.title = newTitle
                    it.description = newDescription
                    it.amount = state.value.amount
                    it.taxAmount = state.value.taxAmount
                    it.time = state.value.dateTime.toEpochMilli()
                    it.type = state.value.type
                    it.category = latestCategory
                    it.method = latestMethod
                    it.source = latestSource
                    it.counterParty = latestCounterParty
                    it.items = latestTransactionItems
                }
            }
        }
        if (result != null) {
            _event.send(Event.SaveSuccess)
        } else {
            _event.send(Event.InternalError)
        }
        _state.update {
            it.copy(
                loading = false
            )
        }
    }

    private suspend fun updateItems(
        itemsMap: Map<Item, TransactionItem>,
        time: Long,
        currency: String
    ) {
        itemsMap.entries.forEach {
            realm.write {
                findLatest(it.key)?.also { item ->
                    item.lastUsed = time
                    item.lastUsedUnit = it.value.unit
                    item.lastKnownPrice = it.value.price
                    item.lastUsedCurrency = currency
                    item.frequency += 1
                }
            }
        }
    }

    private suspend fun updateCategory(category: Category, frequency: Int, time: Long? = null) {
        realm.write {
            findLatest(category)?.also {
                it.frequency = frequency
                it.lastUsed = time ?: it.lastUsed
            }
        }
    }

    private suspend fun updateCounterParty(
        counterParty: CounterParty,
        frequency: Int,
        time: Long? = null
    ) {
        realm.write {
            findLatest(counterParty)?.also {
                it.frequency = frequency
                it.lastUsed = time ?: it.lastUsed
            }
        }
    }

    private suspend fun updateMethod(method: Method, frequency: Int, time: Long? = null) {
        realm.write {
            findLatest(method)?.also {
                it.frequency = frequency
                it.lastUsed = time ?: it.lastUsed
            }
        }
    }

    private suspend fun updateSource(
        source: Source,
        amount: Double,
        frequency: Int,
        time: Long? = null,
        type: TransactionType
    ) {
        val currentBalance = if (transactionUuid.isNotBlank()) {
            previousBalance
        } else {
            source.balance
        }
        val newBalance = if (type == TransactionType.DEBIT) {
            currentBalance - amount
        } else {
            currentBalance + amount
        }
        realm.write {
            findLatest(source)?.also {
                it.frequency = frequency
                it.lastUsed = time ?: it.lastUsed
                it.balance = newBalance
            }
        }
    }

    fun setAmount(amountString: String) {
        var newAmount = 0.0
        var amountValid = false
        var displayAmount = ""
        if (amountString.isNotBlank()) {
            val amt = amountString.toDoubleOrNull()
            if (amt != null) {
                newAmount = amt
                amountValid = true
                displayAmount = amountString
            }
        }
        _state.update {
            it.copy(
                amount = newAmount,
                amountValid = amountValid,
                displayAmount = displayAmount
            )
        }
        hasSufficientBalance()
    }

    fun setTaxAmount(amountString: String) {
        var newTaxAmount = 0.0
        var displayTaxAmount = ""
        if (amountString.isNotBlank()) {
            val amt = amountString.toDoubleOrNull()
            if (amt != null) {
                newTaxAmount = amt
                displayTaxAmount = amountString
            }
        }
        val newAmount = calculateAmount(tax = newTaxAmount)
        _state.update {
            it.copy(
                taxAmount = newTaxAmount,
                amount = newAmount,
                displayTaxAmount = displayTaxAmount,
                displayAmount = newAmount.toString()
            )
        }
        hasSufficientBalance()
    }

    private fun calculateAmount(totalItemPrice: Double? = null, tax: Double? = null): Double {
        return (totalItemPrice
            ?: state.value.transactionItems.values.sumOf { it.price }) + (tax
            ?: state.value.taxAmount)
    }

    fun setDate(date: ZonedDateTime) {
        _state.update {
            val newDateTime = it.dateTime.withYear(date.year)
                .withMonth(date.month.value)
                .withDayOfMonth(date.dayOfMonth)
            it.copy(
                dateTime = newDateTime,
                dateString = newDateTime.getDateString(),
                timeString = newDateTime.getTimeString(),
            )
        }
    }

    fun setTime(time: ZonedDateTime) {
        _state.update {
            it.copy(
                dateTime = time,
                dateString = time.getDateString(),
                timeString = time.getTimeString(),
            )
        }
    }

    fun addItem(transactionItem: TransactionItem) {
        val transactionItems = state.value.transactionItems.toMutableMap()
        transactionItems[transactionItem.item!!] = transactionItem
        val newAmount = calculateAmount(
            totalItemPrice = transactionItems.values.sumOf { it.price }
        )
        _state.update {
            it.copy(
                transactionItems = transactionItems,
                amount = newAmount,
                displayAmount = newAmount.toString(),
                amountValid = true,
            )
        }
    }

    fun removeItem(transactionItem: TransactionItem) {
        val transactionItems = state.value.transactionItems.toMutableMap()
        transactionItems.remove(transactionItem.item)
        val newAmount = calculateAmount(
            totalItemPrice = transactionItems.values.sumOf { it.price }
        )
        _state.update {
            it.copy(
                transactionItems = transactionItems,
                amount = newAmount,
                displayAmount = newAmount.toString(),
                amountValid = true,
            )
        }
    }

    fun setSelectedCategory(category: Category) {
        _state.update {
            it.copy(
                selectedCategory = category,
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
            )
        }
    }

    fun setSelectedSource(source: Source) {
        _state.update {
            it.copy(
                selectedSource = source,
                transaction = it.transaction.apply {
                    currency = source.currency
                }
            )
        }
        hasSufficientBalance()
    }

    fun setTransactionType(transactionType: TransactionType) {
        _state.update {
            it.copy(
                type = transactionType
            )
        }
        if (transactionType == TransactionType.DEBIT) {
            hasSufficientBalance()
        }
    }
}