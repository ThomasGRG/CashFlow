package jp.ikigai.cash.flow.ui.screenStates.common.restore

import java.time.LocalDate

data class ImportBackupScreenEnabledState(
    val enabledLocalDates: Set<LocalDate> = emptySet(),
    val enabledTempTransactions: Set<String> = emptySet(),
    val enabledTempTransactionTemplates: Set<String> = emptySet(),
)
