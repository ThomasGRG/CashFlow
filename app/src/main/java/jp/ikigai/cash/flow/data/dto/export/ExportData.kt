package jp.ikigai.cash.flow.data.dto.export

data class ExportData(
    val transactions: List<TransactionExport>,
    val accounts: List<AccountExport>,
    val categories: List<CategoryExport>,
    val counterParties: List<CommonExport>,
    val methods: List<CommonExport>,
    val templates: List<TemplateExport> = emptyList(),
)
