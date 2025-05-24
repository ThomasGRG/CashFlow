package jp.ikigai.cash.flow.ui.viewmodels.upsert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.flow
import io.objectbox.query.QueryBuilder
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.UpsertTransactionTemplateFlows
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
import jp.ikigai.cash.flow.data.store.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.store.entity.TransactionTemplate_
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionTemplateScreenState
import jp.ikigai.cash.flow.utils.combineFiveFlows
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.util.Locale

class UpsertTransactionTemplateScreenViewModel(
    savedStateHandle: SavedStateHandle,
    store: BoxStore = DataStore.store
) : ViewModel() {

    private val templateId: Long = checkNotNull(savedStateHandle["id"])

    private var currencyFormatterMap = getCurrencyFormatterMap()

    private var loadDataJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertTransactionTemplateScreenState())
    val state: StateFlow<UpsertTransactionTemplateScreenState> = _state.asStateFlow()

    private val accountBox: Box<Account> = store.boxFor()
    private val categoryBox: Box<Category> = store.boxFor()
    private val counterPartyBox: Box<CounterParty> = store.boxFor()
    private val methodBox: Box<Method> = store.boxFor()
    private val templateBox: Box<TransactionTemplate> = store.boxFor()

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

    private val nameAlreadyInUseQuery = templateBox
        .query(TransactionTemplate_.name.equal("", QueryBuilder.StringOrder.CASE_INSENSITIVE))
        .build()

    init {
        loadDataJob = loadData()
    }

    override fun onCleared() {
        super.onCleared()
        _event.close()
        accountQuery.close()
        categoryQuery.close()
        counterPartyQuery.close()
        methodQuery.close()
        templateQuery.close()
        nameAlreadyInUseQuery.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadData() = viewModelScope.launch {
        val flows = if (templateId > 0) {
            combineFiveFlows(
                accountQuery.flow(),
                categoryQuery.flow(),
                counterPartyQuery.flow(),
                methodQuery.flow(),
                templateQuery.setParameter(TransactionTemplate_.id, templateId).flow()
            ) { accounts, categories, counterParties, methods, template ->
                UpsertTransactionTemplateFlows(
                    accounts = accounts,
                    categories = categories,
                    counterParties = counterParties,
                    methods = methods,
                    transactionTemplate = template.first()
                )
            }
        } else {
            combine(
                accountQuery.flow(),
                categoryQuery.flow(),
                counterPartyQuery.flow(),
                methodQuery.flow()
            ) { accounts, categories, counterParties, methods ->
                UpsertTransactionTemplateFlows(
                    accounts = accounts,
                    categories = categories,
                    counterParties = counterParties,
                    methods = methods,
                )
            }
        }
        flows.collectLatest { upsertTransactionTemplateFlows ->
            _state.update {
                val accounts = upsertTransactionTemplateFlows.accounts.map { account ->
                    val formatter = currencyFormatterMap.getValue(account.currency)
                    account.copy(
                        formattedBalance = formatter.format(account.balance).toString()
                    )
                }

                if (upsertTransactionTemplateFlows.transactionTemplate == null) {
                    it.copy(
                        accounts = accounts,
                        categories = upsertTransactionTemplateFlows.categories,
                        counterParties = upsertTransactionTemplateFlows.counterParties,
                        methods = upsertTransactionTemplateFlows.methods,
                        loading = false,
                        enabled = true
                    )
                } else {
                    val transactionTemplate = upsertTransactionTemplateFlows.transactionTemplate

                    val selectedAccount = transactionTemplate.account.target ?: it.selectedAccount
                    val currencyFormatter = currencyFormatterMap.getValue(selectedAccount.currency)

                    val selectedCategory =
                        transactionTemplate.category.target ?: it.selectedCategory
                    val selectedCounterParty =
                        transactionTemplate.counterParty.target ?: it.selectedCounterParty
                    val selectedMethod = transactionTemplate.method.target ?: it.selectedMethod

                    it.copy(
                        transactionTemplate = transactionTemplate,
                        name = transactionTemplate.name,
                        amount = transactionTemplate.amount,
                        displayAmount = transactionTemplate.amount.toString(),
                        accounts = accounts,
                        selectedAccount = selectedAccount.copy(
                            formattedBalance = currencyFormatter.format(selectedAccount.balance)
                                .toString()
                        ),
                        categories = upsertTransactionTemplateFlows.categories,
                        selectedCategory = selectedCategory,
                        counterParties = upsertTransactionTemplateFlows.counterParties,
                        selectedCounterParty = selectedCounterParty,
                        methods = upsertTransactionTemplateFlows.methods,
                        selectedMethod = selectedMethod,
                        type = transactionTemplate.type,
                        loading = false,
                        enabled = true
                    )
                }
            }
        }
    }

    fun checkNameAlreadyInUse(name: String) = viewModelScope.launch {
        if (name.isNotBlank() && name.trim() != state.value.transactionTemplate.name) {
            nameAlreadyInUseQuery
                .setParameter(TransactionTemplate_.name, name.trim())
                .count()
                .let { count ->
                    _state.update {
                        it.copy(
                            nameValid = if (count > 0) false else it.nameValid,
                            nameErrorStringRes = if (count > 0) R.string.name_in_use_label else R.string.name_empty_error_label,
                            loading = false
                        )
                    }
                }
        } else {
            _state.update {
                it.copy(
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
        if (state.value.selectedAccount.id > 0) validCount += 1
        if (state.value.selectedCategory.id > 0) validCount += 1
        if (state.value.selectedCounterParty.id > 0) validCount += 1
        if (state.value.selectedMethod.id > 0) validCount += 1
        return validCount >= 2
    }

    fun upsertTransactionTemplate(newName: String, newTitle: String, newDescription: String) =
        viewModelScope.launch {
            if (newName.isBlank()) {
                _state.update {
                    it.copy(
                        nameValid = false,
                        nameErrorStringRes = R.string.name_empty_error_label
                    )
                }
                return@launch
            }
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
                .copy(
                    name = newName,
                    title = newTitle,
                    description = newDescription,
                    amount = state.value.amount,
                    type = state.value.type
                )

            val selectedAccount = state.value.selectedAccount
            val selectedCategory = state.value.selectedCategory
            val selectedCounterParty = state.value.selectedCounterParty
            val selectedMethod = state.value.selectedMethod

            transactionTemplate.account.targetId = selectedAccount.id
            transactionTemplate.category.targetId = selectedCategory.id
            transactionTemplate.counterParty.targetId = selectedCounterParty.id
            transactionTemplate.method.targetId = selectedMethod.id

            try {
                templateBox.put(transactionTemplate)
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

    fun deleteTransactionTemplate() = viewModelScope.launch {
        if (templateId > 0) {
            loadDataJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            val deleted = templateBox.remove(templateId)

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

    fun setSelectedAccount(account: Account) {
        _state.update {
            it.copy(
                selectedAccount = account
            )
        }
    }

    fun setSelectedCategory(category: Category) {
        _state.update {
            it.copy(
                selectedCategory = category
            )
        }
    }

    fun setSelectedCounterParty(counterParty: CounterParty) {
        _state.update {
            it.copy(
                selectedCounterParty = counterParty
            )
        }
    }

    fun setSelectedMethod(method: Method) {
        _state.update {
            it.copy(
                selectedMethod = method
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

    fun setLocale(locale: Locale?) {
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }

    fun hasChanges(title: String, description: String): Boolean {
        val transactionTemplate = state.value.transactionTemplate
        val selectedAccount = state.value.selectedAccount
        val selectedCategory = state.value.selectedCategory
        val selectedCounterParty = state.value.selectedCounterParty
        val selectedMethod = state.value.selectedMethod
        val selectedType = state.value.type

        val nameChanged = transactionTemplate.name != state.value.name
        val titleChanged = transactionTemplate.title != title
        val descriptionChanged = transactionTemplate.description != description
        val amountChanged = transactionTemplate.amount != state.value.amount
        val accountChanged = transactionTemplate.account.targetId != selectedAccount.id
        val categoryChanged = transactionTemplate.category.targetId != selectedCategory.id
        val counterPartyChanged =
            transactionTemplate.counterParty.targetId != selectedCounterParty.id
        val methodChanged = transactionTemplate.method.targetId != selectedMethod.id
        val typeChanged = transactionTemplate.type.id != selectedType.id

        return nameChanged || titleChanged || descriptionChanged || amountChanged || categoryChanged || counterPartyChanged || methodChanged || accountChanged || typeChanged
    }
}