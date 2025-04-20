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
import jp.ikigai.cash.flow.data.dto.ImportScreenFlows
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
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenEnabledState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenMainState
import jp.ikigai.cash.flow.ui.screenStates.common.restore.ImportBackupScreenSelectionState
import jp.ikigai.cash.flow.utils.combineTenFlows
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getEndOfDayInEpochMilli
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getIconForCategory
import jp.ikigai.cash.flow.utils.getIconForCounterParty
import jp.ikigai.cash.flow.utils.getIconForMethod
import jp.ikigai.cash.flow.utils.getIconForSource
import jp.ikigai.cash.flow.utils.getNumberFormatter
import jp.ikigai.cash.flow.utils.getStartOfDayInEpochMilli
import jp.ikigai.cash.flow.utils.toLocalDate
import jp.ikigai.cash.flow.utils.toZonedDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.buffer
import okio.source
import okio.use
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class ImportBackupScreenViewModel(
    private val realm: Realm = Realm.open(Database.config)
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private var loadDataJob: Job? = null

    //region StateFlows

    private val _mainState = MutableStateFlow(ImportBackupScreenMainState())
    val mainState: StateFlow<ImportBackupScreenMainState> = _mainState.asStateFlow()

    private val _selectionState = MutableStateFlow(ImportBackupScreenSelectionState())
    val selectionState: StateFlow<ImportBackupScreenSelectionState> = _selectionState.asStateFlow()

    private val _enabledState = MutableStateFlow(ImportBackupScreenEnabledState())
    val enabledState: StateFlow<ImportBackupScreenEnabledState> = _enabledState.asStateFlow()

    private val _selectedTransactionsState = MutableStateFlow(emptySet<String>())
    val selectedTransactionsState: StateFlow<Set<String>> = _selectedTransactionsState.asStateFlow()

    //endregion

    private val _event: Channel<Event?> = Channel(Int.MAX_VALUE)
    val event: Flow<Event?> = _event.receiveAsFlow()

    //region Realm Queries

    private val categoryQuery = realm.query<Category>().sort("frequency", Sort.DESCENDING)
    private val tempCategoryQuery = realm.query<TempCategory>().sort("frequency", Sort.DESCENDING)

    private val counterPartyQuery =
        realm.query<CounterParty>().sort("frequency", Sort.DESCENDING)
    private val tempCounterPartyQuery =
        realm.query<TempCounterParty>().sort("frequency", Sort.DESCENDING)

    private val methodQuery = realm.query<Method>().sort("frequency", Sort.DESCENDING)
    private val tempMethodQuery = realm.query<TempMethod>().sort("frequency", Sort.DESCENDING)

    private val sourceQuery = realm.query<Source>().sort("frequency", Sort.DESCENDING)
    private val tempSourceQuery = realm.query<TempSource>().sort("frequency", Sort.DESCENDING)

    private val tempTransactionTemplatesQuery =
        realm.query<TempTransactionTemplate>().sort("frequency", Sort.DESCENDING)

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
        _mainState.update {
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
                                lastUsed = category.lastUsed
                            }
                            val managedInstance = copyToRealm(tempCategory)
                            tempCategoryMap[tempCategory.uuid] = managedInstance
                        }
                        data?.counterParties?.forEach { counterParty ->
                            val tempCounterParty = TempCounterParty().apply {
                                uuid = counterParty.uuid
                                name = counterParty.name
                                icon = counterParty.iconName.getIconForCounterParty()
                                frequency = counterParty.frequency
                                lastUsed = counterParty.lastUsed
                            }
                            val managedInstance = copyToRealm(tempCounterParty)
                            tempCounterPartyMap[tempCounterParty.uuid] = managedInstance
                        }
                        data?.methods?.forEach { method ->
                            val tempMethod = TempMethod().apply {
                                uuid = method.uuid
                                name = method.name
                                icon = method.iconName.getIconForMethod()
                                frequency = method.frequency
                                lastUsed = method.lastUsed
                            }
                            val managedInstance = copyToRealm(tempMethod)
                            tempMethodMap[tempMethod.uuid] = managedInstance
                        }
                        data?.sources?.forEach { source ->
                            val tempSource = TempSource().apply {
                                uuid = source.uuid
                                name = source.name
                                icon = source.iconName.getIconForSource()
                                currency = source.currency
                                balance = source.balance
                                frequency = source.frequency
                                lastUsed = source.lastUsed
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
                                time = transaction.time
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
                                lastUsed = template.lastUsed
                            }
                            copyToRealm(tempTemplate)
                        }
                    }

                    _mainState.update {
                        it.copy(dataLoadComplete = true)
                    }

                    loadDataJob = loadData()
                    updateEnabledState()
                    updateSelectionState()
                } catch (exception: Exception) {
                    _event.send(Event.InternalError)
                    _mainState.update {
                        it.copy(loading = false)
                    }
                }
            }
        }
    }

    private fun loadData() = viewModelScope.launch {
        combineTenFlows(
            categoryQuery.asFlow(),
            counterPartyQuery.asFlow(),
            methodQuery.asFlow(),
            sourceQuery.asFlow(),
            tempCategoryQuery.asFlow(),
            tempCounterPartyQuery.asFlow(),
            tempMethodQuery.asFlow(),
            tempSourceQuery.asFlow(),
            tempTransactionTemplatesQuery.asFlow(),
            getTempTransactionQuery()
        ) { categoryChanges, counterPartyChanges, methodChanges, sourceChanges, tempCategoryChanges, tempCounterPartyChanges, tempMethodChanges, tempSourceChanges, tempTransactionTemplateChanges, tempTransactionChanges ->
            val tempSources = tempSourceChanges.list.toMutableList()
            val sources = sourceChanges.list.toMutableList()

            sources.forEach { source ->
                val formatter = currencyFormatterMap.getValue(source.currency)
                source.displayBalance = formatter.format(source.balance).toString()
            }

            tempSources.forEach { source ->
                val formatter = currencyFormatterMap.getValue(source.currency)
                source.displayBalance = formatter.format(source.balance).toString()
            }

            ImportScreenFlows(
                categories = categoryChanges.list,
                counterParties = counterPartyChanges.list,
                methods = methodChanges.list,
                sources = sources,
                tempCategories = tempCategoryChanges.list,
                tempCounterParties = tempCounterPartyChanges.list,
                tempMethods = tempMethodChanges.list,
                tempSources = tempSources,
                tempTransactionTemplates = tempTransactionTemplateChanges.list,
                tempTransactions = tempTransactionChanges.list
            )
        }.collectLatest { importScreenFlows ->
            val conflictingTempCategories = importScreenFlows.tempCategories
                .filter { tempCategory ->
                    val dbCategory =
                        importScreenFlows.categories.find { it.name == tempCategory.name }
                    dbCategory != null
                }
                .map { tempCategory -> tempCategory.uuid }

            val conflictingTempCounterParties = importScreenFlows.tempCounterParties
                .filter { tempCounterParty ->
                    val dbCounterParty =
                        importScreenFlows.counterParties.find { it.name == tempCounterParty.name }
                    dbCounterParty != null
                }
                .map { tempCounterParty -> tempCounterParty.uuid }

            val conflictingTempMethods = importScreenFlows.tempMethods
                .filter { tempMethod ->
                    val dbMethod =
                        importScreenFlows.methods.find { it.name == tempMethod.name }
                    dbMethod != null
                }
                .map { tempMethod -> tempMethod.uuid }

            val conflictingTempSources = importScreenFlows.tempSources
                .filter { tempSource ->
                    val dbSource =
                        importScreenFlows.sources.find { it.name == tempSource.name }
                    dbSource != null
                }
                .map { tempSource -> tempSource.uuid }

            val sourceMap = importScreenFlows.sources.groupBy { source -> source.currency }

            _mainState.update {
                val selectedTempCategories = getSelectedTempCategories(
                    tempCategories = importScreenFlows.tempCategories,
                    selectedTempCategories = it.selectedTempCategories
                )
                val categoryMappings = getCategoryMappings(
                    dbCategories = importScreenFlows.categories,
                    tempCategories = importScreenFlows.tempCategories,
                    categoryMappings = it.categoryMappings
                )

                val selectedTempCounterParties = getSelectedTempCounterParties(
                    tempCounterParties = importScreenFlows.tempCounterParties,
                    selectedTempCounterParties = it.selectedTempCounterParties
                )
                val counterPartyMappings = getCounterPartyMappings(
                    dbCounterParties = importScreenFlows.counterParties,
                    tempCounterParties = importScreenFlows.tempCounterParties,
                    counterPartyMappings = it.counterPartyMappings
                )

                val selectedTempMethods = getSelectedTempMethods(
                    tempMethods = importScreenFlows.tempMethods,
                    selectedTempMethods = it.selectedTempMethods
                )
                val methodMappings = getMethodMappings(
                    dbMethods = importScreenFlows.methods,
                    tempMethods = importScreenFlows.tempMethods,
                    methodMappings = it.methodMappings
                )

                val selectedTempSources = getSelectedTempSources(
                    tempSources = importScreenFlows.tempSources,
                    selectedTempSources = it.selectedTempSources
                )
                val sourceMappings = getSourceMappings(
                    dbSources = importScreenFlows.sources,
                    tempSources = importScreenFlows.tempSources,
                    sourceMappings = it.sourceMappings
                )

                val selectedCurrencies = it.selectedCurrencies.filter { entry -> entry.value }.keys
                val selectedTempCategoryCount = numberFormatter.format(
                    selectedTempCategories.filter { entry -> entry.value }.size
                ).toString()
                val selectedTempCounterPartyCount = numberFormatter.format(
                    selectedTempCounterParties.filter { entry -> entry.value }.size
                ).toString()
                val selectedTempMethodCount = numberFormatter.format(
                    selectedTempMethods.filter { entry -> entry.value }.size
                ).toString()
                val selectedTempSourceCount = numberFormatter.format(
                    selectedTempSources.filter { entry -> entry.value }.size
                ).toString()

                val filteredTransactionsMap = importScreenFlows.tempTransactions.groupBy(
                    keySelector = { tempTransaction -> tempTransaction.time.toLocalDate() },
                    valueTransform = { tempTransaction ->
                        getTempTransactionWithIcons(
                            tempTransaction,
                            it.searchText
                        )
                    }
                )

                it.copy(
                    transactionsHashCode = importScreenFlows.tempTransactions.hashCode(),
                    tempTransactions = importScreenFlows.tempTransactions,
                    filteredTransactions = filteredTransactionsMap,
                    dbCategories = importScreenFlows.categories,
                    tempCategories = importScreenFlows.tempCategories,
                    selectedTempCategories = selectedTempCategories,
                    selectedTempCategoryCount = selectedTempCategoryCount,
                    conflictingTempCategories = conflictingTempCategories.toSet(),
                    categoryMappings = categoryMappings,
                    dbCounterParties = importScreenFlows.counterParties,
                    tempCounterParties = importScreenFlows.tempCounterParties,
                    selectedTempCounterParties = selectedTempCounterParties,
                    selectedTempCounterPartyCount = selectedTempCounterPartyCount,
                    conflictingTempCounterParties = conflictingTempCounterParties.toSet(),
                    counterPartyMappings = counterPartyMappings,
                    dbMethods = importScreenFlows.methods,
                    tempMethods = importScreenFlows.tempMethods,
                    methodMappings = methodMappings,
                    conflictingTempMethods = conflictingTempMethods.toSet(),
                    selectedTempMethods = selectedTempMethods,
                    selectedTempMethodCount = selectedTempMethodCount,
                    currencySourceMap = sourceMap,
                    tempSources = importScreenFlows.tempSources,
                    sourceMappings = sourceMappings,
                    conflictingTempSources = conflictingTempSources.toSet(),
                    selectedTempSources = selectedTempSources,
                    selectedTempSourceCount = selectedTempSourceCount,
                    tempTransactionTemplates = importScreenFlows.tempTransactionTemplates,
                    tempTransactionTemplatesWithIcons = getTemplateWithIcons(
                        templates = importScreenFlows.tempTransactionTemplates
                    ),
                    selectedCurrencyCount = numberFormatter.format(selectedCurrencies.size)
                        .toString(),
                    loading = false,
                    enabled = true
                )
            }
        }
    }

    private fun updateEnabledState() = viewModelScope.launch {
        mainState.collectLatest { mainScreenState ->
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

            _enabledState.update {
                it.copy(
                    enabledTempTransactions = enabledTempTransactions,
                    enabledTempTransactionTemplates = enabledTempTransactionTemplates,
                    enabledLocalDates = enabledLocalDates
                )
            }
        }
    }

    private fun updateSelectionState() = viewModelScope.launch {
        combine(
            mainState,
            enabledState,
            selectedTransactionsState
        ) { mainScreenState, enabledScreenState, selectedTransactionsScreenState ->
            Triple(mainScreenState, enabledScreenState, selectedTransactionsScreenState)
        }.collectLatest { (mainScreenState, enabledScreenState, selectedTransactions) ->
            val selectedTransactionsCount = selectedTransactions
                .filter { uuid -> enabledScreenState.enabledTempTransactions.contains(uuid) }
                .size

            val selectedTempTransactionTemplateCount =
                mainScreenState.selectedTempTransactionTemplates
                    .filter { uuid ->
                        enabledScreenState.enabledTempTransactionTemplates.contains(
                            uuid
                        )
                    }
                    .size

            val selectedLocalDates = getSelectedLocalDates(
                filteredTransactions = mainScreenState.filteredTransactions,
                enabledTransactions = enabledScreenState.enabledTempTransactions,
                selectedTransactions = selectedTransactions
            )

            val allSelected = getAllSelected(
                filteredTransactions = mainScreenState.filteredTransactions,
                enabledTransactions = enabledScreenState.enabledTempTransactions,
                selectedTransactions = selectedTransactions
            )

            _selectionState.update {
                it.copy(
                    selectedTransactionsCount = if (selectedTransactionsCount > 0) numberFormatter.format(
                        selectedTransactionsCount
                    ).toString() else "",
                    selectedTempTransactionTemplateCount = numberFormatter.format(
                        selectedTempTransactionTemplateCount
                    ).toString(),
                    selectedLocalDates = selectedLocalDates,
                    allSelected = allSelected
                )
            }
        }
    }

    fun importData() = viewModelScope.launch {
        loadDataJob?.cancelAndJoin()
        _mainState.update {
            it.copy(
                importOngoing = true,
                enabled = false
            )
        }
        val startTime = System.currentTimeMillis()
        var result: Event

        val tempCategories = mainState.value.tempCategories
        val selectedTempCategoryUUIDs = mainState.value.selectedTempCategories
            .filter { entry -> entry.value }.keys
        val categoryMappings = mainState.value.categoryMappings
            .filter { entry -> selectedTempCategoryUUIDs.contains(entry.key) }
            .toMutableMap()

        val tempCounterParties = mainState.value.tempCounterParties
        val selectedTempCounterPartyUUIDs = mainState.value.selectedTempCounterParties
            .filter { entry -> entry.value }.keys
        val counterPartyMappings = mainState.value.counterPartyMappings
            .filter { entry -> selectedTempCounterPartyUUIDs.contains(entry.key) }
            .toMutableMap()

        val tempMethods = mainState.value.tempMethods
        val selectedTempMethodUUIDs = mainState.value.selectedTempMethods
            .filter { entry -> entry.value }.keys
        val methodMappings = mainState.value.methodMappings
            .filter { entry -> selectedTempMethodUUIDs.contains(entry.key) }
            .toMutableMap()

        val tempSources = mainState.value.tempSources
        val selectedTempSourceUUIDs = mainState.value.selectedTempSources
            .filter { entry -> entry.value }.keys
        val sourceMappings = mainState.value.sourceMappings
            .filter { entry -> selectedTempSourceUUIDs.contains(entry.key) }
            .toMutableMap()
        val restoreBalanceSources = mainState.value.restoreBalanceSources

        val enabledTempTransactionTemplateUUIDs = enabledState.value.enabledTempTransactionTemplates
        val selectedTempTransactionTemplateUUIDs = mainState.value.selectedTempTransactionTemplates
            .filter { uuid -> enabledTempTransactionTemplateUUIDs.contains(uuid) }
        val tempTransactionTemplates = mainState.value.tempTransactionTemplates
            .filter { tempTransactionTemplate ->
                selectedTempTransactionTemplateUUIDs.contains(
                    tempTransactionTemplate.uuid
                )
            }

        val enabledTempTransactionUUIDs = enabledState.value.enabledTempTransactions
        val selectedTempTransactionUUIDs = _selectedTransactionsState.value
            .filter { uuid -> enabledTempTransactionUUIDs.contains(uuid) }
        val tempTransactions = mainState.value.tempTransactions
            .filter { tempTransaction -> selectedTempTransactionUUIDs.contains(tempTransaction.uuid) }

        try {
            realm.write {
                categoryMappings
                    .entries
                    .filter { (_, category) -> category.uuid.isNotEmpty() }
                    .distinctBy { (_, category) -> category.uuid }
                    .forEach { (tempCategoryUUID, category) ->
                        findLatest(category)?.also {
                            it.frequency += tempTransactions.count { tempTransaction -> tempTransaction.category?.uuid == tempCategoryUUID }
                            it.lastUsed = maxOf(
                                it.lastUsed,
                                tempTransactions.maxOf { tempTransaction -> tempTransaction.time })
                        }
                    }

                categoryMappings
                    .filter { entry -> entry.value.uuid.isEmpty() }
                    .forEach { entry ->
                        val tempCategory =
                            tempCategories.find { tempCategory -> tempCategory.uuid == entry.key }
                        if (tempCategory != null) {
                            val category = copyToRealm(
                                Category().apply {
                                    uuid = UUID.randomUUID().toString()
                                    name = tempCategory.name
                                    icon = tempCategory.icon
                                    frequency =
                                        tempTransactions.count { tempTransaction -> tempTransaction.category?.uuid == tempCategory.uuid }
                                    lastUsed =
                                        tempTransactions.maxOf { tempTransaction -> tempTransaction.time }
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
                        findLatest(counterParty)?.also {
                            it.frequency += tempTransactions.count { tempTransaction -> tempTransaction.counterParty?.uuid == tempCounterPartyUUID }
                            it.lastUsed = maxOf(
                                it.lastUsed,
                                tempTransactions.maxOf { tempTransaction -> tempTransaction.time })
                        }
                    }

                counterPartyMappings
                    .filter { entry -> entry.value.uuid.isEmpty() }
                    .forEach { entry ->
                        val tempCounterParty =
                            tempCounterParties.find { tempCounterParty -> tempCounterParty.uuid == entry.key }
                        if (tempCounterParty != null) {
                            val counterParty = copyToRealm(
                                CounterParty().apply {
                                    uuid = UUID.randomUUID().toString()
                                    name = tempCounterParty.name
                                    icon = tempCounterParty.icon
                                    frequency =
                                        tempTransactions.count { tempTransaction -> tempTransaction.counterParty?.uuid == tempCounterParty.uuid }
                                    lastUsed =
                                        tempTransactions.maxOf { tempTransaction -> tempTransaction.time }
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
                        findLatest(method)?.also {
                            it.frequency += tempTransactions.count { tempTransaction -> tempTransaction.category?.uuid == tempMethodUUID }
                            it.lastUsed = maxOf(
                                it.lastUsed,
                                tempTransactions.maxOf { tempTransaction -> tempTransaction.time })
                        }
                    }

                methodMappings
                    .filter { entry -> entry.value.uuid.isEmpty() }
                    .forEach { entry ->
                        val tempMethod =
                            tempMethods.find { tempMethod -> tempMethod.uuid == entry.key }
                        if (tempMethod != null) {
                            val method = copyToRealm(
                                Method().apply {
                                    uuid = UUID.randomUUID().toString()
                                    name = tempMethod.name
                                    icon = tempMethod.icon
                                    frequency =
                                        tempTransactions.count { tempTransaction -> tempTransaction.method?.uuid == tempMethod.uuid }
                                    lastUsed =
                                        tempTransactions.maxOf { tempTransaction -> tempTransaction.time }
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
                        findLatest(source)?.also {
                            it.frequency += tempTransactions.count { tempTransaction -> tempTransaction.category?.uuid == tempSourceUUID }
                            it.lastUsed = maxOf(
                                it.lastUsed,
                                tempTransactions.maxOf { tempTransaction -> tempTransaction.time })
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
                            val source = copyToRealm(
                                Source().apply {
                                    uuid = UUID.randomUUID().toString()
                                    name = tempSource.name
                                    icon = tempSource.icon
                                    balance = tempSource.balance
                                    currency = tempSource.currency
                                    frequency =
                                        tempTransactions.count { tempTransaction -> tempTransaction.source?.uuid == tempSource.uuid }
                                    lastUsed =
                                        tempTransactions.maxOf { tempTransaction -> tempTransaction.time }
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

        val duration = System.currentTimeMillis() - startTime
        if (duration < Constants.WAIT_DIALOG_MINIMUM_SCREEN_TIME) {
            delay(Constants.WAIT_DIALOG_MINIMUM_SCREEN_TIME - duration)
        }
        _event.send(result)

        _mainState.update {
            it.copy(
                importOngoing = false
            )
        }
    }

    //region Transaction

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTempTransactionQuery(): Flow<ResultsChange<TempTransaction>> {
        return mainState.flatMapLatest {
            var queryString =
                "time >= $0 && time <= $1 && currency==$2 && amount >= $3 && amount <= $4 && typeId IN $5"
            if (it.searchText.isNotBlank()) {
                queryString += " && (title CONTAINS[c] '${it.searchText.trim()}' || description CONTAINS[c] '${it.searchText.trim()}')"
            }
            realm.query<TempTransaction>(
                queryString,
                it.startDate?.getStartOfDayInEpochMilli() ?: 0L,
                it.endDate?.getEndOfDayInEpochMilli() ?: Long.MAX_VALUE,
                it.selectedCurrencies.filter { entry -> entry.value }.keys,
                it.filterAmountMin,
                if (it.filterAmountMax <= it.filterAmountMin) Double.MAX_VALUE else it.filterAmountMax,
                it.selectedTransactionTypes
            ).sort("time", it.sortDirection).asFlow()
        }
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
                    icon = counterParty.icon,
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
                icon = method.icon,
                value = method.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = source.icon,
                value = source.name,
                resId = R.string.placeholder
            )
        )
        chips.add(
            ChipInfo(
                icon = TablerIcons.Alarm,
                value = tempTransaction.time.toZonedDateTime()
                    .format(DateTimeFormatter.ofPattern("hh:mm a")),
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
        selectedTempCategories: Map<String, Boolean>,
        selectedTempCounterParties: Map<String, Boolean>,
        selectedTempMethods: Map<String, Boolean>,
        selectedTempSources: Map<String, Boolean>,
        tempTransactions: List<TempTransaction>,
    ): Set<String> {
        return tempTransactions
            .filter { tempTransaction ->
                val categoryValid = selectedTempCategories[tempTransaction.category?.uuid] == true
                val counterPartyValid =
                    tempTransaction.counterParty == null || selectedTempCounterParties[tempTransaction.counterParty?.uuid] == true
                val methodValid = selectedTempMethods[tempTransaction.method?.uuid] == true
                val sourceValid = selectedTempSources[tempTransaction.source?.uuid] == true
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
        selectedTempCategories: Map<String, Boolean>,
        selectedTempCounterParties: Map<String, Boolean>,
        selectedTempMethods: Map<String, Boolean>,
        selectedTempSources: Map<String, Boolean>,
        tempTransactionTemplates: List<TempTransactionTemplate>,
    ): Set<String> {
        return tempTransactionTemplates
            .filter { tempTransactionTemplate ->
                val categoryValid =
                    tempTransactionTemplate.category == null || selectedTempCategories[tempTransactionTemplate.category?.uuid] == true
                val counterPartyValid =
                    tempTransactionTemplate.counterParty == null || selectedTempCounterParties[tempTransactionTemplate.counterParty?.uuid] == true
                val methodValid =
                    tempTransactionTemplate.method == null || selectedTempMethods[tempTransactionTemplate.method?.uuid] == true
                val sourceValid =
                    tempTransactionTemplate.source == null || selectedTempSources[tempTransactionTemplate.source?.uuid] == true
                categoryValid && counterPartyValid && methodValid && sourceValid
            }
            .map { tempTransactionTemplate -> tempTransactionTemplate.uuid }
            .toSet()
    }

    fun toggleTransactionTemplateSelected(tempTransactionTemplateUUID: String) {
        _mainState.update {
            val selectedTempTransactionTemplates =
                it.selectedTempTransactionTemplates.toMutableSet()

            if (selectedTempTransactionTemplates.contains(tempTransactionTemplateUUID)) {
                selectedTempTransactionTemplates.remove(tempTransactionTemplateUUID)
            } else {
                selectedTempTransactionTemplates.add(tempTransactionTemplateUUID)
            }

            it.copy(
                selectedTempTransactionTemplates = selectedTempTransactionTemplates
            )
        }
    }

    private fun getTemplateWithIcons(
        templates: List<TempTransactionTemplate>
    ): List<TransactionTemplateWithIcons> {
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
                        icon = counterParty.icon
                    )
                )
            }
            if (method != null) {
                chips.add(
                    ChipInfo(resId = R.string.placeholder, value = method.name, icon = method.icon)
                )
            }
            if (source != null) {
                chips.add(
                    ChipInfo(resId = R.string.placeholder, value = source.name, icon = source.icon)
                )
            }
            chips.add(
                ChipInfo(
                    resId = R.string.frequency_of_use_label,
                    value = numberFormatter.format(template.frequency).toString(),
                    icon = TablerIcons.ChartLine
                )
            )
            if (template.lastUsed > 0) {
                chips.add(
                    ChipInfo(
                        resId = R.string.last_used_datetime_label,
                        value = template.lastUsed.toZonedDateTime()
                            .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
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
        _mainState.update {
            val categoryMappings = it.categoryMappings.toMutableMap()
            categoryMappings[tempCategoryUUID] = dbCategory

            it.copy(
                categoryMappings = categoryMappings
            )
        }
    }

    fun toggleCategorySelected(uuid: String) {
        _mainState.update {
            val selectedTempCategories = it.selectedTempCategories.toMutableMap()
            selectedTempCategories[uuid] = !selectedTempCategories[uuid]!!

            it.copy(
                selectedTempCategories = selectedTempCategories
            )
        }
    }

    private fun getSelectedTempCategories(
        tempCategories: List<TempCategory>,
        selectedTempCategories: Map<String, Boolean>
    ): Map<String, Boolean> {
        return tempCategories.associateBy(
            {
                it.uuid
            },
            {
                selectedTempCategories.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getCategoryMappings(
        dbCategories: List<Category>,
        tempCategories: List<TempCategory>,
        categoryMappings: Map<String, Category>
    ): Map<String, Category> {
        return tempCategories.associateBy(
            { tempCategory ->
                tempCategory.uuid
            },
            { tempCategory ->
                val mappedCategory = categoryMappings[tempCategory.uuid]
                if (mappedCategory != null) {
                    mappedCategory
                } else {
                    val dbCategory =
                        dbCategories.find { category -> category.name == tempCategory.name }
                    dbCategory ?: Category()
                }
            }
        )
    }

    //endregion

    //region CounterParty

    fun setSelectedCounterParty(tempCounterPartyUUID: String, dbCounterParty: CounterParty) {
        _mainState.update {
            val counterPartyMappings = it.counterPartyMappings.toMutableMap()
            counterPartyMappings[tempCounterPartyUUID] = dbCounterParty

            it.copy(
                counterPartyMappings = counterPartyMappings
            )
        }
    }

    fun toggleCounterPartySelected(uuid: String) {
        _mainState.update {
            val selectedTempCounterParties = it.selectedTempCounterParties.toMutableMap()
            selectedTempCounterParties[uuid] = !selectedTempCounterParties[uuid]!!

            it.copy(
                selectedTempCounterParties = selectedTempCounterParties
            )
        }
    }

    private fun getSelectedTempCounterParties(
        tempCounterParties: List<TempCounterParty>,
        selectedTempCounterParties: Map<String, Boolean>
    ): Map<String, Boolean> {
        return tempCounterParties.associateBy(
            {
                it.uuid
            },
            {
                selectedTempCounterParties.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getCounterPartyMappings(
        dbCounterParties: List<CounterParty>,
        tempCounterParties: List<TempCounterParty>,
        counterPartyMappings: Map<String, CounterParty>
    ): Map<String, CounterParty> {
        return tempCounterParties.associateBy(
            { tempCounterParty ->
                tempCounterParty.uuid
            },
            { tempCounterParty ->
                val mappedCounterParty = counterPartyMappings[tempCounterParty.uuid]
                if (mappedCounterParty != null) {
                    mappedCounterParty
                } else {
                    val dbCounterParty =
                        dbCounterParties.find { counterParty -> counterParty.name == tempCounterParty.name }
                    dbCounterParty ?: CounterParty()
                }
            }
        )
    }

    //endregion

    //region Method

    fun setSelectedMethod(tempMethodUUID: String, dbMethod: Method) {
        _mainState.update {
            val methodMappings = it.methodMappings.toMutableMap()
            methodMappings[tempMethodUUID] = dbMethod

            it.copy(
                methodMappings = methodMappings
            )
        }
    }

    fun toggleMethodSelected(uuid: String) {
        _mainState.update {
            val selectedTempMethods = it.selectedTempMethods.toMutableMap()
            selectedTempMethods[uuid] = !selectedTempMethods[uuid]!!

            it.copy(
                selectedTempMethods = selectedTempMethods
            )
        }
    }

    private fun getSelectedTempMethods(
        tempMethods: List<TempMethod>,
        selectedTempMethods: Map<String, Boolean>
    ): Map<String, Boolean> {
        return tempMethods.associateBy(
            {
                it.uuid
            },
            {
                selectedTempMethods.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getMethodMappings(
        dbMethods: List<Method>,
        tempMethods: List<TempMethod>,
        methodMappings: Map<String, Method>
    ): Map<String, Method> {
        return tempMethods.associateBy(
            { tempMethod ->
                tempMethod.uuid
            },
            { tempMethod ->
                val mappedMethod = methodMappings[tempMethod.uuid]
                if (mappedMethod != null) {
                    mappedMethod
                } else {
                    val dbMethod = dbMethods.find { method -> method.name == tempMethod.name }
                    dbMethod ?: Method()
                }
            }
        )
    }

    //endregion

    //region Source

    fun setSelectedSource(tempSourceUUID: String, dbSource: Source) {
        _mainState.update {
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
        _mainState.update {
            val selectedTempSources = it.selectedTempSources.toMutableMap()
            selectedTempSources[uuid] = !selectedTempSources[uuid]!!

            it.copy(
                selectedTempSources = selectedTempSources
            )
        }
    }

    fun toggleRestoreBalance(tempSourceUUID: String) {
        _mainState.update {
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

    private fun getSelectedTempSources(
        tempSources: List<TempSource>,
        selectedTempSources: Map<String, Boolean>
    ): Map<String, Boolean> {
        return tempSources.associateBy(
            {
                it.uuid
            },
            {
                selectedTempSources.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getSourceMappings(
        dbSources: List<Source>,
        tempSources: List<TempSource>,
        sourceMappings: Map<String, Source>
    ): Map<String, Source> {
        return tempSources.associateBy(
            { tempSource ->
                tempSource.uuid
            },
            { tempSource ->
                val mappedSource = sourceMappings[tempSource.uuid]
                if (mappedSource != null) {
                    mappedSource
                } else {
                    val dbSource = dbSources.find { source -> source.name == tempSource.name }
                    dbSource ?: Source()
                }
            }
        )
    }

    //endregion

    fun checkPageValidity(currentPage: Int) = viewModelScope.launch {
        val state = _mainState.value
        var eventToSend: Event? = null
        if (currentPage == 0) {
            val atLeastOneRestoreCategorySelected =
                state.selectedTempCategories.any { entry -> entry.value }
            if (atLeastOneRestoreCategorySelected) {
                if (
                    state.tempCategories
                        .filter { tempCategory ->
                            state.selectedTempCategories[tempCategory.uuid] == true && state.conflictingTempCategories.contains(
                                tempCategory.uuid
                            )
                        }
                        .any { tempCategory ->
                            val mappedCategory = state.categoryMappings[tempCategory.uuid]
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
            state.tempCounterParties
                .filter { tempCounterParty ->
                    state.selectedTempCounterParties[tempCounterParty.uuid] == true && state.conflictingTempCounterParties.contains(
                        tempCounterParty.uuid
                    )
                }
                .any { tempCounterParty ->
                    val mappedCounterParty = state.counterPartyMappings[tempCounterParty.uuid]
                    mappedCounterParty != null && mappedCounterParty.uuid.isEmpty()
                }
        ) {
            eventToSend = Event.MappingInvalid
        }
        if (currentPage == 2) {
            val atLeastOneRestoreMethodSelected =
                state.selectedTempMethods.any { entry -> entry.value }
            if (atLeastOneRestoreMethodSelected) {
                if (
                    state.tempMethods
                        .filter { tempMethod ->
                            state.selectedTempMethods[tempMethod.uuid] == true && state.conflictingTempMethods.contains(
                                tempMethod.uuid
                            )
                        }
                        .any { tempMethod ->
                            val mappedMethod = state.methodMappings[tempMethod.uuid]
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
            val atLeastOneRestoreSourceSelected =
                state.selectedTempSources.any { entry -> entry.value }
            if (atLeastOneRestoreSourceSelected) {
                if (
                    state.tempSources
                        .filter { tempSource ->
                            state.selectedTempSources[tempSource.uuid] == true && state.conflictingTempSources.contains(
                                tempSource.uuid
                            )
                        }
                        .any { tempSource ->
                            val mappedSource = state.sourceMappings[tempSource.uuid]
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
        _selectedTransactionsState.update {
            val transactionUUIDs =
                transactions.map { transactionWithIcons -> transactionWithIcons.uuid }.toSet()
            val selectedTransactions = it.toMutableSet()
            if (localDateSelected) {
                selectedTransactions.removeAll(transactionUUIDs)
            } else {
                selectedTransactions.addAll(transactionUUIDs)
            }
            selectedTransactions
        }
    }

    fun toggleTransactionSelected(uuid: String) {
        _selectedTransactionsState.update {
            val selectedTransactions = it.toMutableSet()
            if (selectedTransactions.contains(uuid)) {
                selectedTransactions.remove(uuid)
            } else {
                selectedTransactions.add(uuid)
            }
            selectedTransactions
        }
    }

    fun toggleSelection(
        allSelected: Boolean,
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>
    ) {
        _selectedTransactionsState.update {
            val selectedTransactions = it.toMutableSet()
            if (allSelected) {
                selectedTransactions.clear()
            } else {
                val transactionUUIDs = filteredTransactions
                    .values
                    .flatten()
                    .map { transaction -> transaction.uuid }

                selectedTransactions.addAll(transactionUUIDs)
            }
            selectedTransactions
        }
    }

    fun setSelectedCurrencies(selectedCurrencies: Map<String, Boolean>) {
        _mainState.update {
            it.copy(
                loading = true,
                selectedCurrencies = selectedCurrencies
            )
        }
    }

    fun setStartDateAndEndDate(startDate: LocalDate?, endDate: LocalDate?) {
        _mainState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                startDateString = startDate?.getDateString() ?: "",
                endDateString = endDate?.getDateString() ?: "",
                dateRangeStringRes = if (startDate == null && endDate == null) {
                    R.string.all_time_date_range_label
                } else {
                    R.string.date_range_label
                },
                loading = true
            )
        }
    }

    fun setSelectedTransactionTypes(selectedTransactionTypes: List<Int>) {
        _mainState.update {
            it.copy(
                loading = true,
                selectedTransactionTypes = selectedTransactionTypes
            )
        }
    }

    fun setSortDirection(sortDirection: Sort) {
        _mainState.update {
            it.copy(
                sortDirection = sortDirection,
                loading = true
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
        _mainState.update {
            it.copy(
                loading = true,
                filterAmountMin = min,
                filterAmountMax = max,
                filterAmountRange = filterAmountRange
            )
        }
    }

    fun setSearchText(searchText: String) {
        _mainState.update {
            it.copy(
                loading = true,
                searchText = searchText,
            )
        }
    }

    fun setLocale(locale: Locale?) {
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        numberFormatter = getNumberFormatter(locale)
        _mainState.update {
            it.copy(
                locale = locale
            )
        }
    }
}