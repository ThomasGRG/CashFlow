package jp.ikigai.cash.flow.data.dto.export

data class AccountExport(
    val id: Long,
    val name: String,
    val currency: String,
    val balance: Double
)
