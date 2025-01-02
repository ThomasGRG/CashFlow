package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.UpsertTemplateItemCardInfo
import jp.ikigai.cash.flow.data.dto.UpsertTransactionTemplateFlows
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.enums.ItemUnit
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionTemplateScreenState
import jp.ikigai.cash.flow.utils.combineSixFlows
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class UpsertTransactionTemplateScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private val templateUuid: String = checkNotNull(savedStateHandle["id"])

    private var loadDataJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertTransactionTemplateScreenState())
    val state: StateFlow<UpsertTransactionTemplateScreenState> = _state.asStateFlow()

    private val sourceQuery = realm.query<Source>().sort("frequency", Sort.DESCENDING)

    private val methodQuery = realm.query<Method>().sort("frequency", Sort.DESCENDING)

    private val itemsQuery = realm.query<Item>().sort("frequency", Sort.DESCENDING)

    private val counterPartyQuery = realm.query<CounterParty>().sort("frequency", Sort.DESCENDING)

    private val categoryQuery = realm.query<Category>().sort("frequency", Sort.DESCENDING)

    init {
        loadDataJob = loadData()
        checkNameAlreadyInUse()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }

    private fun loadData() = viewModelScope.launch {
        val flows = if (templateUuid.isNotBlank()) {
            combineSixFlows(
                categoryQuery.asFlow(),
                counterPartyQuery.asFlow(),
                itemsQuery.asFlow(),
                methodQuery.asFlow(),
                sourceQuery.asFlow(),
                realm.query<TransactionTemplate>("uuid==$0", templateUuid).asFlow()
            ) { categoryChanges, counterPartyChanges, itemChanges, methodChanges, sourceChanges, transactionTemplateChanges ->
                UpsertTransactionTemplateFlows(
                    categories = categoryChanges.list,
                    counterParties = counterPartyChanges.list,
                    items = itemChanges.list,
                    methods = methodChanges.list,
                    sources = sourceChanges.list,
                    transactionTemplate = transactionTemplateChanges.list.first()
                )
            }
        } else {
            combine(
                categoryQuery.asFlow(),
                counterPartyQuery.asFlow(),
                itemsQuery.asFlow(),
                methodQuery.asFlow(),
                sourceQuery.asFlow(),
            ) { categoryChanges, counterPartyChanges, itemChanges, methodChanges, sourceChanges ->
                UpsertTransactionTemplateFlows(
                    categories = categoryChanges.list,
                    counterParties = counterPartyChanges.list,
                    items = itemChanges.list,
                    methods = methodChanges.list,
                    sources = sourceChanges.list,
                )
            }
        }
        flows.collectLatest { upsertTransactionTemplateFlows ->
            _state.update {
                val transactionTemplate =
                    upsertTransactionTemplateFlows.transactionTemplate ?: it.transactionTemplate
                val selectedCategory = transactionTemplate.category ?: it.selectedCategory
                val selectedCounterParty = transactionTemplate.counterParty ?: it.selectedCounterParty
                val selectedMethod = transactionTemplate.method ?: it.selectedMethod
                val selectedSource = transactionTemplate.source ?: it.selectedSource
                it.copy(
                    transactionTemplate = transactionTemplate,
                    name = transactionTemplate.name,
                    amount = transactionTemplate.amount,
                    displayAmount = transactionTemplate.amount.toString(),
                    selectedCategory = selectedCategory,
                    categories = upsertTransactionTemplateFlows.categories,
                    selectedCounterParty = selectedCounterParty,
                    counterParties = upsertTransactionTemplateFlows.counterParties,
                    items = upsertTransactionTemplateFlows.items,
                    addItemsFilteredList = upsertTransactionTemplateFlows.items,
                    templateItems = transactionTemplate.items.map { transactionItem ->
                        val total = transactionItem.price * transactionItem.quantity
                        UpsertTemplateItemCardInfo(
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
                    selectedMethod = selectedMethod,
                    methods = upsertTransactionTemplateFlows.methods,
                    selectedSource = selectedSource,
                    sources = upsertTransactionTemplateFlows.sources,
                    type = transactionTemplate.type,
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun checkNameAlreadyInUse() = viewModelScope.launch {
        state
            .debounce(250L)
            .flatMapLatest { screenState ->
                val searchName =
                    if (screenState.name.isNotBlank() && screenState.name.trim() != screenState.transactionTemplate.name) {
                        screenState.name.trim()
                    } else {
                        ""
                    }
                realm.query<TransactionTemplate>("name == [c]$0", searchName)
                    .count()
                    .asFlow()
            }.collectLatest { count ->
                _state.update {
                    it.copy(
                        nameValid = if (count > 0) false else it.nameValid,
                        nameErrorStringRes = if (count > 0) R.string.name_in_use_label else R.string.name_empty_error_label,
                        loading = false
                    )
                }
            }
    }

    private fun validate(title: String, description: String): Boolean {
        var validCount = 0
        if (title.isNotBlank()) validCount += 1
        if (description.isNotBlank()) validCount += 1
        if (state.value.amount > 0) validCount += 1
        if (state.value.selectedCategory.uuid.isNotBlank()) validCount += 1
        if (state.value.selectedCounterParty.uuid.isNotBlank()) validCount += 1
        if (state.value.selectedMethod.uuid.isNotBlank()) validCount += 1
        if (state.value.selectedSource.uuid.isNotBlank()) validCount += 1
        if (state.value.templateItems.isNotEmpty()) validCount += 1
        return validCount >= 2
    }

    fun upsertTransactionTemplate(newName: String, newTitle: String, newDescription: String) =
        viewModelScope.launch {
            if (!state.value.nameValid || state.value.loading) {
                return@launch
            }
            val isValid = validate(newTitle, newDescription)
            if (!isValid) {
                _event.send(Event.MinimumTwoFieldsRequired)
                return@launch
            }
            loadDataJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val transactionTemplate = state.value.transactionTemplate
            val selectedCategory = state.value.selectedCategory
            val selectedCounterParty = state.value.selectedCounterParty
            val selectedMethod = state.value.selectedMethod
            val selectedSource = state.value.selectedSource

            val result = realm.write {
                val latestCategory =
                    if (selectedCategory.uuid.isNotBlank()) findLatest(selectedCategory) else null
                val latestCounterParty =
                    if (selectedCounterParty.uuid.isNotBlank()) findLatest(selectedCounterParty) else null
                val latestMethod =
                    if (selectedMethod.uuid.isNotBlank()) findLatest(selectedMethod) else null
                val latestSource =
                    if (selectedSource.uuid.isNotBlank()) findLatest(selectedSource) else null
                val latestTransactionItems = if (state.value.type == TransactionType.DEBIT) {
                    state.value.templateItems.map {
                        val latestItem = findLatest(it.item)
                        TransactionItem(
                            item = latestItem,
                            price = it.price,
                            quantity = it.quantity,
                            unit = it.unit
                        )
                    }
                } else {
                    emptyList()
                }
                if (templateUuid.isBlank()) {
                    copyToRealm(
                        instance = transactionTemplate.apply {
                            this.uuid = UUID.randomUUID().toString()
                            this.name = newName
                            this.title = newTitle
                            this.description = newDescription
                            this.amount = state.value.amount
                            this.type = state.value.type
                            this.category = latestCategory
                            this.method = latestMethod
                            this.source = latestSource
                            this.counterParty = latestCounterParty
                            this.items = latestTransactionItems.toRealmList()
                        },
                        updatePolicy = UpdatePolicy.ALL
                    )
                } else {
                    findLatest(transactionTemplate)?.also {
                        it.name = newName
                        it.title = newTitle
                        it.description = newDescription
                        it.amount = state.value.amount
                        it.type = state.value.type
                        it.category = latestCategory
                        it.method = latestMethod
                        it.source = latestSource
                        it.counterParty = latestCounterParty
                        it.items = latestTransactionItems.toRealmList()
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

    fun deleteTransactionTemplate() = viewModelScope.launch {
        if (templateUuid.isNotBlank()) {
            loadDataJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }
            realm.write {
                findLatest(state.value.transactionTemplate)?.also {
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

    fun setAmount(amountString: String) {
        _state.update {
            it.copy(
                amount = amountString.toDoubleOrNull() ?: 0.0,
                displayAmount = amountString
            )
        }
    }

    fun addItems(items: List<Item>) {
        _state.update { screenState ->
            val templateItems = screenState.templateItems.toMutableList()
            templateItems.addAll(
                items.map { item ->
                    val isSameCurrency =
                        screenState.selectedSource.uuid.isNotEmpty() && item.lastUsedCurrency == screenState.selectedSource.currency
                    val price = if (isSameCurrency) item.lastKnownPrice else 0.0
                    UpsertTemplateItemCardInfo(
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
            val selectedItemUUIDs = templateItems.map { it.item.uuid }
            val filteredItems = screenState.items.filter { !selectedItemUUIDs.contains(it.uuid) }
            val newAmount = templateItems.sumOf { it.totalPrice }
            screenState.copy(
                templateItems = templateItems,
                addItemsFilteredList = filteredItems,
                amount = newAmount,
                displayAmount = newAmount.toString(),
            )
        }
    }

    fun updateItem(item: Item, index: Int) {
        _state.update { screenState ->
            val templateItems = screenState.templateItems.toMutableList()
            templateItems[index] = templateItems[index].copy(
                item = item
            )
            val selectedItemUUIDs = templateItems.map { it.item.uuid }
            val filteredItems = screenState.items.filter { !selectedItemUUIDs.contains(it.uuid) }
            screenState.copy(
                templateItems = templateItems,
                addItemsFilteredList = filteredItems
            )
        }
    }

    fun updateUnit(itemUnit: ItemUnit, index: Int) {
        _state.update { screenState ->
            val templateItems = screenState.templateItems.toMutableList()
            templateItems[index] = templateItems[index].copy(
                unit = itemUnit
            )
            screenState.copy(
                templateItems = templateItems
            )
        }
    }

    fun getChangeItemFilteredList(uuid: String): List<Item> {
        val templateItems = state.value.templateItems
        val selectedItemUUIDs = templateItems.map { it.item.uuid }
        return state.value.items.filter { item ->
            item.uuid == uuid || !selectedItemUUIDs.contains(item.uuid)
        }
    }

    fun updateTemplateItemPrice(amountString: String, index: Int) {
        _state.update { screenState ->
            val templateItems = screenState.templateItems.toMutableList()
            val newAmount = amountString.toDoubleOrNull() ?: 0.0
            val totalPrice = newAmount * templateItems[index].quantity
            templateItems[index] = templateItems[index].copy(
                price = newAmount,
                displayPrice = amountString,
                totalPrice = totalPrice,
                totalDisplayPrice = totalPrice.toString()
            )
            val totalAmount = templateItems.sumOf { it.totalPrice }
            screenState.copy(
                templateItems = templateItems,
                amount = totalAmount,
                displayAmount = totalAmount.toString()
            )
        }
    }

    fun updateTemplateItemQuantity(quantityString: String, index: Int) {
        _state.update { screenState ->
            val templateItems = screenState.templateItems.toMutableList()
            val newQuantity = quantityString.toDoubleOrNull() ?: 0.0
            val totalPrice = templateItems[index].price * newQuantity
            templateItems[index] = templateItems[index].copy(
                quantity = newQuantity,
                displayQuantity = quantityString,
                totalPrice = totalPrice,
                totalDisplayPrice = totalPrice.toString()
            )
            val totalAmount = templateItems.sumOf { it.price }
            screenState.copy(
                templateItems = templateItems,
                amount = totalAmount,
                displayAmount = totalAmount.toString()
            )
        }
    }

    fun removeItem(index: Int) {
        _state.update { screenState ->
            val templateItems = screenState.templateItems.toMutableList()
            templateItems.removeAt(index)
            val selectedItemUUIDs = templateItems.map { it.item.uuid }
            val filteredItems = screenState.items.filter { !selectedItemUUIDs.contains(it.uuid) }
            val totalAmount = templateItems.sumOf { it.price }
            screenState.copy(
                templateItems = templateItems,
                addItemsFilteredList = filteredItems,
                amount = totalAmount,
                displayAmount = totalAmount.toString()
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
            )
        }
    }

    fun setTransactionType(transactionType: TransactionType) {
        _state.update {
            it.copy(
                type = transactionType
            )
        }
    }
}