package jp.ikigai.cash.flow.ui.viewmodels.upsert

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
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.UpsertTransactionTemplateFlows
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.upsert.UpsertTransactionTemplateScreenState
import jp.ikigai.cash.flow.utils.combineFiveFlows
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import kotlinx.coroutines.Dispatchers
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
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private val templateId: Long = checkNotNull(savedStateHandle["id"])

    private var currencyFormatterMap = getCurrencyFormatterMap()

    private var loadDataJob: Job? = null

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val _state = MutableStateFlow(UpsertTransactionTemplateScreenState())
    val state: StateFlow<UpsertTransactionTemplateScreenState> = _state.asStateFlow()

    init {
        loadDataJob = loadData()
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

    private fun loadData() = viewModelScope.launch {
        val flows = if (templateId > 0) {
            combineFiveFlows(
                getAccountQuery(),
                getCategoryQuery(),
                getCounterPartyQuery(),
                getMethodQuery(),
                database
                    .transactionTemplateQueries
                    .getById(templateId)
                    .asFlow()
                    .mapToOne(Dispatchers.IO)
            ) { accounts, categories, counterParties, methods, template ->
                UpsertTransactionTemplateFlows(
                    accounts = accounts,
                    categories = categories,
                    counterParties = counterParties,
                    methods = methods,
                    transactionTemplate = template
                )
            }
        } else {
            combine(
                getAccountQuery(),
                getCategoryQuery(),
                getCounterPartyQuery(),
                getMethodQuery()
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

                    val selectedAccount = accounts.find { account ->
                        account.accountId == transactionTemplate.templateAccountId
                    } ?: it.selectedAccount

                    val selectedCategory = upsertTransactionTemplateFlows
                        .categories
                        .find { category ->
                            category.categoryId == transactionTemplate.templateCategoryId
                        } ?: it.selectedCategory

                    val selectedCounterParty = upsertTransactionTemplateFlows
                        .counterParties
                        .find { counterParty ->
                            counterParty.counterPartyId == transactionTemplate.templateCounterPartyId
                        } ?: it.selectedCounterParty

                    val selectedMethod = upsertTransactionTemplateFlows
                        .methods
                        .find { method ->
                            method.methodId == transactionTemplate.templateMethodId
                        } ?: it.selectedMethod

                    it.copy(
                        transactionTemplate = transactionTemplate,
                        name = transactionTemplate.templateName,
                        amount = transactionTemplate.templateAmount,
                        displayAmount = transactionTemplate.templateAmount.toString(),
                        accounts = accounts,
                        selectedAccount = selectedAccount,
                        categories = upsertTransactionTemplateFlows.categories,
                        selectedCategory = selectedCategory,
                        counterParties = upsertTransactionTemplateFlows.counterParties,
                        selectedCounterParty = selectedCounterParty,
                        methods = upsertTransactionTemplateFlows.methods,
                        selectedMethod = selectedMethod,
                        type = transactionTemplate.templateType,
                        loading = false,
                        enabled = true
                    )
                }
            }
        }
    }

    fun checkNameAlreadyInUse(name: String) = viewModelScope.launch {
        if (name.isNotBlank() && name.trim() != state.value.transactionTemplate.templateName) {
            database
                .transactionTemplateQueries
                .checkNameAlreadyInUse(
                    name.trim()
                )
                .executeAsOne()
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
        if (state.value.selectedAccount.accountId > 0) validCount += 1
        if (state.value.selectedCategory.categoryId > 0) validCount += 1
        if (state.value.selectedCounterParty.counterPartyId > 0) validCount += 1
        if (state.value.selectedMethod.methodId > 0) validCount += 1
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

            val selectedAccount = state.value.selectedAccount
            val selectedCategory = state.value.selectedCategory
            val selectedCounterParty = state.value.selectedCounterParty
            val selectedMethod = state.value.selectedMethod

            try {
                if (templateId == 0L) {
                    database
                        .transactionTemplateQueries
                        .insert(
                            templateName = newName,
                            templateTitle = newTitle,
                            templateDescription = newDescription,
                            templateAmount = state.value.amount,
                            templateType = state.value.type,
                            templateAccountId = selectedAccount.accountId,
                            templateCategoryId = selectedCategory.categoryId,
                            templateCounterPartyId = selectedCounterParty.counterPartyId,
                            templateMethodId = selectedMethod.methodId
                        )
                } else {
                    database
                        .transactionTemplateQueries
                        .update(
                            templateName = newName,
                            templateTitle = newTitle,
                            templateDescription = newDescription,
                            templateAmount = state.value.amount,
                            templateType = state.value.type,
                            templateAccountId = selectedAccount.accountId,
                            templateCategoryId = selectedCategory.categoryId,
                            templateCounterPartyId = selectedCounterParty.counterPartyId,
                            templateMethodId = selectedMethod.methodId,
                            templateId = templateId
                        )
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

    fun deleteTransactionTemplate() = viewModelScope.launch {
        if (templateId > 0) {
            loadDataJob?.cancelAndJoin()
            _state.update {
                it.copy(
                    loading = true,
                    enabled = false
                )
            }

            try {
                database
                    .transactionTemplateQueries
                    .delete(templateId)
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

    fun setSelectedAccount(account: AccountWithTransactionMetadata) {
        _state.update {
            it.copy(
                selectedAccount = account
            )
        }
    }

    fun setSelectedCategory(category: CategoryWithTransactionMetadata) {
        _state.update {
            it.copy(
                selectedCategory = category
            )
        }
    }

    fun setSelectedCounterParty(counterParty: CounterPartyWithTransactionMetadata) {
        _state.update {
            it.copy(
                selectedCounterParty = counterParty
            )
        }
    }

    fun setSelectedMethod(method: MethodWithTransactionMetadata) {
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

        val nameChanged = transactionTemplate.templateName != state.value.name
        val titleChanged = transactionTemplate.templateTitle != title
        val descriptionChanged = transactionTemplate.templateDescription != description
        val amountChanged = transactionTemplate.templateAmount != state.value.amount
        val accountChanged = transactionTemplate.templateAccountId != selectedAccount.accountId
        val categoryChanged = transactionTemplate.templateCategoryId != selectedCategory.categoryId
        val counterPartyChanged =
            transactionTemplate.templateCounterPartyId != selectedCounterParty.counterPartyId
        val methodChanged = transactionTemplate.templateMethodId != selectedMethod.methodId
        val typeChanged = transactionTemplate.templateType.id != selectedType.id

        return nameChanged || titleChanged || descriptionChanged || amountChanged || categoryChanged || counterPartyChanged || methodChanged || accountChanged || typeChanged
    }
}