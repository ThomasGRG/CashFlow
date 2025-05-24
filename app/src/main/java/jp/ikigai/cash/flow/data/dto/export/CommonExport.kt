package jp.ikigai.cash.flow.data.dto.export

data class CommonExport(
    val id: Long,
    val name: String,
    val frequency: Int,
    val lastUsed: Long,
)
