package jp.ikigai.cash.flow.data.dto.export

data class CommonExport(
    val uuid: String,
    val name: String,
    val frequency: Int,
    val lastUsed: Long,
)
