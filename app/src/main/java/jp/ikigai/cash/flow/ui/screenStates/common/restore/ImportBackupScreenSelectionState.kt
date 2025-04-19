package jp.ikigai.cash.flow.ui.screenStates.common.restore

import java.time.LocalDate

data class ImportBackupScreenSelectionState(
    val selectedTransactionsCount: String = "",
    val selectedTempTransactionTemplateCount: String = "",
    val selectedLocalDates: Set<LocalDate> = emptySet(),
    val allSelected: Boolean = false,
)
