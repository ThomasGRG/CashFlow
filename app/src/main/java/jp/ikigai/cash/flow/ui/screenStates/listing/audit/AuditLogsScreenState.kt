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

package jp.ikigai.cash.flow.ui.screenStates.listing.audit

import jp.ikigai.cash.flow.data.dto.audit.AuditLogListItem
import java.time.LocalDate
import java.util.Locale

data class AuditLogsScreenState(
    val auditLogs: Map<LocalDate, List<AuditLogListItem>> = emptyMap(),
    val loading: Boolean = true,
    val locale: Locale? = null
)
