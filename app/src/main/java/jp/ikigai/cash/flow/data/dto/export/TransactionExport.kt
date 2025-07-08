package jp.ikigai.cash.flow.data.dto.export

data class TransactionExport(
    val id: Long,
    val title: String,
    val description: String,
    val amount: Double,
    val type: String,
    val currency: String,
    val time: Long,
    val accountId: Long,
    val categoryId: Long,
    val counterPartyId: Long,
    val methodId: Long,
    val templateId: Long,
)
