package jp.ikigai.cash.flow.data

import java.time.LocalDate

sealed class TransactionHeader {
    data class DateHeader(val date: LocalDate, val formattedAmount: String) : TransactionHeader()
    data class AmountHeader(val formattedAmountRange: String) : TransactionHeader()
}