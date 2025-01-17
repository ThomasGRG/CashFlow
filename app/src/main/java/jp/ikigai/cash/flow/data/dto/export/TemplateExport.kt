package jp.ikigai.cash.flow.data.dto.export

data class TemplateExport(
    val uuid: String,
    val name: String,
    val title: String,
    val description: String,
    val amount: Double,
    val typeId: Int,
    val categoryUUID: String?,
    val counterPartyUUID: String?,
    val methodUUID: String?,
    val sourceUUID: String?,
    val frequency: Int,
    val lastUsed: Long,
)
