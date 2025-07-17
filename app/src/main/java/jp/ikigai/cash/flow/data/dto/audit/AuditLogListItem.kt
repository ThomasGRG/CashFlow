package jp.ikigai.cash.flow.data.dto.audit

import jp.ikigai.cash.flow.Account
import jp.ikigai.cash.flow.data.enums.EntityType
import jp.ikigai.cash.flow.data.enums.LogAction

sealed class AuditLogListItem {
    data class TransactionLog(
        val logId: Long,
        val entityId: Long,
        val entityType: EntityType,
        val logAction: LogAction,
        val dateTime: String,
        val time: String,
        val formattedBeforeAmount: String,
        val before: TransactionAuditDTO?,
        val formattedAfterAmount: String,
        val after: TransactionAuditDTO?
    ) : AuditLogListItem()

    data class AccountLog(
        val logId: Long,
        val entityId: Long,
        val entityType: EntityType,
        val logAction: LogAction,
        val dateTime: String,
        val time: String,
        val formattedBeforeBalance: String,
        val before: Account,
        val formattedAfterBalance: String,
        val after: Account
    ) : AuditLogListItem()
}
