package jp.ikigai.cash.flow.ui.screenStates.listing.audit

import jp.ikigai.cash.flow.data.dto.audit.AuditLogListItem
import java.time.LocalDate
import java.util.Locale

data class AuditLogsScreenState(
    val auditLogs: Map<LocalDate, List<AuditLogListItem>> = emptyMap(),
    val loading: Boolean = true,
    val locale: Locale? = null
)
