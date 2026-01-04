/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

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
