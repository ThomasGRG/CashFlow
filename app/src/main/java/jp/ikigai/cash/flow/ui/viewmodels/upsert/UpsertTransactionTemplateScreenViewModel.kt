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
import jp.ikigai.cash.flow.data.dto.UpsertTransactionTemplateFlows
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Item
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.TransactionItem
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionTemplateScreenState
import jp.ikigai.cash.flow.utils.combineSixFlows
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
                    amount = transactionTemplate.amount,
                    displayAmount = transactionTemplate.amount.toString(),
                    taxAmount = transactionTemplate.taxAmount,
                    displayTaxAmount = transactionTemplate.taxAmount.toString(),
                    selectedCategory = selectedCategory,
                    categories = upsertTransactionTemplateFlows.categories,
                    selectedCounterParty = selectedCounterParty,
                    counterParties = upsertTransactionTemplateFlows.counterParties,
                    items = upsertTransactionTemplateFlows.items,
                    transactionItems = transactionTemplate.items.associateBy { transactionItem -> transactionItem.item!! },
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

    private fun validate(title: String, description: String): Boolean {
        var validCount = 0
        if (title.isNotBlank()) validCount += 1
        if (description.isNotBlank()) validCount += 1
        if (state.value.amount > 0) validCount += 1
        if (state.value.selectedCategory.uuid.isNotBlank()) validCount += 1
        if (state.value.selectedCounterParty.uuid.isNotBlank()) validCount += 1
        if (state.value.selectedMethod.uuid.isNotBlank()) validCount += 1
        if (state.value.selectedSource.uuid.isNotBlank()) validCount += 1
        return validCount >= 2
    }

    fun upsertTransactionTemplate(newName: String, newTitle: String, newDescription: String) =
        viewModelScope.launch {
            val isValid = validate(newTitle, newDescription)
            if (!isValid) {
                _event.send(Event.MinimumTwoFieldsRequired)
                return@launch
            }
            if (newName != state.value.transactionTemplate.name && realm.query<TransactionTemplate>("name == [c]$0", newName).count().find() > 0) {
                _event.send(Event.NameAlreadyTaken)
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

            val transactionItems: List<TransactionItem> =
                state.value.transactionItems.values.toList()

            val result = realm.write {
                val latestCategory =
                    if (selectedCategory.uuid.isNotBlank()) findLatest(selectedCategory) else null
                val latestCounterParty =
                    if (selectedCounterParty.uuid.isNotBlank()) findLatest(selectedCounterParty) else null
                val latestMethod =
                    if (selectedMethod.uuid.isNotBlank()) findLatest(selectedMethod) else null
                val latestSource =
                    if (selectedSource.uuid.isNotBlank()) findLatest(selectedSource) else null
                val latestTransactionItems = transactionItems.map {
                    val latestItem = findLatest(it.item!!)
                    TransactionItem(
                        item = latestItem,
                        price = it.price,
                        quantity = it.quantity,
                        unit = it.unit
                    )
                }.toRealmList()
                if (templateUuid.isBlank()) {
                    copyToRealm(
                        instance = transactionTemplate.apply {
                            this.uuid = UUID.randomUUID().toString()
                            this.name = newName
                            this.title = newTitle
                            this.description = newDescription
                            this.amount = state.value.amount
                            this.taxAmount = state.value.taxAmount
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
                    findLatest(transactionTemplate)?.also {
                        it.name = newName
                        it.title = newTitle
                        it.description = newDescription
                        it.amount = state.value.amount
                        it.taxAmount = state.value.taxAmount
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

    fun setAmount(amountString: String) {
        _state.update {
            it.copy(
                amount = amountString.toDoubleOrNull() ?: 0.0,
                displayAmount = amountString
            )
        }
    }

    fun setTaxAmount(amountString: String) {
        val newTaxAmount = amountString.toDoubleOrNull() ?: 0.0
        val newAmount = calculateAmount(tax = newTaxAmount)
        _state.update {
            it.copy(
                taxAmount = newTaxAmount,
                amount = newAmount,
                displayTaxAmount = amountString,
                displayAmount = newAmount.toString()
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
            )
        }
    }

    private fun calculateAmount(totalItemPrice: Double? = null, tax: Double? = null): Double {
        return (totalItemPrice
            ?: state.value.transactionItems.values.sumOf { it.price }) + (tax
            ?: state.value.taxAmount)
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