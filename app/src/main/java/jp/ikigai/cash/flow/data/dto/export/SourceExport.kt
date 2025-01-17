package jp.ikigai.cash.flow.data.dto.export

data class SourceExport(
    val uuid: String,
    val name: String,
    val iconName: String,
    val currency: String,
    val balance: Double,
    val frequency: Int,
    val lastUsed: Long,
)
