package jp.ikigai.cash.flow.data.dto.export

data class CategoryExport(
    val uuid: String,
    val name: String,
    val iconName: String,
    val frequency: Int,
    val lastUsed: Long,
)
