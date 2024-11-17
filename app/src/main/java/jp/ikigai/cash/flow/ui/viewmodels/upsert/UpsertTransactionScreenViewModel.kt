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
import jp.ikigai.cash.flow.data.dto.UpsertTransactionFlows
import jp.ikigai.cash.flow.data.dto.UpsertTransactionItemCardInfo
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.entity.TransactionTitle
import jp.ikigai.cash.flow.data.enums.ItemUnit
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionScreenState
import jp.ikigai.cash.flow.utils.combineSevenFlows
import jp.ikigai.cash.flow.utils.combineSixFlows
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
    private val templateUuid: String = checkNotNull(savedStateHandle["templateId"])

    private var loadDataJob: Job? = null

    private var previousBalance = 0.0
    private var previousSource: Source? = null

    private val _state = MutableStateFlow(UpsertTransactionScreenState())
    val state: StateFlow<UpsertTransactionScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val sourceQuery = realm.query<Source>().sort("frequency", Sort.DESCENDING)

    private val methodQuery = realm.query<Method>().sort("frequency", Sort.DESCENDING)

    private val itemsQuery = realm.query<Item>().sort("frequency", Sort.DESCENDING)

    private val counterPartyQuery = realm.query<CounterParty>().sort("frequency", Sort.DESCENDING)

    private val categoryQuery = realm.query<Category>().sort("frequency", Sort.DESCENDING)

    private val transactionTitleQuery =
        realm.query<TransactionTitle>().sort("frequency", Sort.DESCENDING)

    init {
        loadDataJob = loadData()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun loadData() = viewModelScope.launch {
        val flows = if (transactionUuid.isNotBlank()) {
            combineSevenFlows(
                categoryQuery.asFlow(),
                counterPartyQuery.asFlow(),
                itemsQuery.asFlow(),
                methodQuery.asFlow(),
                sourceQuery.asFlow(),
                transactionTitleQuery.asFlow(),
                realm.query<Transaction>("uuid==$0", transactionUuid).asFlow()
            ) { categoryChanges, counterPartyChanges, itemChanges, methodChanges, sourceChanges, transactionTitleChanges, transactionChanges ->
                UpsertTransactionFlows(
                    categories = categoryChanges.list,
                    counterParties = counterPartyChanges.list,
                    methods = methodChanges.list,
                    sources = sourceChanges.list,
                    items = itemChanges.list,
                    transactionTitles = transactionTitleChanges.list,
                    transaction = transactionChanges.list.first(),
                    transactionTemplate = null,
                )
            }
        } else if (templateUuid.isNotBlank()) {
            combineSevenFlows(
                categoryQuery.asFlow(),
                counterPartyQuery.asFlow(),
                itemsQuery.asFlow(),
                methodQuery.asFlow(),
                sourceQuery.asFlow(),
                transactionTitleQuery.asFlow(),
                realm.query<TransactionTemplate>("uuid==$0", templateUuid).asFlow()
            ) { categoryChanges, counterPartyChanges, itemChanges, methodChanges, sourceChanges, transactionTitleChanges, templateChanges ->
                UpsertTransactionFlows(
                    categories = categoryChanges.list,
                    counterParties = counterPartyChanges.list,
                    methods = methodChanges.list,
                    sources = sourceChanges.list,
                    items = itemChanges.list,
                    transactionTitles = transactionTitleChanges.list,
                    transaction = null,
                    transactionTemplate = templateChanges.list.first(),
                )
            }
        } else {
            combineSixFlows(
                categoryQuery.asFlow(),
                counterPartyQuery.asFlow(),
                itemsQuery.asFlow(),
                methodQuery.asFlow(),
                sourceQuery.asFlow(),
                transactionTitleQuery.asFlow()
            ) { categoryChanges, counterPartyChanges, itemChanges, methodChanges, sourceChanges, transactionTitleChanges ->
                UpsertTransactionFlows(
                    categories = categoryChanges.list,
                    counterParties = counterPartyChanges.list,
                    methods = methodChanges.list,
                    sources = sourceChanges.list,
                    items = itemChanges.list,
                    transactionTitles = transactionTitleChanges.list,
                    transaction = null,
                    transactionTemplate = null,
                )
            }
        }
        flows.collectLatest { upsertTransactionFlows ->
            if (upsertTransactionFlows.transaction == null && upsertTransactionFlows.transactionTemplate == null) {
                _state.update {
                    it.copy(
                        transactionTitles = upsertTransactionFlows.transactionTitles,
                        categories = upsertTransactionFlows.categories,
                        counterParties = upsertTransactionFlows.counterParties,
                        methods = upsertTransactionFlows.methods,
                        sources = upsertTransactionFlows.sources,
                        items = upsertTransactionFlows.items,
                        addItemsFilteredList = upsertTransactionFlows.items,
                        dateString = it.dateTime.getDateString(),
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
        val dateTime = transaction.time.toZonedDateTime()
        val source = transaction.source!!
        previousSource = transaction.source
        previousBalance = if (transaction.type == TransactionType.DEBIT) {
            source.balance + transaction.amount
        } else {
            source.balance - transaction.amount
        }
        _state.update {
            it.copy(
                transactionTitles = upsertTransactionFlows.transactionTitles,
                categories = upsertTransactionFlows.categories,
                selectedCategory = transaction.category!!,
                counterParties = upsertTransactionFlows.counterParties,
                selectedCounterParty = transaction.counterParty ?: it.selectedCounterParty,
                methods = upsertTransactionFlows.methods,
                selectedMethod = transaction.method!!,
                sources = upsertTransactionFlows.sources,
                selectedSource = source,
                items = upsertTransactionFlows.items,
                addItemsFilteredList = upsertTransactionFlows.items,
                transaction = transaction,
                title = transaction.title,
                dateTime = dateTime,
                dateString = dateTime.getDateString(),
                timeString = dateTime.getTimeString(),
                amount = transaction.amount,
                taxAmount = transaction.taxAmount,
                displayAmount = transaction.amount.toString(),
                displayTaxAmount = transaction.taxAmount.toString(),
                type = transaction.type,
                transactionItems = transaction.items.map { transactionItem ->
                    val total = transactionItem.price * transactionItem.quantity
                    UpsertTransactionItemCardInfo(
                        item = transactionItem.item!!,
                        unit = transactionItem.unit,
                        price = transactionItem.price,
                        displayPrice = transactionItem.price.toString(),
                        totalPrice = total,
                        totalDisplayPrice = total.toString(),
                        quantity = transactionItem.quantity,
                        displayQuantity = transactionItem.quantity.toString()
                    )
                },
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
            it.copy(
                transactionTitles = upsertTransactionFlows.transactionTitles,
                categories = upsertTransactionFlows.categories,
                selectedCategory = transactionTemplate.category
                    ?: it.selectedCategory,
                counterParties = upsertTransactionFlows.counterParties,
                selectedCounterParty = transactionTemplate.counterParty ?: it.selectedCounterParty,
                methods = upsertTransactionFlows.methods,
                selectedMethod = transactionTemplate.method
                    ?: it.selectedMethod,
                sources = upsertTransactionFlows.sources,
                selectedSource = transactionTemplate.source
                    ?: it.selectedSource,
                items = upsertTransactionFlows.items,
                addItemsFilteredList = upsertTransactionFlows.items,
                transaction = Transaction(
                    transactionTemplate.title,
                    transactionTemplate.description
                ),
                dateString = it.dateTime.getDateString(),
                timeString = it.dateTime.getTimeString(),
                title = transactionTemplate.title,
                amount = transactionTemplate.amount,
                taxAmount = transactionTemplate.taxAmount,
                displayAmount = transactionTemplate.amount.toString(),
                displayTaxAmount = transactionTemplate.taxAmount.toString(),
                type = transactionTemplate.type,
                transactionItems = transactionTemplate.items.map { transactionItem ->
                    val total = transactionItem.price * transactionItem.quantity
                    UpsertTransactionItemCardInfo(
                        item = transactionItem.item!!,
                        unit = transactionItem.unit,
                        displayPrice = transactionItem.price.toString(),
                        totalPrice = total,
                        totalDisplayPrice = total.toString(),
                        quantity = transactionItem.quantity,
                        displayQuantity = transactionItem.quantity.toString()
                    )
                },
                loading = false,
                enabled = true
            )
        }
    }

    private fun isFormValid(): Boolean {
        val amount = state.value.amount
        val amountValid = amount > 0.0

        val titleValid = state.value.title.isNotBlank()
        val categoryValid = state.value.selectedCategory.uuid.isNotEmpty()
        val methodValid = state.value.selectedMethod.uuid.isNotEmpty()
        val sourceValid = state.value.selectedSource.uuid.isNotEmpty()

        val transactionItems = state.value.transactionItems.toMutableList()
            .map { transactionItem ->
                transactionItem.copy(
                    priceValid = transactionItem.price > 0,
                    quantityValid = transactionItem.quantity > 0
                )
            }

        _state.update {
            it.copy(
                titleValid = titleValid,
                amountValid = amountValid,
                categoryValid = categoryValid,
                methodValid = methodValid,
                sourceValid = sourceValid,
                transactionItems = transactionItems
            )
        }

        val itemsValid =
            state.value.type == TransactionType.CREDIT || !transactionItems.any { it.quantity == 0.0 || it.price == 0.0 }

        val selectedSource = state.value.selectedSource

        if (amountValid && selectedSource.uuid.isNotBlank() && state.value.type == TransactionType.DEBIT && amount > selectedSource.balance) {
            viewModelScope.launch {
                _event.send(Event.NotEnoughBalance)
            }
            return false
        }
        return amountValid && itemsValid && categoryValid && methodValid && sourceValid && titleValid
    }

    private fun hasSufficientBalance(
        amount: Double,
        tax: Double,
        type: TransactionType,
        source: Source
    ) {
        if (type != TransactionType.DEBIT) {
            return
        }
        val totalAmount = amount + tax
        if (totalAmount > 0.0 && source.uuid.isNotBlank() && totalAmount > source.balance) {
            viewModelScope.launch {
                _event.send(Event.NotEnoughBalance)
            }
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
            val selectedCategory = state.value.selectedCategory
            val selectedCounterParty = state.value.selectedCounterParty
            val selectedMethod = state.value.selectedMethod
            val selectedSource = state.value.selectedSource

            val time = ZonedDateTime.now(ZoneId.of("UTC")).toEpochMilli()

            val transactionItems: List<TransactionItem> =
                if (state.value.type == TransactionType.DEBIT) {
                    state.value.transactionItems.map {
                        TransactionItem(
                            item = it.item,
                            unit = it.unit,
                            price = it.price,
                            quantity = it.quantity
                        )
                    }
                } else {
                    emptyList()
                }

            updateItems(transactionItems, time, transaction.currency)

            updateTransactionTitle(newTitle, time)

            if (templateUuid.isNotBlank()) {
                updateTemplate(time)
            }

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
            loadDataJob?.cancelAndJoin()
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
                    it.frequency -= 1
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
            val latestCounterParty =
                if (counterParty.uuid.isNotBlank()) findLatest(counterParty) else null
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
                        this.taxAmount =
                            if (state.value.type == TransactionType.DEBIT) state.value.taxAmount else 0.0
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
                    it.taxAmount =
                        if (state.value.type == TransactionType.DEBIT) state.value.taxAmount else 0.0
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

    private suspend fun updateTemplate(time: Long) {
        realm.write {
            val template = query<TransactionTemplate>("uuid==$0", templateUuid).find().first()
            template.frequency += 1
            template.lastUsed = time
        }
    }

    private suspend fun updateTransactionTitle(title: String, time: Long) {
        realm.write {
            val transactionTitle = query<TransactionTitle>("title == [c]$0", title.trim()).find()
            if (transactionTitle.isEmpty()) {
                copyToRealm(
                    instance = TransactionTitle().apply {
                        uuid = UUID.randomUUID().toString()
                        this.title = title
                        frequency = 1
                        lastUsed = time
                    }
                )
            } else {
                findLatest(transactionTitle.first())?.also {
                    it.frequency += 1
                    it.lastUsed
                }
            }
        }
    }

    private suspend fun updateItems(
        transactionItems: List<TransactionItem>,
        time: Long,
        currency: String
    ) {
        transactionItems.forEach {
            realm.write {
                findLatest(it.item!!)?.also { item ->
                    item.lastUsed = time
                    item.lastUsedUnit = it.unit
                    item.lastKnownPrice = it.price
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
        realm.write {
            val currentSourceBalance: Double
            if (transactionUuid.isNotBlank()) {
                if (previousSource!!.uuid != source.uuid) {
                    findLatest(previousSource!!)?.also {
                        it.balance = previousBalance
                    }
                    currentSourceBalance = if (type == TransactionType.DEBIT) {
                        source.balance - amount
                    } else {
                        source.balance + amount
                    }
                } else {
                    currentSourceBalance = if (type == TransactionType.DEBIT) {
                        previousBalance - amount
                    } else {
                        previousBalance + amount
                    }
                }
            } else {
                currentSourceBalance = if (type == TransactionType.DEBIT) {
                    source.balance - amount
                } else {
                    source.balance + amount
                }
            }
            findLatest(source)?.also {
                it.frequency = frequency
                it.lastUsed = time ?: it.lastUsed
                it.balance = currentSourceBalance
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
                tax = screenState.taxAmount,
                type = screenState.type,
                source = screenState.selectedSource
            )
            screenState.copy(
                amount = newAmount,
                amountValid = amountValid,
                displayAmount = displayAmount
            )
        }
    }

    fun setTaxAmount(amountString: String) {
        _state.update { screenState ->
            var newTaxAmount = 0.0
            var displayTaxAmount = ""
            if (amountString.isNotBlank()) {
                val amt = amountString.toDoubleOrNull()
                if (amt != null) {
                    newTaxAmount = amt
                    displayTaxAmount = amountString
                }
            }
            hasSufficientBalance(
                amount = screenState.amount,
                tax = newTaxAmount,
                type = screenState.type,
                source = screenState.selectedSource
            )
            screenState.copy(
                taxAmount = newTaxAmount,
                displayTaxAmount = displayTaxAmount
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

    fun addItems(items: List<Item>) {
        _state.update { screenState ->
            val transactionItems = screenState.transactionItems.toMutableList()
            transactionItems.addAll(
                items.map { item ->
                    val isSameCurrency =
                        screenState.selectedSource.uuid.isNotEmpty() && item.lastUsedCurrency == screenState.selectedSource.currency
                    val price = if (isSameCurrency) item.lastKnownPrice else 0.0
                    UpsertTransactionItemCardInfo(
                        item = item,
                        unit = item.lastUsedUnit,
                        price = price,
                        displayPrice = price.toString(),
                        quantity = 0.0,
                        displayQuantity = "0",
                        totalPrice = 0.0,
                        totalDisplayPrice = "0"
                    )
                }
            )
            val selectedItemUUIDs = transactionItems.map { it.item.uuid }
            val filteredItems = screenState.items.filter { !selectedItemUUIDs.contains(it.uuid) }
            val newAmount = transactionItems.sumOf { it.totalPrice }
            hasSufficientBalance(
                newAmount,
                screenState.taxAmount,
                screenState.type,
                screenState.selectedSource
            )
            screenState.copy(
                transactionItems = transactionItems,
                addItemsFilteredList = filteredItems,
                amount = newAmount,
                displayAmount = newAmount.toString(),
                amountValid = true
            )
        }
    }

    fun updateItem(item: Item, index: Int) {
        _state.update { screenState ->
            val transactionItems = screenState.transactionItems.toMutableList()
            transactionItems[index] = transactionItems[index].copy(
                item = item
            )
            val selectedItemUUIDs = transactionItems.map { it.item.uuid }
            val filteredItems = screenState.items.filter { !selectedItemUUIDs.contains(it.uuid) }
            screenState.copy(
                transactionItems = transactionItems,
                addItemsFilteredList = filteredItems
            )
        }
    }

    fun updateUnit(itemUnit: ItemUnit, index: Int) {
        _state.update { screenState ->
            val transactionItems = screenState.transactionItems.toMutableList()
            transactionItems[index] = transactionItems[index].copy(
                unit = itemUnit
            )
            screenState.copy(
                transactionItems = transactionItems
            )
        }
    }

    fun getChangeItemFilteredList(uuid: String): List<Item> {
        val transactionItems = state.value.transactionItems
        val selectedItemUUIDs = transactionItems.map { it.item.uuid }
        return state.value.items.filter { item ->
            item.uuid == uuid || !selectedItemUUIDs.contains(item.uuid)
        }
    }

    fun updateTransactionItemPrice(amountString: String, index: Int) {
        _state.update { screenState ->
            val transactionItems = screenState.transactionItems.toMutableList()
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
            val totalPrice = newAmount * transactionItems[index].quantity
            transactionItems[index] = transactionItems[index].copy(
                price = newAmount,
                priceValid = amountValid,
                displayPrice = displayAmount,
                totalPrice = totalPrice,
                totalDisplayPrice = totalPrice.toString()
            )
            val totalAmount = transactionItems.sumOf { it.totalPrice }
            hasSufficientBalance(
                totalAmount,
                screenState.taxAmount,
                screenState.type,
                screenState.selectedSource
            )
            screenState.copy(
                transactionItems = transactionItems,
                amount = totalAmount,
                amountValid = true,
                displayAmount = totalAmount.toString()
            )
        }
    }

    fun updateTransactionItemQuantity(quantityString: String, index: Int) {
        _state.update { screenState ->
            val transactionItems = screenState.transactionItems.toMutableList()
            var newQuantity = 0.0
            var quantityValid = false
            var displayQuantity = ""
            if (quantityString.isNotBlank()) {
                val quantity = quantityString.toDoubleOrNull()
                if (quantity != null) {
                    newQuantity = quantity
                    quantityValid = quantity > 0
                    displayQuantity = quantityString
                }
            }
            val totalPrice = transactionItems[index].price * newQuantity
            transactionItems[index] = transactionItems[index].copy(
                quantity = newQuantity,
                quantityValid = quantityValid,
                displayQuantity = displayQuantity,
                totalPrice = totalPrice,
                totalDisplayPrice = totalPrice.toString()
            )
            val totalAmount = transactionItems.sumOf { it.price }
            hasSufficientBalance(
                totalAmount,
                screenState.taxAmount,
                screenState.type,
                screenState.selectedSource
            )
            screenState.copy(
                transactionItems = transactionItems,
                amount = totalAmount,
                amountValid = true,
                displayAmount = totalAmount.toString()
            )
        }
    }

    fun removeItem(index: Int) {
        _state.update { screenState ->
            val transactionItems = screenState.transactionItems.toMutableList()
            transactionItems.removeAt(index)
            val selectedItemUUIDs = transactionItems.map { it.item.uuid }
            val filteredItems = screenState.items.filter { !selectedItemUUIDs.contains(it.uuid) }
            val totalAmount = transactionItems.sumOf { it.price }
            hasSufficientBalance(
                totalAmount,
                screenState.taxAmount,
                screenState.type,
                screenState.selectedSource
            )
            screenState.copy(
                transactionItems = transactionItems,
                addItemsFilteredList = filteredItems,
                amount = totalAmount,
                amountValid = true,
                displayAmount = totalAmount.toString()
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

    fun setSelectedSource(source: Source) {
        _state.update { screenState ->
            hasSufficientBalance(
                screenState.amount,
                screenState.taxAmount,
                screenState.type,
                source
            )
            screenState.copy(
                selectedSource = source,
                sourceValid = true,
                transaction = screenState.transaction.apply {
                    currency = source.currency
                }
            )
        }
    }

    fun setTransactionType(transactionType: TransactionType) {
        _state.update { screenState ->
            hasSufficientBalance(
                screenState.amount,
                screenState.taxAmount,
                transactionType,
                screenState.selectedSource
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
}