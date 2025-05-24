package jp.ikigai.cash.flow.ui.screenStates.common.restore

import java.time.LocalDate

data class ImportBackupScreenSecondaryState(
    val enabledLocalDates: Set<LocalDate> = emptySet(),
    val enabledTempTransactions: Set<Long> = emptySet(),
    val selectedTransactionsCount: String = "",
    val enabledTempTransactionTemplates: Set<Long> = emptySet(),
    val selectedTempTransactionTemplateCount: String = "",
    val selectedLocalDates: Set<LocalDate> = emptySet(),
    val allSelected: Boolean = false,
)
