package jp.ikigai.cash.flow.data.dto.audit

import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.ZonedDateTime

data class TransactionAuditDTO(
    val transactionId: Long,
    val transactionTitle: String,
    val transactionDescription: String,
    val transactionAmount: Double,
    val transactionCurrency: String,
    val transactionType: TransactionType,
    val transactionDateTime: ZonedDateTime,
    val transactionAccountName: String,
    val transactionCategoryName: String,
    val transactionCounterPartyName: String,
    val transactionMethodName: String
)
