package jp.ikigai.cash.flow.data.dto

data class TransactionsWithTotalAmount(
    val transactions: List<TransactionWithChips>,
    val totalAmount: String,
)
