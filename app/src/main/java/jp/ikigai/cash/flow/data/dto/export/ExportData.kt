package jp.ikigai.cash.flow.data.dto.export

data class ExportData(
    val transactions: List<TransactionExport>,
    val categories: List<CategoryExport>,
    val counterParties: List<CommonExport>,
    val methods: List<CommonExport>,
    val sources: List<SourceExport>,
    val templates: List<TemplateExport> = emptyList(),
)
