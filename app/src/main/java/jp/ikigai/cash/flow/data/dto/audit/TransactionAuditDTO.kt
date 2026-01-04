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

import jp.ikigai.cash.flow.data.enums.TransactionType
import java.time.ZonedDateTime

data class TransactionAuditDTO(
    val transactionId: Long,
    val transactionTitle: String,
    val transactionDescription: String,
    val transactionAmount: Double,
    val transactionCurrency: String,
    val transactionType: TransactionType,
    val transactionDateTime: ZonedDateTime,
    val transactionAccountName: String,
    val transactionCategoryName: String,
    val transactionCounterPartyName: String,
    val transactionMethodName: String
)
