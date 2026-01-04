/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui.viewmodels.charts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import jp.ikigai.cash.flow.AccountWithTransactionMetadata
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.enums.ChartType
import jp.ikigai.cash.flow.data.enums.TransactionType
import jp.ikigai.cash.flow.ui.screenStates.charts.ChartsScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.toEndOfMonth
import jp.ikigai.cash.flow.utils.toStartOfMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

class ChartsScreenViewModel(
    savedStateHandle: SavedStateHandle,
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private val initialChartType: String = checkNotNull(savedStateHandle["type"])

    private var currencyFormatterMap = getCurrencyFormatterMap()

    val modelProducer: CartesianChartModelProducer = CartesianChartModelProducer()
    val labelKey: ExtraStore.Key<List<String>> = ExtraStore.Key()

    private val _state = MutableStateFlow(
        ChartsScreenState(
            selectedChartType = ChartType.valueOf(initialChartType)
        )
    )
    val state: StateFlow<ChartsScreenState> = _state.asStateFlow()

    private val _loadingState = MutableStateFlow(true)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    private val _hasDataState = MutableStateFlow(false)
    val hasDataState: StateFlow<Boolean> = _hasDataState.asStateFlow()

    init {
        loadData()
        loadChartData()
    }

    private fun loadData() = viewModelScope.launch {
        val accounts = database
            .accountWithTransactionMetadataQueries
            .getAllAccountsSortedByTransactionCountDesc()
            .executeAsList()
            .map { account ->
                val formatter = currencyFormatterMap.getValue(account.currency)
                account.copy(
                    formattedBalance = formatter.format(account.balance).toString()
                )
            }

        val categories = database
            .categoryWithTransactionMetadataQueries
            .getAllCategoriesSortedByTransactionCountDesc()
            .executeAsList()

        val counterParties = database
            .counterPartyWithTransactionMetadataQueries
            .getAllCounterPartiesSortedByTransactionCountDesc()
            .executeAsList()

        val methods = database
            .methodWithTransactionMetadataQueries
            .getAllMethodsSortedByTransactionCountDesc()
            .executeAsList()

        _state.update {
            it.copy(
                accounts = accounts,
                selectedAccount = accounts.getOrNull(0) ?: it.selectedAccount,
                categories = categories,
                selectedCategory = categories.getOrNull(0) ?: it.selectedCategory,
                counterParties = counterParties,
                selectedCounterParty = counterParties.getOrNull(0) ?: it.selectedCounterParty,
                methods = methods,
                selectedMethod = methods.getOrNull(0) ?: it.selectedMethod,
            )
        }
    }

    private fun loadChartData() = viewModelScope.launch {
        state
            .onEach {
                _loadingState.update { true }
            }
            .collectLatest { state ->
                val startDate = state.yearMonthSelectionStart?.toStartOfMonth()
                    ?: ZonedDateTime.ofInstant(
                        Instant.EPOCH,
                        ZoneId.systemDefault()
                    )
                val endDate = state.yearMonthSelectionEnd?.toEndOfMonth()
                    ?: ZonedDateTime.now(
                        ZoneId.systemDefault()
                    )

                var hasData = false

                val yAxisData: MutableList<List<Number>> = mutableListOf()
                val xAxisData: MutableList<String> = mutableListOf()

                when (state.selectedChartType) {
                    ChartType.CATEGORY_TRANSACTION_COUNT_BAR_CHART -> {
                        val categoriesWithTransactionCount = database
                            .chartsQueries
                            .CategoryWithTransactionCount(
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                        hasData = categoriesWithTransactionCount.isNotEmpty()
                        yAxisData.add(
                            categoriesWithTransactionCount.map { it.transactionCount }
                        )
                        xAxisData.addAll(
                            categoriesWithTransactionCount.map { it.categoryName }
                        )
                    }

                    ChartType.CATEGORY_DEBIT_CREDIT_BAR_CHART -> {
                        val categoriesWithTotalAmount = database
                            .chartsQueries
                            .CategoryWithTotalAmount(
                                currency = state.selectedCurrency,
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                            .sortedByDescending {
                                (it.totalCreditAmount ?: 0.0) + (it.totalDebitAmount ?: 0.0)
                            }
                        hasData = categoriesWithTotalAmount.isNotEmpty()
                        xAxisData.addAll(
                            categoriesWithTotalAmount.map { it.categoryName }
                        )
                        yAxisData.add(
                            categoriesWithTotalAmount.map { it.totalDebitAmount ?: 0.0 }
                        )
                        yAxisData.add(
                            categoriesWithTotalAmount.map { it.totalCreditAmount ?: 0.0 }
                        )
                    }

                    ChartType.COUNTERPARTY_TRANSACTION_COUNT_BAR_CHART -> {
                        val counterPartiesWithTransactionCount = database
                            .chartsQueries
                            .CounterPartyWithTransactionCount(
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                        hasData = counterPartiesWithTransactionCount.isNotEmpty()
                        yAxisData.add(
                            counterPartiesWithTransactionCount.map { it.transactionCount }
                        )
                        xAxisData.addAll(
                            counterPartiesWithTransactionCount.map { it.counterPartyName }
                        )
                    }

                    ChartType.COUNTERPARTY_DEBIT_CREDIT_BAR_CHART -> {
                        val counterPartiesWithTotalAmount = database
                            .chartsQueries
                            .CounterPartyWithTotalAmount(
                                currency = state.selectedCurrency,
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                            .sortedByDescending {
                                (it.totalCreditAmount ?: 0.0) + (it.totalDebitAmount ?: 0.0)
                            }
                        hasData = counterPartiesWithTotalAmount.isNotEmpty()
                        yAxisData.add(
                            counterPartiesWithTotalAmount.map { it.totalDebitAmount ?: 0.0 }
                        )
                        yAxisData.add(
                            counterPartiesWithTotalAmount.map { it.totalCreditAmount ?: 0.0 }
                        )
                        xAxisData.addAll(
                            counterPartiesWithTotalAmount.map { it.counterPartyName }
                        )
                    }

                    ChartType.METHOD_TRANSACTION_COUNT_BAR_CHART -> {
                        val methodsWithTransactionCount = database
                            .chartsQueries
                            .MethodWithTransactionCount(
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                        hasData = methodsWithTransactionCount.isNotEmpty()
                        yAxisData.add(
                            methodsWithTransactionCount.map { it.transactionCount }
                        )
                        xAxisData.addAll(
                            methodsWithTransactionCount.map { it.methodName }
                        )
                    }

                    ChartType.METHOD_DEBIT_CREDIT_BAR_CHART -> {
                        val methodsWithTotalAmount = database
                            .chartsQueries
                            .MethodWithTotalAmount(
                                currency = state.selectedCurrency,
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                            .sortedByDescending {
                                (it.totalCreditAmount ?: 0.0) + (it.totalDebitAmount ?: 0.0)
                            }
                        hasData = methodsWithTotalAmount.isNotEmpty()
                        yAxisData.add(
                            methodsWithTotalAmount.map { it.totalDebitAmount ?: 0.0 }
                        )
                        yAxisData.add(
                            methodsWithTotalAmount.map { it.totalCreditAmount ?: 0.0 }
                        )
                        xAxisData.addAll(
                            methodsWithTotalAmount.map { it.methodName }
                        )
                    }

                    ChartType.ACCOUNT_TRANSACTION_COUNT_BAR_CHART -> {
                        val accountsWithTransactionCount = database
                            .chartsQueries
                            .AccountWithTransactionCount(
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                        hasData = accountsWithTransactionCount.isNotEmpty()
                        yAxisData.add(
                            accountsWithTransactionCount.map { it.transactionCount }
                        )
                        xAxisData.addAll(
                            accountsWithTransactionCount.map { it.accountName }
                        )
                    }

                    ChartType.ACCOUNT_DEBIT_CREDIT_BAR_CHART -> {
                        val accountsWithTotalAmount = database
                            .chartsQueries
                            .AccountWithTotalAmount(
                                currency = state.selectedCurrency,
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                            .sortedByDescending {
                                (it.totalCreditAmount ?: 0.0) + (it.totalDebitAmount ?: 0.0)
                            }
                        hasData = accountsWithTotalAmount.isNotEmpty()
                        yAxisData.add(
                            accountsWithTotalAmount.map { it.totalDebitAmount ?: 0.0 }
                        )
                        yAxisData.add(
                            accountsWithTotalAmount.map { it.totalCreditAmount ?: 0.0 }
                        )
                        xAxisData.addAll(
                            accountsWithTotalAmount.map { it.accountName }
                        )
                    }

                    ChartType.TRANSACTION_TYPE_AMOUNT_BAR_CHART -> {
                        val totals = database
                            .chartsQueries
                            .TotalDebitCreditInfo(
                                currency = state.selectedCurrency,
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                        hasData = totals.any { it.totalAmount != null && it.totalAmount > 0 }
                        yAxisData.add(
                            totals.map { it.totalAmount ?: 0.0 }
                        )
                        xAxisData.addAll(
                            totals.map { it.transactionType.name }
                        )
                    }

                    ChartType.TEMPLATE_TRANSACTION_COUNT_BAR_CHART -> {
                        val templatesWithTransactionCount = database
                            .chartsQueries
                            .TemplateWithTransactionCount(
                                startDate = startDate,
                                endDate = endDate
                            )
                            .executeAsList()
                        hasData = templatesWithTransactionCount.isNotEmpty()
                        yAxisData.add(
                            templatesWithTransactionCount.map { it.transactionCount }
                        )
                        xAxisData.addAll(
                            templatesWithTransactionCount.map { it.templateName }
                        )
                    }

                    ChartType.CATEGORY_TRENDS_LINE_CHART -> {
                        val trendsData = database
                            .chartsQueries
                            .CategoryTrend(
                                currency = state.selectedCurrency,
                                startDate = startDate,
                                endDate = endDate,
                                categoryId = state.selectedCategory.categoryId,
                            )
                            .executeAsList()
                            .groupBy { "${it.transactionDateTime.year}-${it.transactionDateTime.monthValue}" }
                        hasData = trendsData.isNotEmpty()
                        xAxisData.addAll(
                            trendsData.keys
                        )
                        yAxisData.add(
                            trendsData
                                .values
                                .map {
                                    it.sumOf { trendData ->
                                        if (trendData.transactionType == TransactionType.DEBIT) {
                                            trendData.transactionAmount
                                        } else {
                                            0.0
                                        }
                                    }
                                }
                        )
                        yAxisData.add(
                            trendsData
                                .values
                                .map {
                                    it.sumOf { trendData ->
                                        if (trendData.transactionType == TransactionType.CREDIT) {
                                            trendData.transactionAmount
                                        } else {
                                            0.0
                                        }
                                    }
                                }
                        )
                    }

                    ChartType.COUNTERPARTY_TRENDS_LINE_CHART -> {
                        val trendsData = database
                            .chartsQueries
                            .CounterPartyTrend(
                                currency = state.selectedCurrency,
                                startDate = startDate,
                                endDate = endDate,
                                counterPartyId = state.selectedCounterParty.counterPartyId,
                            )
                            .executeAsList()
                            .groupBy { "${it.transactionDateTime.year}-${it.transactionDateTime.monthValue}" }
                        hasData = trendsData.isNotEmpty()
                        xAxisData.addAll(
                            trendsData.keys
                        )
                        yAxisData.add(
                            trendsData
                                .values
                                .map {
                                    it.sumOf { trendData ->
                                        if (trendData.transactionType == TransactionType.DEBIT) {
                                            trendData.transactionAmount
                                        } else {
                                            0.0
                                        }
                                    }
                                }
                        )
                        yAxisData.add(
                            trendsData
                                .values
                                .map {
                                    it.sumOf { trendData ->
                                        if (trendData.transactionType == TransactionType.CREDIT) {
                                            trendData.transactionAmount
                                        } else {
                                            0.0
                                        }
                                    }
                                }
                        )
                    }

                    ChartType.METHOD_TRENDS_LINE_CHART -> {
                        val trendsData = database
                            .chartsQueries
                            .MethodTrend(
                                currency = state.selectedCurrency,
                                startDate = startDate,
                                endDate = endDate,
                                methodId = state.selectedMethod.methodId,
                            )
                            .executeAsList()
                            .groupBy { "${it.transactionDateTime.year}-${it.transactionDateTime.monthValue}" }
                        hasData = trendsData.isNotEmpty()
                        xAxisData.addAll(
                            trendsData.keys
                        )
                        yAxisData.add(
                            trendsData
                                .values
                                .map {
                                    it.sumOf { trendData ->
                                        if (trendData.transactionType == TransactionType.DEBIT) {
                                            trendData.transactionAmount
                                        } else {
                                            0.0
                                        }
                                    }
                                }
                        )
                        yAxisData.add(
                            trendsData
                                .values
                                .map {
                                    it.sumOf { trendData ->
                                        if (trendData.transactionType == TransactionType.CREDIT) {
                                            trendData.transactionAmount
                                        } else {
                                            0.0
                                        }
                                    }
                                }
                        )
                    }

                    ChartType.ACCOUNT_TRENDS_LINE_CHART -> {
                        val trendsData = database
                            .chartsQueries
                            .AccountTrend(
                                currency = state.selectedCurrency,
                                startDate = startDate,
                                endDate = endDate,
                                accountId = state.selectedAccount.accountId,
                            )
                            .executeAsList()
                            .groupBy { "${it.transactionDateTime.year}-${it.transactionDateTime.monthValue}" }
                        hasData = trendsData.isNotEmpty()
                        xAxisData.addAll(
                            trendsData.keys
                        )
                        yAxisData.add(
                            trendsData
                                .values
                                .map {
                                    it.sumOf { trendData ->
                                        if (trendData.transactionType == TransactionType.DEBIT) {
                                            trendData.transactionAmount
                                        } else {
                                            0.0
                                        }
                                    }
                                }
                        )
                        yAxisData.add(
                            trendsData
                                .values
                                .map {
                                    it.sumOf { trendData ->
                                        if (trendData.transactionType == TransactionType.CREDIT) {
                                            trendData.transactionAmount
                                        } else {
                                            0.0
                                        }
                                    }
                                }
                        )
                    }
                }

                if (hasData) {
                    modelProducer.runTransaction {
                        if (state.selectedChartType.isLineChart()) {
                            lineSeries {
                                yAxisData.forEach { y ->
                                    series(
                                        List(y.size) { index -> index },
                                        y
                                    )
                                }
                            }
                        } else {
                            columnSeries {
                                yAxisData.forEach { y ->
                                    series(
                                        List(y.size) { index -> index },
                                        y
                                    )
                                }
                            }
                        }
                        extras { mutableExtraStore ->
                            mutableExtraStore[labelKey] = xAxisData
                        }
                    }
                }
                _hasDataState.update { hasData }
                _loadingState.update { false }
            }
    }

    fun setChartType(chartType: ChartType) {
        _state.update {
            it.copy(
                selectedChartType = chartType
            )
        }
    }

    fun setYearMonthRange(startYearMonth: YearMonth?, endYearMonth: YearMonth?) {
        _state.update {
            it.copy(
                yearMonthSelectionStart = startYearMonth,
                startYearMonthString = if (startYearMonth != null) {
                    "${
                        startYearMonth.month.getDisplayName(
                            TextStyle.SHORT,
                            it.locale
                        )
                    }, ${startYearMonth.year}"
                } else "",
                yearMonthSelectionEnd = endYearMonth,
                endYearMonthString = if (endYearMonth != null) {
                    "${
                        endYearMonth.month.getDisplayName(
                            TextStyle.SHORT,
                            it.locale
                        )
                    }, ${endYearMonth.year}"
                } else "",
                yearMonthRangeStringRes = if (startYearMonth == null && endYearMonth == null) {
                    R.string.all_time_date_range_label
                } else {
                    R.string.date_range_label
                }
            )
        }
    }

    fun setSelectedCurrency(currency: String) {
        _state.update {
            it.copy(
                selectedCurrency = currency
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

    fun setLocale(locale: Locale?) {
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}