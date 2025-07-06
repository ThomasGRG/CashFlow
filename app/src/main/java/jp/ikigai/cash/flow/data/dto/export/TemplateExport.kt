package jp.ikigai.cash.flow.data.dto.export

data class TemplateExport(
    val id: Long,
    val name: String,
    val title: String,
    val description: String,
    val amount: Double,
    val typeId: Int,
    val accountId: Long,
    val categoryId: Long,
    val counterPartyId: Long,
    val methodId: Long
)
