package jp.ikigai.cash.flow.ui.screenStates.listing

import android.icu.util.Currency
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.Filters
import jp.ikigai.cash.flow.data.dto.TransactionDetailsByDay
import java.time.LocalDate
import java.time.YearMonth

data class TransactionsScreenState(
    val transactions: Map<LocalDate, TransactionDetailsByDay> = emptyMap(),
    val income: Double = 0.0,
    val incomeTransactionsCount: Int = 0,
    val expense: Double = 0.0,
    val expenseTransactionsCount: Int = 0,
    val filters: Filters = Filters(),
    val currencies: List<Currency> = Constants.currencyList,
    val selectedCurrency: String = Currency.getInstance("INR").currencyCode,
    val startDate: LocalDate = YearMonth.now().atDay(1),
    val endDate: LocalDate = YearMonth.now().atEndOfMonth(),
    val startDateString: String = "",
    val endDateString: String = "",
    val balance: Double = 0.0,
    val loading: Boolean = true,
)
