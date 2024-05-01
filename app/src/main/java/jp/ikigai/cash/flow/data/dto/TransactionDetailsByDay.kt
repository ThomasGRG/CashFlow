package jp.ikigai.cash.flow.data.dto

data class TransactionDetailsByDay(
    val transactions: List<TransactionWithIcons>,
    val totalAmount: Double,
)
