package jp.ikigai.cash.flow.ui.viewmodels.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.Sort
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.Event
import jp.ikigai.cash.flow.data.dto.ChipInfo
import jp.ikigai.cash.flow.data.dto.ExportTransactionsScreenFlows
import jp.ikigai.cash.flow.data.dto.TransactionWithIcons
import jp.ikigai.cash.flow.data.dto.export.CommonExport
import jp.ikigai.cash.flow.data.dto.export.ExportData
import jp.ikigai.cash.flow.data.dto.export.SourceExport
import jp.ikigai.cash.flow.data.dto.export.TemplateExport
import jp.ikigai.cash.flow.data.dto.export.TransactionExport
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.data.entity.Transaction
import jp.ikigai.cash.flow.data.entity.TransactionTemplate
import jp.ikigai.cash.flow.ui.screenStates.common.ExportTransactionsScreenState
import jp.ikigai.cash.flow.utils.combineFiveFlows
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.getEndOfDayInEpochMilli
import jp.ikigai.cash.flow.utils.getHighlightedString
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.buffer
import okio.sink
import java.io.IOException
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExportTransactionsScreenViewModel(
    private val realm: Realm = Realm.open(Database.config),
) : ViewModel() {

    private var numberFormatter = getNumberFormatter()
    private var currencyFormatterMap = getCurrencyFormatterMap()

    private var loadDataJob: Job? = null

    private val _state = MutableStateFlow(ExportTransactionsScreenState())
    val state: StateFlow<ExportTransactionsScreenState> = _state.asStateFlow()

    private val _event: Channel<Event> = Channel(Int.MAX_VALUE)
    val event: Flow<Event> = _event.receiveAsFlow()

    private val sourceQuery = realm.query<Source>().sort("frequency", Sort.DESCENDING)

    private val methodQuery = realm.query<Method>().sort("frequency", Sort.DESCENDING)

    private val counterPartyQuery = realm.query<CounterParty>().sort("frequency", Sort.DESCENDING)

    private val categoryQuery = realm.query<Category>().sort("frequency", Sort.DESCENDING)

    init {
        loadDataJob = loadData()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
        _event.close()
    }

    private fun loadData() = viewModelScope.launch {
        combineFiveFlows(
            categoryQuery.asFlow(),
            counterPartyQuery.asFlow(),
            methodQuery.asFlow(),
            sourceQuery.asFlow(),
            getTransactionQuery()
        ) { categoryChanges, counterPartyChanges, methodChanges, sourceChanges, transactionChanges ->
            ExportTransactionsScreenFlows(
                categories = categoryChanges.list,
                counterParties = counterPartyChanges.list,
                methods = methodChanges.list,
                sources = sourceChanges.list,
                transactions = transactionChanges.list
            )
        }.collectLatest { exportTransactionsScreenFlows ->
            _state.update {
                val sources = exportTransactionsScreenFlows.sources.toMutableList()

                sources.forEach { source ->
                    val formatter = currencyFormatterMap.getValue(source.currency)
                    source.displayBalance = formatter.format(source.balance).toString()
                }

                val selectedCategories = getSelectedCategories(
                    exportTransactionsScreenFlows.categories,
                    it.selectedCategories
                )
                val selectedCounterParties = getSelectedCounterParties(
                    exportTransactionsScreenFlows.counterParties,
                    it.selectedCounterParties
                )
                val selectedMethods = getSelectedMethods(
                    exportTransactionsScreenFlows.methods,
                    it.selectedMethods
                )
                val selectedSources = getSelectedSources(
                    exportTransactionsScreenFlows.sources,
                    it.selectedSources
                )

                val selectedCategoryCount = selectedCategories.filter { entry -> entry.value }.size
                val selectedCounterPartyCount =
                    selectedCounterParties.filter { entry -> entry.value }.size
                val selectedMethodCount = selectedMethods.filter { entry -> entry.value }.size
                val selectedSourceCount = selectedSources.filter { entry -> entry.value }.size

                val filteredTransactions = getTransactionsMap(
                    exportTransactionsScreenFlows.transactions,
                    it.searchText
                )

                it.copy(
                    transactionsHashCode = exportTransactionsScreenFlows.transactions.hashCode(),
                    filteredTransactions = filteredTransactions,
                    selectedLocalDates = getSelectedLocalDates(
                        filteredTransactions,
                        it.selectedTransactions
                    ),
                    allSelected = getAllSelected(
                        filteredTransactions,
                        it.selectedTransactions
                    ),
                    loading = false,
                    enabled = true,
                    categories = exportTransactionsScreenFlows.categories,
                    selectedCategories = selectedCategories,
                    selectedCategoryCount = numberFormatter.format(selectedCategoryCount)
                        .toString(),
                    counterParties = exportTransactionsScreenFlows.counterParties,
                    selectedCounterParties = selectedCounterParties,
                    selectedCounterPartyCount = numberFormatter.format(selectedCounterPartyCount)
                        .toString(),
                    methods = exportTransactionsScreenFlows.methods,
                    selectedMethods = selectedMethods,
                    selectedMethodCount = numberFormatter.format(selectedMethodCount).toString(),
                    sources = sources,
                    selectedSources = selectedSources,
                    selectedSourceCount = numberFormatter.format(selectedSourceCount).toString(),
                )
            }
        }
    }

    private fun getSelectedLocalDates(
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>,
        selectedTransactions: Set<String>
    ): Set<LocalDate> {
        return filteredTransactions
            .filter { entry ->
                val transactionUUIDs =
                    entry.value.map { transactionWithIcons -> transactionWithIcons.uuid }
                transactionUUIDs.all { uuid -> selectedTransactions.contains(uuid) }
            }
            .keys
    }

    private fun getAllSelected(
        filteredTransactions: Map<LocalDate, List<TransactionWithIcons>>,
        selectedTransactions: Set<String>
    ): Boolean {
        return filteredTransactions
            .values
            .flatten()
            .map { transaction -> transaction.uuid }
            .all { uuid -> selectedTransactions.contains(uuid) }
    }

    private fun getSelectedCategories(
        categories: List<Category>,
        selectedCategories: Map<String, Boolean>
    ): Map<String, Boolean> {
        return categories.associateBy(
            {
                it.uuid
            },
            {
                selectedCategories.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getSelectedCounterParties(
        counterParties: List<CounterParty>,
        selectedCounterParties: Map<String, Boolean>
    ): Map<String, Boolean> {
        return counterParties.associateBy(
            {
                it.uuid
            },
            {
                selectedCounterParties.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getSelectedMethods(
        methods: List<Method>,
        selectedMethods: Map<String, Boolean>
    ): Map<String, Boolean> {
        return methods.associateBy(
            {
                it.uuid
            },
            {
                selectedMethods.getOrDefault(it.uuid, true)
            }
        )
    }

    private fun getSelectedSources(
        sources: List<Source>,
        selectedSources: Map<String, Boolean>
    ): Map<String, Boolean> {
        return sources.associateBy(
            {
                it.uuid
            },
            {
                selectedSources.getOrDefault(it.uuid, true)
            }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getTransactionQuery(): Flow<ResultsChange<Transaction>> {
        return state.flatMapLatest {
            var queryString =
                "time >= $0 && time <= $1 && currency IN $2 && amount >= $3 && amount <= $4 && typeId IN $5 && category.uuid IN $6 && method.uuid IN $7 && source.uuid IN $8"
            if (it.searchText.isNotBlank()) {
                queryString += " && (title CONTAINS[c] '${it.searchText.trim()}' || description CONTAINS[c] '${it.searchText.trim()}')"
            }
            queryString += if (it.includeNoCounterPartyTransactions) {
                " && (counterParty == nil || counterParty.uuid IN $9)"
            } else {
                " && counterParty.uuid IN $9"
            }
            realm.query<Transaction>(
                queryString,
                it.startDate?.getStartOfDayInEpochMilli() ?: 0L,
                it.endDate?.getEndOfDayInEpochMilli() ?: Long.MAX_VALUE,
                it.selectedCurrencies.filter { selectedCurrency -> selectedCurrency.value }.keys,
                it.filterAmountMin,
                if (it.filterAmountMax <= it.filterAmountMin) Double.MAX_VALUE else it.filterAmountMax,
                it.selectedTransactionTypes,
                it.selectedCategories.filter { selectedCategory -> selectedCategory.value }.keys,
                it.selectedMethods.filter { selectedMethods -> selectedMethods.value }.keys,
                it.selectedSources.filter { selectedSources -> selectedSources.value }.keys,
                it.selectedCounterParties.filter { selectedCounterParties -> selectedCounterParties.value }.keys
            ).sort("time", it.sortDirection).asFlow()
        }
    }

    private fun getTransactionsMap(
        transactions: List<Transaction>,
        searchText: String
    ): Map<LocalDate, List<TransactionWithIcons>> {
        val transactionsMap = transactions.groupBy { it.time.toLocalDate() }
        val transactionDetailsMap = mutableMapOf<LocalDate, List<TransactionWithIcons>>()
        transactionsMap.forEach { (localDate, transactionsList) ->
            transactionDetailsMap[localDate] =
                transactionsList.map { getTransactionWithIcons(it, searchText) }
        }
        return transactionDetailsMap
    }

    private fun getTransactionWithIcons(
        transaction: Transaction,
        searchText: String
    ): TransactionWithIcons {
        val category = transaction.category!!
        val counterParty = transaction.counterParty
        val method = transaction.method!!
        val source = transaction.source!!
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
                value = transaction.time.toZonedDateTime()
                    .format(DateTimeFormatter.ofPattern("hh:mm a")),
                resId = R.string.placeholder
            )
        )
        return TransactionWithIcons(
            uuid = transaction.uuid,
            annotatedTitle = getHighlightedString(transaction.title, searchText),
            annotatedDescription = getHighlightedString(transaction.description, searchText),
            amount = currencyFormatter.format(transaction.amount).toString(),
            typeIcon = transaction.type.icon,
            typeIconColor = transaction.type.color,
            currency = transaction.currency,
            chips = chips
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun export(includeTemplates: Boolean, outputStream: OutputStream?) = viewModelScope.launch {
        loadDataJob?.cancelAndJoin()
        _state.update {
            it.copy(
                exportOngoing = true,
                enabled = false
            )
        }
        val startTime = System.currentTimeMillis()
        var result: Event

        val exportTemplates = if (includeTemplates) getExportTemplates() else emptyList()
        val exportTransactions = getExportTransactions(state.value.selectedTransactions)
        val exportCategories = getExportCategories(
            exportTransactions.map { it.categoryUUID }.toSet()
        )
        val exportCounterParties = getExportCounterParties(
            exportTransactions.mapNotNull { it.counterPartyUUID }.toSet()
        )
        val exportMethods = getExportMethods(
            exportTransactions.map { it.methodUUID }.toSet()
        )
        val exportSources = getExportSources(
            exportTransactions.map { it.sourceUUID }.toSet()
        )

        val exportData = ExportData(
            transactions = exportTransactions,
            categories = exportCategories,
            counterParties = exportCounterParties,
            methods = exportMethods,
            sources = exportSources,
            templates = exportTemplates
        )

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter<ExportData>()

        outputStream?.sink()?.buffer().use { bufferedSink ->
            try {
                bufferedSink?.writeUtf8(adapter.indent("    ").serializeNulls().toJson(exportData))
                result = Event.ExportSuccess
            } catch (e: IOException) {
                result = Event.IOError
            }
        }

        val duration = System.currentTimeMillis() - startTime
        if (duration < Constants.WAIT_DIALOG_MINIMUM_SCREEN_TIME) {
            delay(Constants.WAIT_DIALOG_MINIMUM_SCREEN_TIME - duration)
        }
        _event.send(result)

        _state.update {
            it.copy(
                exportOngoing = false
            )
        }
    }

    private fun getExportTransactions(
        selectedTransactions: Set<String>
    ): List<TransactionExport> {
        return realm.query<Transaction>("uuid IN $0", selectedTransactions).find().map {
            TransactionExport(
                uuid = it.uuid,
                title = it.title,
                description = it.description,
                amount = it.amount,
                typeId = it.type.id,
                currency = it.currency,
                time = it.time,
                categoryUUID = it.category!!.uuid,
                counterPartyUUID = it.counterParty?.uuid,
                methodUUID = it.method!!.uuid,
                sourceUUID = it.source!!.uuid,
            )
        }
    }

    private fun getExportTemplates(): List<TemplateExport> {
        return realm.query<TransactionTemplate>().find().map {
            TemplateExport(
                uuid = it.uuid,
                name = it.name,
                title = it.title,
                description = it.description,
                amount = it.amount,
                typeId = it.type.id,
                categoryUUID = it.category?.uuid,
                counterPartyUUID = it.counterParty?.uuid,
                methodUUID = it.method?.uuid,
                sourceUUID = it.source?.uuid,
                frequency = it.frequency,
                lastUsed = it.lastUsed,
            )
        }
    }

    private fun getExportCategories(
        categoryUUIDs: Set<String>
    ): List<CommonExport> {
        return realm.query<Category>("uuid IN $0", categoryUUIDs).find().map {
            CommonExport(
                uuid = it.uuid,
                name = it.name,
                frequency = it.frequency,
                iconName = it.icon.name,
                lastUsed = it.lastUsed
            )
        }
    }

    private fun getExportCounterParties(
        counterPartyUUIDs: Set<String>
    ): List<CommonExport> {
        return realm.query<CounterParty>("uuid IN $0", counterPartyUUIDs).find().map {
            CommonExport(
                uuid = it.uuid,
                name = it.name,
                frequency = it.frequency,
                iconName = it.icon.name,
                lastUsed = it.lastUsed
            )
        }
    }

    private fun getExportMethods(
        methodUUIDs: Set<String>
    ): List<CommonExport> {
        return realm.query<Method>("uuid IN $0", methodUUIDs).find().map {
            CommonExport(
                uuid = it.uuid,
                name = it.name,
                frequency = it.frequency,
                iconName = it.icon.name,
                lastUsed = it.lastUsed
            )
        }
    }

    private fun getExportSources(
        sourceUUIDs: Set<String>
    ): List<SourceExport> {
        return realm.query<Source>("uuid IN $0", sourceUUIDs).find().map {
            SourceExport(
                uuid = it.uuid,
                name = it.name,
                frequency = it.frequency,
                iconName = it.icon.name,
                lastUsed = it.lastUsed,
                balance = it.balance,
                currency = it.currency
            )
        }
    }

    fun toggleLocalDateSelected(localDate: LocalDate) {
        _state.update {
            val transactionUUIDs = it.filteredTransactions
                .getOrDefault(localDate, emptyList())
                .map { transaction -> transaction.uuid }
                .toSet()
            val selectedTransactions = it.selectedTransactions.toMutableSet()

            if (it.selectedLocalDates.contains(localDate)) {
                selectedTransactions.removeAll(transactionUUIDs)
            } else {
                selectedTransactions.addAll(transactionUUIDs)
            }

            it.copy(
                selectedTransactions = selectedTransactions
            )
        }
    }

    fun toggleTransactionSelected(uuid: String) {
        _state.update {
            val selectedTransactions = it.selectedTransactions.toMutableSet()
            if (selectedTransactions.contains(uuid)) {
                selectedTransactions.remove(uuid)
            } else {
                selectedTransactions.add(uuid)
            }
            it.copy(
                selectedTransactions = selectedTransactions
            )
        }
    }

    fun toggleSelection() {
        _state.update {
            val transactionUUIDs = it.filteredTransactions
                .values
                .flatten()
                .map { transaction -> transaction.uuid }
                .toSet()
            val selectedTransactions = it.selectedTransactions.toMutableSet()
            if (it.allSelected) {
                selectedTransactions.removeAll(transactionUUIDs)
            } else {
                selectedTransactions.addAll(transactionUUIDs)
            }
            it.copy(
                selectedTransactions = selectedTransactions
            )
        }
    }

    fun setSelectedCurrencies(selectedCurrencies: Map<String, Boolean>) {
        _state.update {
            it.copy(
                loading = true,
                selectedCurrencies = selectedCurrencies
            )
        }
    }

    fun setStartDateAndEndDate(startDate: LocalDate?, endDate: LocalDate?) {
        _state.update {
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

    fun setSelectedCategories(
        selectedCategories: Map<String, Boolean>
    ) {
        _state.update {
            it.copy(
                loading = true,
                selectedCategories = selectedCategories
            )
        }
    }

    fun setSelectedCounterParties(
        includeTransactionsWithNoCounterParty: Boolean,
        selectedCounterParties: Map<String, Boolean>
    ) {
        _state.update {
            it.copy(
                loading = true,
                selectedCounterParties = selectedCounterParties,
                includeNoCounterPartyTransactions = includeTransactionsWithNoCounterParty
            )
        }
    }

    fun setSelectedMethods(selectedMethods: Map<String, Boolean>) {
        _state.update {
            it.copy(
                loading = true,
                selectedMethods = selectedMethods
            )
        }
    }

    fun setSelectedSources(selectedSources: Map<String, Boolean>) {
        _state.update {
            it.copy(
                loading = true,
                selectedSources = selectedSources
            )
        }
    }

    fun setSelectedTransactionTypes(selectedTransactionTypes: List<Int>) {
        _state.update {
            it.copy(
                loading = true,
                selectedTransactionTypes = selectedTransactionTypes
            )
        }
    }

    fun setSortDirection(sortDirection: Sort) {
        _state.update {
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
        _state.update {
            it.copy(
                loading = true,
                filterAmountMin = min,
                filterAmountMax = max,
                filterAmountRange = filterAmountRange
            )
        }
    }

    fun setSearchText(searchText: String) {
        _state.update {
            it.copy(
                loading = true,
                searchText = searchText,
            )
        }
    }

    fun setLocale(locale: Locale?) {
        numberFormatter = getNumberFormatter(locale)
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}