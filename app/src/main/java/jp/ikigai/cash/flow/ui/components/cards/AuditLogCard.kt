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

package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.data.dto.audit.AuditLogListItem

@Composable
fun AuditLogCard(
    modifier: Modifier,
    auditLogDetails: AuditLogListItem.TransactionLog,
    onClick: (AuditLogListItem.TransactionLog) -> Unit
) {
    val haptics = LocalHapticFeedback.current

    val entityTypeLabel = stringResource(id = auditLogDetails.entityType.label)
    val logActionLabel = stringResource(id = auditLogDetails.logAction.label)

    ElevatedCard(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick(auditLogDetails)
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "$entityTypeLabel $logActionLabel",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
            Text(
                text = auditLogDetails.time,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(0.7f)
            )
        }
    }
}

@Composable
fun AuditLogCard(
    modifier: Modifier,
    auditLogDetails: AuditLogListItem.AccountLog,
    onClick: (AuditLogListItem.AccountLog) -> Unit
) {
    val haptics = LocalHapticFeedback.current

    val entityTypeLabel = stringResource(id = auditLogDetails.entityType.label)
    val logActionLabel = stringResource(id = auditLogDetails.logAction.label)

    ElevatedCard(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick(auditLogDetails)
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "$entityTypeLabel $logActionLabel",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
            Text(
                text = auditLogDetails.time,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(0.7f)
            )
        }
    }
}