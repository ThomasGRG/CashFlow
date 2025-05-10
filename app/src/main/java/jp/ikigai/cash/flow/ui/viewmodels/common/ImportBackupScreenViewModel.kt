package jp.ikigai.cash.flow.ui.viewmodels.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import compose.icons.tablericons.ChartLine
import compose.icons.tablericons.FileText
import compose.icons.tablericons.History
import compose.icons.tablericons.Typography
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.TransactionTemplateWithIcons
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.dto.export.ExportData
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.data.entity.temp.TempCategory
import jp.ikigai.cash.flow.data.entity.temp.TempCounterParty
import jp.ikigai.cash.flow.data.entity.temp.TempMethod
import jp.ikigai.cash.flow.data.entity.temp.TempSource
import jp.ikigai.cash.flow.data.entity.temp.TempTransaction
import jp.ikigai.cash.flow.data.entity.temp.TempTransactionTemplate
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.common.SortOptionsScreenState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenFiltersState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenPrimaryState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenSecondaryState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getIconForCategory
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.toEpochMilli
import jp.ikigai.cash.flow.utils.toZonedDateTime
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.buffer
import okio.source
import okio.use
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class ImportBackupScreenViewModel(
    private val realm: Realm = Realm.open(Database.config)
) : ViewModel() {

    private val datePattern = "dd-LLL-yyyy"

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private var loadDataJob: Job? = null
    private var loadTransactionsJob: Job? = null

    //region StateFlows

    private val _primaryState = MutableStateFlow(ImportBackupScreenPrimaryState())
    val primaryState: StateFlow<ImportBackupScreenPrimaryState> = _primaryState.asStateFlow()

    private val _secondaryState = MutableStateFlow(ImportBackupScreenSecondaryState())
    val secondaryState: StateFlow<ImportBackupScreenSecondaryState> = _secondaryState.asStateFlow()

    private val _searchState = MutableStateFlow("")
    val searchState: StateFlow<String> = _searchState.asStateFlow()

    private val _sortOptionsState = MutableStateFlow(
        SortOptionsScreenState(Sort.DESCENDING, "time")
    )
    val sortOptionsState: StateFlow<SortOptionsScreenState> = _sortOptionsState.asStateFlow()

    private val _filtersState = MutableStateFlow(ImportBackupScreenFiltersState())
    val filtersState: StateFlow<ImportBackupScreenFiltersState> = _filtersState.asStateFlow()

    //endregion

    private val _event: Channel<Event?> = Channel(Int.MAX_VALUE)
    val event: Flow<Event?> = _event.receiveAsFlow()

    //region Realm Queries

    private val categoryQuery = realm
        .query<Category>()
        .sort("frequency", Sort.DESCENDING)

    private val tempCategoryQuery = realm
        .query<TempCategory>()
        .sort("frequency", Sort.DESCENDING)

    private val counterPartyQuery = realm
        .query<CounterParty>()
        .sort("frequency", Sort.DESCENDING)

    private val tempCounterPartyQuery = realm
        .query<TempCounterParty>()
        .sort("frequency", Sort.DESCENDING)

    private val methodQuery = realm
        .query<Method>()
        .sort("frequency", Sort.DESCENDING)

    private val tempMethodQuery = realm
        .query<TempMethod>()
        .sort("frequency", Sort.DESCENDING)

    private val sourceQuery = realm
        .query<Source>()
        .sort("frequency", Sort.DESCENDING)

    private val tempSourceQuery = realm
        .query<TempSource>()
        .sort("frequency", Sort.DESCENDING)

    private val tempTransactionTemplatesQuery = realm
        .query<TempTransactionTemplate>()
        .sort("frequency", Sort.DESCENDING)

    //endregion

    override fun onCleared() {
        super.onCleared()
        realm.close()
        _event.close()
    }

    private fun MutableRealm.clearTempData() {
        delete(TempCategory::class)
        delete(TempCounterParty::class)
        delete(TempMethod::class)
        delete(TempSource::class)
        delete(TempTransactionTemplate::class)
        delete(TempTransaction::class)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun loadFile(inputStream: InputStream?) = viewModelScope.launch {
        _primaryState.update {
            it.copy(
                loading = true
            )
        }

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter<ExportData>()

        inputStream?.source()?.buffer().use { bufferedSource ->
            if (bufferedSource != null) {
                try {
                    val data = adapter.fromJson(bufferedSource)

                    realm.write {
                        clearTempData()

                        val tempCategoryMap: MutableMap<String, TempCategory> = mutableMapOf()
                        val tempCounterPartyMap: MutableMap<String, TempCounterParty> =
                            mutableMapOf()
                        val tempMethodMap: MutableMap<String, TempMethod> = mutableMapOf()
                        val tempSourceMap: MutableMap<String, TempSource> = mutableMapOf()

                        data?.categories?.forEach { category ->
                            val tempCategory = TempCategory().apply {
                                uuid = category.uuid
                                name = category.name
                                icon = category.iconName.getIconForCategory()
                                frequency = category.frequency
                                lastUsed = category.lastUsed.toZonedDateTime()
                            }
                            val managedInstance = copyToRealm(tempCategory)
                            tempCategoryMap[tempCategory.uuid] = managedInstance
                        }
                        data?.counterParties?.forEach { counterParty ->
                            val tempCounterParty = TempCounterParty().apply {
                                uuid = counterParty.uuid
                                name = counterParty.name
                                frequency = counterParty.frequency
                                lastUsed = counterParty.lastUsed.toZonedDateTime()
                            }
                            val managedInstance = copyToRealm(tempCounterParty)
                            tempCounterPartyMap[tempCounterParty.uuid] = managedInstance
                        }
                        data?.methods?.forEach { method ->
                            val tempMethod = TempMethod().apply {
                                uuid = method.uuid
                                name = method.name
                                frequency = method.frequency
                                lastUsed = method.lastUsed.toZonedDateTime()
                            }
                            val managedInstance = copyToRealm(tempMethod)
                            tempMethodMap[tempMethod.uuid] = managedInstance
                        }
                        data?.sources?.forEach { source ->
                            val tempSource = TempSource().apply {
                                uuid = source.uuid
                                name = source.name
                                currency = source.currency
                                balance = source.balance
                                frequency = source.frequency
                                lastUsed = source.lastUsed.toZonedDateTime()
                            }
                            val managedInstance = copyToRealm(tempSource)
                            tempSourceMap[tempSource.uuid] = managedInstance
                        }
                        data?.transactions?.forEach { transaction ->
                            val tempTransaction = TempTransaction().apply {
                                uuid = transaction.uuid
                                title = transaction.title
                                description = transaction.description
                                amount = transaction.amount
                                type = TransactionType.values()
                                    .find { transactionType -> transactionType.id == transaction.typeId }
                                    ?: TransactionType.DEBIT
                                currency = transaction.currency
                                time = transaction.time.toZonedDateTime()
                                category = tempCategoryMap[transaction.categoryUUID]
                                counterParty = tempCounterPartyMap[transaction.counterPartyUUID]
                                method = tempMethodMap[transaction.methodUUID]
                                source = tempSourceMap[transaction.sourceUUID]
                            }
                            copyToRealm(tempTransaction)
                        }
                        data?.templates?.forEach { template ->
                            val tempTemplate = TempTransactionTemplate().apply {
                                uuid = template.uuid
                                name = template.name
                                title = template.title
                                description = template.description
                                amount = template.amount
                                type = TransactionType.values()
                                    .find { transactionType -> transactionType.id == template.typeId }
                                    ?: TransactionType.DEBIT
                                category = tempCategoryMap[template.categoryUUID]
                                counterParty = tempCounterPartyMap[template.counterPartyUUID]
                                method = tempMethodMap[template.methodUUID]
                                source = tempSourceMap[template.sourceUUID]
                                frequency = template.frequency
                                lastUsed = template.lastUsed.toZonedDateTime()
                            }
                            copyToRealm(tempTemplate)
                        }
                    }

                    _primaryState.update {
                        it.copy(dataLoadComplete = true)
                    }

                    loadDataJob = loadData()
                } catch (exception: Exception) {
                    _event.send(Event.InternalError)
                    _primaryState.update {
                        it.copy(
                            loading = false
                        )
                    }
                }
            }
        }
    }

    private fun loadData() = viewModelScope.launch {
        val categories = categoryQuery.find()
        val counterParties = counterPartyQuery.find()
        val methods = methodQuery.find()
        val sources = sourceQuery.find().toMutableList()

        val tempCategories = tempCategoryQuery.find()
        val tempCounterParties = tempCounterPartyQuery.find()
        val tempMethods = tempMethodQuery.find()
        val tempSources = tempSourceQuery.find().toMutableList()
        val tempTransactionTemplates = tempTransactionTemplatesQuery.find()

        sources.forEach { source ->
            val formatter = currencyFormatterMap.getValue(source.currency)
            source.displayBalance = formatter.format(source.balance).toString()
        }

        tempSources.forEach { source ->
            val formatter = currencyFormatterMap.getValue(source.currency)
            source.displayBalance = formatter.format(source.balance).toString()
        }

        val conflictingTempCategories = tempCategories
            .filter { tempCategory ->
                val dbCategory =
                    categories.find { it.name == tempCategory.name }
                dbCategory != null
            }
            .map { tempCategory -> tempCategory.uuid }

        val conflictingTempCounterParties = tempCounterParties
            .filter { tempCounterParty ->
                val dbCounterParty =
                    counterParties.find { it.name == tempCounterParty.name }
                dbCounterParty != null
            }
            .map { tempCounterParty -> tempCounterParty.uuid }

        val conflictingTempMethods = tempMethods
            .filter { tempMethod ->
                val dbMethod =
                    methods.find { it.name == tempMethod.name }
                dbMethod != null
            }
            .map { tempMethod -> tempMethod.uuid }

        val conflictingTempSources = tempSources
            .filter { tempSource ->
                val dbSource =
                    sources.find { it.name == tempSource.name }
                dbSource != null
            }
            .map { tempSource -> tempSource.uuid }

        val categoryMappings = tempCategories.associateBy(
            { tempCategory ->
                tempCategory.uuid
            },
            { tempCategory ->
                val dbCategory = categories
                    .find { category -> category.name == tempCategory.name }
                dbCategory ?: Category()
            }
        )

        val counterPartyMappings = tempCounterParties.associateBy(
            { tempCounterParty ->
                tempCounterParty.uuid
            },
            { tempCounterParty ->
                val dbCounterParty = counterParties
                    .find { counterParty -> counterParty.name == tempCounterParty.name }
                dbCounterParty ?: CounterParty()
            }
        )

        val methodMappings = tempMethods.associateBy(
            { tempMethod ->
                tempMethod.uuid
            },
            { tempMethod ->
                val dbMethod = methods.find { method -> method.name == tempMethod.name }
                dbMethod ?: Method()
            }
        )

        val sourceMappings = tempSources.associateBy(
            { tempSource ->
                tempSource.uuid
            },
            { tempSource ->
                val dbSource = sources.find { source -> source.name == tempSource.name }
                dbSource ?: Source()
            }
        )

        val currencySourceMap = sources.groupBy { source -> source.currency }

        val selectedTempCategories = tempCategories
            .map { it.uuid }
            .toSet()

        val selectedTempCounterParties = tempCounterParties
            .map { it.uuid }
            .toSet()

        val selectedTempMethods = tempMethods
            .map { it.uuid }
            .toSet()

        val selectedTempSources = tempSources
            .map { it.uuid }
            .toSet()

        _primaryState.update {
            it.copy(
                dbCategories = categories,
                tempCategories = tempCategories,
                categoryMappings = categoryMappings,
                conflictingTempCategories = conflictingTempCategories.toSet(),
                selectedTempCategories = selectedTempCategories,
                selectedTempCategoryCount = numberFormatter.format(selectedTempCategories.size)
                    .toString(),
                dbCounterParties = counterParties,
                tempCounterParties = tempCounterParties,
                counterPartyMappings = counterPartyMappings,
                conflictingTempCounterParties = conflictingTempCounterParties.toSet(),
                selectedTempCounterParties = selectedTempCounterParties,
                selectedTempCounterPartyCount = numberFormatter.format(selectedTempCounterParties.size)
                    .toString(),
                dbMethods = methods,
                tempMethods = tempMethods,
                methodMappings = methodMappings,
                conflictingTempMethods = conflictingTempMethods.toSet(),
                selectedTempMethods = selectedTempMethods,
                selectedTempMethodCount = numberFormatter.format(selectedTempMethods.size)
                    .toString(),
                currencySourceMap = currencySourceMap,
                tempSources = tempSources,
                sourceMappings = sourceMappings,
                conflictingTempSources = conflictingTempSources.toSet(),
                selectedTempSources = selectedTempSources,
                selectedTempSourceCount = numberFormatter.format(selectedTempSources.size)
                    .toString(),
                tempTransactionTemplates = tempTransactionTemplates,
                tempTransactionTemplatesWithIcons = getTemplateWithIcons(
                    templates = tempTransactionTemplates
                )
            )
        }

        _filtersState.update {
            it.copy(
                selectedCurrencyCount = numberFormatter.format(it.selectedCurrencies.size)
                    .toString()
            )
        }

        loadTransactionsJob = loadTransactions()
        updateSecondaryState()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun loadTransactions() = viewModelScope.launch {
        combine(
            _searchState
                .onEach {
                    _primaryState.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
                .debounce(300),
            _sortOptionsState
                .onEach {
                    _primaryState.update {
                        it.copy(
                            loading = true
                        )
                    }
                },
            _filtersState
                .onEach {
                    _primaryState.update {
                        it.copy(
                            loading = true
                        )
                    }
                }
        ) { searchText, sortOptions, filters ->
            Triple(searchText, sortOptions, filters)
        }.flatMapLatest { (searchText, sortOptions, filters) ->
            getTempTransactionQuery(
                searchText,
                sortOptions,
                filters
            )
        }.collectLatest { transactionChanges ->
            val filteredTransactionsMap = transactionChanges.list.groupBy(
                keySelector = { tempTransaction -> tempTransaction.time.toLocalDate() },
                valueTransform = { tempTransaction ->
                    getTempTransactionWithIcons(
                        tempTransaction,
                        searchState.value
                    )
                }
            )

            _primaryState.update {
                it.copy(
                    transactionsHashCode = transactionChanges.list.hashCode(),
                    tempTransactions = transactionChanges.list,
                    filteredTransactions = filteredTransactionsMap,
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    private fun updateSecondaryState() = viewModelScope.launch {
        primaryState.collectLatest { mainScreenState ->
            val enabledTempTransactions = getEnabledTempTransactions(
                selectedTempCategories = mainScreenState.selectedTempCategories,
                selectedTempCounterParties = mainScreenState.selectedTempCounterParties,
                selectedTempMethods = mainScreenState.selectedTempMethods,
                selectedTempSources = mainScreenState.selectedTempSources,
                tempTransactions = mainScreenState.tempTransactions
            )

            val enabledTempTransactionTemplates = getEnabledTempTransactionTemplates(
                selectedTempCategories = mainScreenState.selectedTempCategories,
                selectedTempCounterParties = mainScreenState.selectedTempCounterParties,
                selectedTempMethods = mainScreenState.selectedTempMethods,
                selectedTempSources = mainScreenState.selectedTempSources,
                tempTransactionTemplates = mainScreenState.tempTransactionTemplates
            )

            val enabledLocalDates = getEnabledLocalDates(
                filteredTransactions = mainScreenState.filteredTransactions,
                enabledTempTransactions = enabledTempTransactions
            )

            val selectedLocalDates = getSelectedLocalDates(
                filteredTransactions = mainScreenState.filteredTransactions,
                enabledTransactions = enabledTempTransactions,
                selectedTransactions = mainScreenState.selectedTempTransactions
            )

            val allSelected = getAllSelected(
                filteredTransactions = mainScreenState.filteredTransactions,
                enabledTransactions = enabledTempTransactions,
                selectedTransactions = mainScreenState.selectedTempTransactions
            )

            _secondaryState.update {
                it.copy(
                    enabledTempTransactions = enabledTempTransactions,
                    enabledTempTransactionTemplates = enabledTempTransactionTemplates,
                    enabledLocalDates = enabledLocalDates,
                    selectedLocalDates = selectedLocalDates,
                    allSelected = allSelected
                )
            }
        }
    }

    fun importData() = viewModelScope.launch {
        loadDataJob?.cancelAndJoin()
        _primaryState.update {
            it.copy(
                loading = true,
                enabled = false
            )
        }

        var result: Event

        val tempCategories = primaryState.value.tempCategories
        val selectedTempCategoryUUIDs = primaryState.value.selectedTempCategories
        val categoryMappings = primaryState.value.categoryMappings
            .filter { entry -> selectedTempCategoryUUIDs.contains(entry.key) }
            .toMutableMap()

        val tempCounterParties = primaryState.value.tempCounterParties
        val selectedTempCounterPartyUUIDs = primaryState.value.selectedTempCounterParties
        val counterPartyMappings = primaryState.value.counterPartyMappings
            .filter { entry -> selectedTempCounterPartyUUIDs.contains(entry.key) }
            .toMutableMap()

        val tempMethods = primaryState.value.tempMethods
        val selectedTempMethodUUIDs = primaryState.value.selectedTempMethods
        val methodMappings = primaryState.value.methodMappings
            .filter { entry -> selectedTempMethodUUIDs.contains(entry.key) }
            .toMutableMap()

        val tempSources = primaryState.value.tempSources
        val selectedTempSourceUUIDs = primaryState.value.selectedTempSources
        val sourceMappings = primaryState.value.sourceMappings
            .filter { entry -> selectedTempSourceUUIDs.contains(entry.key) }
            .toMutableMap()
        val restoreBalanceSources = primaryState.value.restoreBalanceSources

        val enabledTempTransactionTemplateUUIDs =
            secondaryState.value.enabledTempTransactionTemplates
        val selectedTempTransactionTemplateUUIDs =
            primaryState.value.selectedTempTransactionTemplates
                .filter { uuid -> enabledTempTransactionTemplateUUIDs.contains(uuid) }
        val tempTransactionTemplates = primaryState.value.tempTransactionTemplates
            .filter { tempTransactionTemplate ->
                selectedTempTransactionTemplateUUIDs.contains(
                    tempTransactionTemplate.uuid
                )
            }

        val enabledTempTransactionUUIDs = secondaryState.value.enabledTempTransactions
        val selectedTempTransactionUUIDs = _primaryState.value.selectedTempTransactions
            .filter { uuid -> enabledTempTransactionUUIDs.contains(uuid) }
        val tempTransactions = primaryState.value.tempTransactions
            .filter { tempTransaction -> selectedTempTransactionUUIDs.contains(tempTransaction.uuid) }

        try {
            realm.write {
                categoryMappings
                    .entries
                    .filter { (_, category) -> category.uuid.isNotEmpty() }
                    .distinctBy { (_, category) -> category.uuid }
                    .forEach { (tempCategoryUUID, category) ->
                        val filteredTransactions = tempTransactions
                            .filter { tempTransaction -> tempTransaction.category?.uuid == tempCategoryUUID }
                        findLatest(category)?.also {
                            it.frequency += filteredTransactions.size
                            it.lastUsed = maxOf(
                                it.lastUsed,
                                filteredTransactions.maxOf { tempTransaction -> tempTransaction.time }
                            )
                        }
                    }

                categoryMappings
                    .filter { entry -> entry.value.uuid.isEmpty() }
                    .forEach { entry ->
                        val tempCategory =
                            tempCategories.find { tempCategory -> tempCategory.uuid == entry.key }
                        if (tempCategory != null) {
                            val filteredTransactions = tempTransactions
                                .filter { tempTransaction -> tempTransaction.category?.uuid == tempCategory.uuid }
                            val category = copyToRealm(
                                Category().apply {
                                    uuid = UUID.randomUUID().toString()
                                    name = tempCategory.name
                                    icon = tempCategory.icon
                                    frequency = filteredTransactions.size
                                    lastUsed = filteredTransactions
                                        .maxOf { tempTransaction -> tempTransaction.time }
                                }
                            )
                            categoryMappings[tempCategory.uuid] = category
                        }
                    }

                counterPartyMappings
                    .entries
                    .filter { (_, counterParty) -> counterParty.uuid.isNotEmpty() }
                    .distinctBy { (_, counterParty) -> counterParty.uuid }
                    .forEach { (tempCounterPartyUUID, counterParty) ->
                        val filteredTransactions = tempTransactions
                            .filter { tempTransaction -> tempTransaction.counterParty?.uuid == tempCounterPartyUUID }
                        findLatest(counterParty)?.also {
                            it.frequency += filteredTransactions.size
                            it.lastUsed = maxOf(
                                it.lastUsed,
                                filteredTransactions.maxOf { tempTransaction -> tempTransaction.time }
                            )
                        }
                    }

                counterPartyMappings
                    .filter { entry -> entry.value.uuid.isEmpty() }
                    .forEach { entry ->
                        val tempCounterParty =
                            tempCounterParties.find { tempCounterParty -> tempCounterParty.uuid == entry.key }
                        if (tempCounterParty != null) {
                            val filteredTransactions = tempTransactions
                                .filter { tempTransaction -> tempTransaction.counterParty?.uuid == tempCounterParty.uuid }
                            val counterParty = copyToRealm(
                                CounterParty().apply {
                                    uuid = UUID.randomUUID().toString()
                                    name = tempCounterParty.name
                                    frequency = filteredTransactions.size
                                    lastUsed = filteredTransactions
                                        .maxOf { tempTransaction -> tempTransaction.time }
                                }
                            )
                            counterPartyMappings[tempCounterParty.uuid] = counterParty
                        }
                    }

                methodMappings
                    .entries
                    .filter { (_, method) -> method.uuid.isNotEmpty() }
                    .distinctBy { (_, method) -> method.uuid }
                    .forEach { (tempMethodUUID, method) ->
                        val filteredTransactions = tempTransactions
                            .filter { tempTransaction -> tempTransaction.category?.uuid == tempMethodUUID }
                        findLatest(method)?.also {
                            it.frequency += filteredTransactions.size
                            it.lastUsed = maxOf(
                                it.lastUsed,
                                filteredTransactions.maxOf { tempTransaction -> tempTransaction.time }
                            )
                        }
                    }

                methodMappings
                    .filter { entry -> entry.value.uuid.isEmpty() }
                    .forEach { entry ->
                        val tempMethod =
                            tempMethods.find { tempMethod -> tempMethod.uuid == entry.key }
                        if (tempMethod != null) {
                            val filteredTransactions = tempTransactions
                                .filter { tempTransaction -> tempTransaction.method?.uuid == tempMethod.uuid }
                            val method = copyToRealm(
                                Method().apply {
                                    uuid = UUID.randomUUID().toString()
                                    name = tempMethod.name
                                    frequency = filteredTransactions.size
                                    lastUsed = filteredTransactions
                                        .maxOf { tempTransaction -> tempTransaction.time }
                                }
                            )
                            methodMappings[tempMethod.uuid] = method
                        }
                    }

                sourceMappings
                    .entries
                    .filter { (_, source) -> source.uuid.isNotEmpty() }
                    .distinctBy { (_, source) -> source.uuid }
                    .forEach { (tempSourceUUID, source) ->
                        val filteredTransactions = tempTransactions
                            .filter { tempTransaction -> tempTransaction.category?.uuid == tempSourceUUID }
                        findLatest(source)?.also {
                            it.frequency += filteredTransactions.size
                            it.lastUsed = maxOf(
                                it.lastUsed,
                                filteredTransactions.maxOf { tempTransaction -> tempTransaction.time }
                            )
                        }
                    }

                sourceMappings
                    .filter { entry -> restoreBalanceSources.contains(entry.key) }
                    .forEach { entry ->
                        val tempSource =
                            tempSources.find { tempSource -> tempSource.uuid == entry.key }
                        if (tempSource != null) {
                            findLatest(entry.value)?.also {
                                it.balance = tempSource.balance
                            }
                        }
                    }

                sourceMappings
                    .filter { entry -> entry.value.uuid.isEmpty() }
                    .forEach { entry ->
                        val tempSource =
                            tempSources.find { tempSource -> tempSource.uuid == entry.key }
                        if (tempSource != null) {
                            val filteredTransactions = tempTransactions
                                .filter { tempTransaction -> tempTransaction.source?.uuid == tempSource.uuid }
                            val source = copyToRealm(
                                Source().apply {
                                    uuid = UUID.randomUUID().toString()
                                    name = tempSource.name
                                    balance = tempSource.balance
                                    currency = tempSource.currency
                                    frequency = filteredTransactions.size
                                    lastUsed = filteredTransactions
                                        .maxOf { tempTransaction -> tempTransaction.time }
                                }
                            )
                            sourceMappings[tempSource.uuid] = source
                        }
                    }

                tempTransactionTemplates.forEach { tempTransactionTemplate ->
                    val category = categoryMappings[tempTransactionTemplate.category?.uuid]
                    val counterParty =
                        counterPartyMappings[tempTransactionTemplate.counterParty?.uuid]
                    val method = methodMappings[tempTransactionTemplate.method?.uuid]
                    val source = sourceMappings[tempTransactionTemplate.source?.uuid]

                    copyToRealm(
                        TransactionTemplate().apply {
                            this.uuid = UUID.randomUUID().toString()
                            this.name = tempTransactionTemplate.name
                            this.title = tempTransactionTemplate.title
                            this.description = tempTransactionTemplate.description
                            this.amount = tempTransactionTemplate.amount
                            this.type = tempTransactionTemplate.type
                            this.category = if (category != null) findLatest(category) else null
                            this.counterParty =
                                if (counterParty != null) findLatest(counterParty) else null
                            this.method = if (method != null) findLatest(method) else null
                            this.source = if (source != null) findLatest(source) else null
                            this.frequency = tempTransactionTemplate.frequency
                            this.lastUsed = tempTransactionTemplate.lastUsed
                        }
                    )
                }

                tempTransactions.forEach { tempTransaction ->
                    val category = categoryMappings[tempTransaction.category?.uuid]
                    val counterParty = counterPartyMappings[tempTransaction.counterParty?.uuid]
                    val method = methodMappings[tempTransaction.method?.uuid]
                    val source = sourceMappings[tempTransaction.source?.uuid]

                    if (category != null && method != null && source != null) {
                        copyToRealm(
                            Transaction().apply {
                                this.uuid = UUID.randomUUID().toString()
                                this.title = tempTransaction.title
                                this.description = tempTransaction.description
                                this.amount = tempTransaction.amount
                                this.time = tempTransaction.time
                                this.currency = tempTransaction.currency
                                this.type = tempTransaction.type
                                this.category = findLatest(category)
                                this.counterParty =
                                    if (counterParty != null) findLatest(counterParty) else null
                                this.method = findLatest(method)
                                this.source = findLatest(source)
                            }
                        )
                    }
                }

                clearTempData()
            }
            result = Event.ImportSuccess
        } catch (exception: Exception) {
            result = Event.InternalError
        }

        _event.send(result)

        _primaryState.update {
            it.copy(
                loading = false
            )
        }
    }

    //region Transaction

    private fun getTempTransactionQuery(
        searchText: String,
        sortOptions: SortOptionsScreenState,
        filters: ImportBackupScreenFiltersState
    ): Flow<ResultsChange<TempTransaction>> {
        var queryString =
            "time >= $0 && time <= $1 && currency==$2 && amount >= $3 && amount <= $4 && typeId IN $5"
        if (searchText.isNotBlank()) {
            queryString += " && (title CONTAINS[c] '${searchText.trim()}' || description CONTAINS[c] '${searchText.trim()}')"
        }
        return realm
            .query<TempTransaction>(
                queryString,
                filters.startDate?.toEpochMilli() ?: 0L,
                filters.endDate?.toEpochMilli() ?: Long.MAX_VALUE,
                filters.selectedCurrencies,
                filters.filterAmountMin,
                if (filters.filterAmountMax <= filters.filterAmountMin) Double.MAX_VALUE else filters.filterAmountMax,
                filters.selectedTransactionTypes
            )
            .sort("time", sortOptions.sortDirection)
            .asFlow()
    }

    private fun getTempTransactionWithIcons(
        tempTransaction: TempTransaction,
        searchText: String
    ): TransactionWithIcons {
        val category = tempTransaction.category!!
        val counterParty = tempTransaction.counterParty
        val method = tempTransaction.method!!
        val source = tempTransaction.source!!
        val currencyFormatter = currencyFormatterMap.getValue(source.currency)
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
                icon = Constants.DEFAULT_SOURCE_ICON,
                value = source.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = TablerIcons.Alarm,
                value = tempTransaction.time.format(DateTimeFormatter.ofPattern("hh:mm a")),
                resId = R.string.placeholder
            )
        )
        return TransactionWithIcons(
            uuid = tempTransaction.uuid,
            annotatedTitle = getHighlightedString(tempTransaction.title, searchText),
            annotatedDescription = getHighlightedString(tempTransaction.description, searchText),
            amount = currencyFormatter.format(tempTransaction.amount).toString(),
            typeIcon = tempTransaction.type.icon,
            typeIconColor = tempTransaction.type.color,
            currency = tempTransaction.currency,
            chips = chips
        )
    }

    private fun getEnabledTempTransactions(
        selectedTempCategories: Set<String>,
        selectedTempCounterParties: Set<String>,
        selectedTempMethods: Set<String>,
        selectedTempSources: Set<String>,
        tempTransactions: List<TempTransaction>,
    ): Set<String> {
        return tempTransactions
            .filter { tempTransaction ->
                val categoryValid = selectedTempCategories.contains(tempTransaction.category?.uuid)
                val counterPartyValid =
                    tempTransaction.counterParty == null || selectedTempCounterParties.contains(
                        tempTransaction.counterParty?.uuid
                    )
                val methodValid = selectedTempMethods.contains(tempTransaction.method?.uuid)
                val sourceValid = selectedTempSources.contains(tempTransaction.source?.uuid)
                categoryValid && counterPartyValid && methodValid && sourceValid
            }
            .map { tempTransaction -> tempTransaction.uuid }
            .toSet()
    }

    private fun getEnabledLocalDates(
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>,
        enabledTempTransactions: Set<String>
    ): Set<LocalDate> {
        return filteredTransactions
            .filter { entry ->
                entry.value
                    .map { transactionWithIcons -> transactionWithIcons.uuid }
                    .any { uuid -> enabledTempTransactions.contains(uuid) }
            }
            .keys
    }

    private fun getSelectedLocalDates(
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>,
        enabledTransactions: Set<String>,
        selectedTransactions: Set<String>
    ): Set<LocalDate> {
        return filteredTransactions
            .filter { entry ->
                val transactionUUIDs = entry.value
                    .filter { transactionWithIcons ->
                        enabledTransactions.contains(
                            transactionWithIcons.uuid
                        )
                    }
                    .map { transactionWithIcons -> transactionWithIcons.uuid }
                transactionUUIDs.isNotEmpty() && transactionUUIDs.all { uuid ->
                    selectedTransactions.contains(
                        uuid
                    )
                }
            }.keys
    }

    private fun getAllSelected(
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>,
        enabledTransactions: Set<String>,
        selectedTransactions: Set<String>
    ): Boolean {
        return filteredTransactions
            .values
            .flatten()
            .filter { transactionWithIcons -> enabledTransactions.contains(transactionWithIcons.uuid) }
            .map { transaction -> transaction.uuid }
            .all { uuid -> selectedTransactions.contains(uuid) }
    }

    //endregion

    //region Template

    private fun getEnabledTempTransactionTemplates(
        selectedTempCategories: Set<String>,
        selectedTempCounterParties: Set<String>,
        selectedTempMethods: Set<String>,
        selectedTempSources: Set<String>,
        tempTransactionTemplates: List<TempTransactionTemplate>,
    ): Set<String> {
        return tempTransactionTemplates
            .filter { tempTransactionTemplate ->
                val categoryValid =
                    tempTransactionTemplate.category == null || selectedTempCategories.contains(
                        tempTransactionTemplate.category?.uuid
                    )
                val counterPartyValid =
                    tempTransactionTemplate.counterParty == null || selectedTempCounterParties.contains(
                        tempTransactionTemplate.counterParty?.uuid
                    )
                val methodValid =
                    tempTransactionTemplate.method == null || selectedTempMethods.contains(
                        tempTransactionTemplate.method?.uuid
                    )
                val sourceValid =
                    tempTransactionTemplate.source == null || selectedTempSources.contains(
                        tempTransactionTemplate.source?.uuid
                    )
                categoryValid && counterPartyValid && methodValid && sourceValid
            }
            .map { tempTransactionTemplate -> tempTransactionTemplate.uuid }
            .toSet()
    }

    fun toggleTransactionTemplateSelected(tempTransactionTemplateUUID: String) {
        _primaryState.update {
            val selectedTempTransactionTemplates =
                it.selectedTempTransactionTemplates.toMutableSet()

            if (selectedTempTransactionTemplates.contains(tempTransactionTemplateUUID)) {
                selectedTempTransactionTemplates.remove(tempTransactionTemplateUUID)
            } else {
                selectedTempTransactionTemplates.add(tempTransactionTemplateUUID)
            }

            it.copy(
                selectedTempTransactionTemplates = selectedTempTransactionTemplates,
                selectedTempTransactionTemplateCount = numberFormatter.format(
                    selectedTempTransactionTemplates.size
                ).toString()
            )
        }
    }

    private fun getTemplateWithIcons(
        templates: List<TempTransactionTemplate>
    ): List<TransactionTemplateWithIcons> {
        val hasBeenUsedComparator = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())
        return templates.map { template ->
            val category = template.category
            val counterParty = template.counterParty
            val method = template.method
            val source = template.source
            val chips: MutableList<ChipInfo> = mutableListOf()
            if (template.title.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.title,
                        icon = TablerIcons.Typography
                    )
                )
            }
            if (template.description.isNotEmpty()) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = template.description,
                        icon = TablerIcons.FileText
                    )
                )
            }
            if (category != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = category.name,
                        icon = category.icon
                    )
                )
            }
            if (counterParty != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = counterParty.name,
                        icon = Constants.DEFAULT_COUNTERPARTY_ICON
                    )
                )
            }
            if (method != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = method.name,
                        icon = Constants.DEFAULT_METHOD_ICON
                    )
                )
            }
            if (source != null) {
                chips.add(
                    ChipInfo(
                        resId = R.string.placeholder,
                        value = source.name,
                        icon = Constants.DEFAULT_SOURCE_ICON
                    )
                )
            }
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = numberFormatter.format(template.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (template.lastUsed > hasBeenUsedComparator) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = template.lastUsed.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                        icon = TablerIcons.History
                    )
                )
            } else {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_never_label,
                        value = "",
                        icon = TablerIcons.History
                    )
                )
            }
            val formattedAmount = if (template.amount > 0) {
                if (source != null) {
                    currencyFormatterMap.getValue(source.currency).format(template.amount)
                        .toString()
                } else {
                    numberFormatter.format(template.amount).toString()
                }
            } else {
                ""
            }
            TransactionTemplateWithIcons(
                uuid = template.uuid,
                annotatedName = getHighlightedString(template.name, ""),
                amount = formattedAmount,
                typeIcon = template.type.icon,
                typeIconColor = template.type.color,
                chips = chips
            )
        }
    }

    //endregion

    //region Category

    fun setCategoryMapping(tempCategoryUUID: String, dbCategory: Category) {
        _primaryState.update {
            val categoryMappings = it.categoryMappings.toMutableMap()
            categoryMappings[tempCategoryUUID] = dbCategory

            it.copy(
                categoryMappings = categoryMappings
            )
        }
    }

    fun toggleCategorySelected(uuid: String) {
        _primaryState.update {
            val selectedTempCategories = it.selectedTempCategories.toMutableSet()

            if (selectedTempCategories.contains(uuid)) {
                selectedTempCategories.remove(uuid)
            } else {
                selectedTempCategories.add(uuid)
            }

            it.copy(
                selectedTempCategories = selectedTempCategories,
                selectedTempCategoryCount = numberFormatter.format(selectedTempCategories.size)
                    .toString()
            )
        }
    }

    //endregion

    //region CounterParty

    fun setSelectedCounterParty(tempCounterPartyUUID: String, dbCounterParty: CounterParty) {
        _primaryState.update {
            val counterPartyMappings = it.counterPartyMappings.toMutableMap()
            counterPartyMappings[tempCounterPartyUUID] = dbCounterParty

            it.copy(
                counterPartyMappings = counterPartyMappings
            )
        }
    }

    fun toggleCounterPartySelected(uuid: String) {
        _primaryState.update {
            val selectedTempCounterParties = it.selectedTempCounterParties.toMutableSet()

            if (selectedTempCounterParties.contains(uuid)) {
                selectedTempCounterParties.remove(uuid)
            } else {
                selectedTempCounterParties.add(uuid)
            }

            it.copy(
                selectedTempCounterParties = selectedTempCounterParties,
                selectedTempCounterPartyCount = numberFormatter.format(selectedTempCounterParties.size)
                    .toString()
            )
        }
    }

    //endregion

    //region Method

    fun setSelectedMethod(tempMethodUUID: String, dbMethod: Method) {
        _primaryState.update {
            val methodMappings = it.methodMappings.toMutableMap()
            methodMappings[tempMethodUUID] = dbMethod

            it.copy(
                methodMappings = methodMappings
            )
        }
    }

    fun toggleMethodSelected(uuid: String) {
        _primaryState.update {
            val selectedTempMethods = it.selectedTempMethods.toMutableSet()

            if (selectedTempMethods.contains(uuid)) {
                selectedTempMethods.remove(uuid)
            } else {
                selectedTempMethods.add(uuid)
            }

            it.copy(
                selectedTempMethods = selectedTempMethods,
                selectedTempMethodCount = numberFormatter.format(selectedTempMethods.size)
                    .toString()
            )
        }
    }

    //endregion

    //region Source

    fun setSelectedSource(tempSourceUUID: String, dbSource: Source) {
        _primaryState.update {
            val sourceMappings = it.sourceMappings.toMutableMap()
            sourceMappings[tempSourceUUID] = dbSource

            val restoreBalanceSources = it.restoreBalanceSources.toMutableSet()
            restoreBalanceSources.remove(tempSourceUUID)

            it.copy(
                sourceMappings = sourceMappings,
                restoreBalanceSources = restoreBalanceSources
            )
        }
    }

    fun toggleSourceSelected(uuid: String) {
        _primaryState.update {
            val selectedTempSources = it.selectedTempSources.toMutableSet()

            if (selectedTempSources.contains(uuid)) {
                selectedTempSources.remove(uuid)
            } else {
                selectedTempSources.add(uuid)
            }

            it.copy(
                selectedTempSources = selectedTempSources,
                selectedTempSourceCount = numberFormatter.format(selectedTempSources.size)
                    .toString()
            )
        }
    }

    fun toggleRestoreBalance(tempSourceUUID: String) {
        _primaryState.update {
            val restoreBalanceSources = it.restoreBalanceSources.toMutableSet()

            if (!restoreBalanceSources.contains(tempSourceUUID)) {
                val mappedSource = it.sourceMappings[tempSourceUUID]
                val sameMappedSourceUUIDs = it.sourceMappings
                    .filter { entry ->
                        entry.key != tempSourceUUID && entry.value.uuid == mappedSource?.uuid
                    }
                    .keys
                restoreBalanceSources.removeAll(sameMappedSourceUUIDs)
                restoreBalanceSources.add(tempSourceUUID)
            } else {
                restoreBalanceSources.remove(tempSourceUUID)
            }

            it.copy(
                restoreBalanceSources = restoreBalanceSources
            )
        }
    }

    //endregion

    fun checkPageValidity(currentPage: Int) = viewModelScope.launch {
        val mainState = _primaryState.value
        var eventToSend: Event? = null
        if (currentPage == 0) {
            val atLeastOneRestoreCategorySelected = mainState.selectedTempCategories.isNotEmpty()
            if (atLeastOneRestoreCategorySelected) {
                if (
                    mainState.tempCategories
                        .filter { tempCategory ->
                            mainState.selectedTempCategories.contains(tempCategory.uuid) && mainState.conflictingTempCategories.contains(
                                tempCategory.uuid
                            )
                        }
                        .any { tempCategory ->
                            val mappedCategory = mainState.categoryMappings[tempCategory.uuid]
                            mappedCategory != null && mappedCategory.uuid.isEmpty()
                        }
                ) {
                    eventToSend = Event.MappingInvalid
                }
            } else {
                eventToSend = Event.CategoryMapRequired
            }
        }
        if (
            currentPage == 1 &&
            mainState.tempCounterParties
                .filter { tempCounterParty ->
                    mainState.selectedTempCounterParties.contains(tempCounterParty.uuid) && mainState.conflictingTempCounterParties.contains(
                        tempCounterParty.uuid
                    )
                }
                .any { tempCounterParty ->
                    val mappedCounterParty = mainState.counterPartyMappings[tempCounterParty.uuid]
                    mappedCounterParty != null && mappedCounterParty.uuid.isEmpty()
                }
        ) {
            eventToSend = Event.MappingInvalid
        }
        if (currentPage == 2) {
            val atLeastOneRestoreMethodSelected = mainState.selectedTempMethods.isNotEmpty()
            if (atLeastOneRestoreMethodSelected) {
                if (
                    mainState.tempMethods
                        .filter { tempMethod ->
                            mainState.selectedTempMethods.contains(tempMethod.uuid) && mainState.conflictingTempMethods.contains(
                                tempMethod.uuid
                            )
                        }
                        .any { tempMethod ->
                            val mappedMethod = mainState.methodMappings[tempMethod.uuid]
                            mappedMethod != null && mappedMethod.uuid.isEmpty()
                        }
                ) {
                    eventToSend = Event.MappingInvalid
                }
            } else {
                eventToSend = Event.MethodMapRequired
            }
        }
        if (currentPage == 3) {
            val atLeastOneRestoreSourceSelected = mainState.selectedTempSources.isNotEmpty()
            if (atLeastOneRestoreSourceSelected) {
                if (
                    mainState.tempSources
                        .filter { tempSource ->
                            mainState.selectedTempSources.contains(tempSource.uuid) && mainState.conflictingTempSources.contains(
                                tempSource.uuid
                            )
                        }
                        .any { tempSource ->
                            val mappedSource = mainState.sourceMappings[tempSource.uuid]
                            mappedSource != null && mappedSource.uuid.isEmpty()
                        }
                ) {
                    eventToSend = Event.MappingInvalid
                }
            } else {
                eventToSend = Event.SourceMapRequired
            }
        }
        _event.send(eventToSend)
    }

    fun toggleLocalDateSelected(
        localDateSelected: Boolean,
        transactions: List<TransactionWithIcons>
    ) {
        _primaryState.update {
            val transactionUUIDs = transactions
                .map { transactionWithIcons -> transactionWithIcons.uuid }
                .toSet()

            val selectedTempTransactions = it.selectedTempTransactions.toMutableSet()

            if (localDateSelected) {
                selectedTempTransactions.removeAll(transactionUUIDs)
            } else {
                selectedTempTransactions.addAll(transactionUUIDs)
            }

            it.copy(
                selectedTempTransactions = selectedTempTransactions,
                selectedTransactionsCount = numberFormatter.format(selectedTempTransactions.size)
                    .toString()
            )
        }
    }

    fun toggleTransactionSelected(uuid: String) {
        _primaryState.update {
            val selectedTempTransactions = it.selectedTempTransactions.toMutableSet()

            if (selectedTempTransactions.contains(uuid)) {
                selectedTempTransactions.remove(uuid)
            } else {
                selectedTempTransactions.add(uuid)
            }

            it.copy(
                selectedTempTransactions = selectedTempTransactions,
                selectedTransactionsCount = numberFormatter.format(selectedTempTransactions.size)
                    .toString()
            )
        }
    }

    fun toggleSelection(allSelected: Boolean) {
        _primaryState.update {
            val selectedTempTransactions = it.selectedTempTransactions.toMutableSet()

            if (allSelected) {
                selectedTempTransactions.clear()
            } else {
                val transactionUUIDs = it.filteredTransactions
                    .values
                    .flatten()
                    .map { transaction -> transaction.uuid }

                selectedTempTransactions.addAll(transactionUUIDs)
            }

            it.copy(
                selectedTempTransactions = selectedTempTransactions,
                selectedTransactionsCount = numberFormatter.format(selectedTempTransactions.size)
                    .toString()
            )
        }
    }

    fun setSelectedCurrencies(selectedCurrencies: Set<String>) {
        _filtersState.update {
            it.copy(
                selectedCurrencies = selectedCurrencies,
                selectedCurrencyCount = numberFormatter.format(selectedCurrencies.size).toString()
            )
        }
    }

    fun setStartDateAndEndDate(startDate: ZonedDateTime?, endDate: ZonedDateTime?) {
        _filtersState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                startDateString = startDate?.getDateString(datePattern) ?: "",
                endDateString = endDate?.getDateString(datePattern) ?: "",
                dateRangeStringRes = if (startDate == null && endDate == null) {
                    R.string.all_time_date_range_label
                } else {
                    R.string.date_range_label
                }
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

    fun setSortDirection(sortDirection: Sort) {
        _sortOptionsState.update {
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
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        numberFormatter = getNumberFormatter(locale)
        _primaryState.update {
            it.copy(
                locale = locale
            )
        }
    }
}