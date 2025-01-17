package jp.ikigai.cash.flow.data.dto.export

data class TransactionExport(
    val uuid: String,
    val title: String,
    val description: String,
    val amount: Double,
    val typeId: Int,
    val currency: String,
    val time: Long,
    val categoryUUID: String,
    val counterPartyUUID: String?,
    val methodUUID: String,
    val sourceUUID: String,
)
