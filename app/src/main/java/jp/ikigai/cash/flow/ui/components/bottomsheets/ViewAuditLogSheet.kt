package jp.ikigai.cash.flow.ui.components.bottomsheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Alarm
import compose.icons.tablericons.ArrowDownCircle
import compose.icons.tablericons.Calendar
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.dto.audit.AuditLogListItem
import jp.ikigai.cash.flow.data.dto.audit.TransactionAuditDTO
import jp.ikigai.cash.flow.ui.components.common.CustomChip
import java.time.format.DateTimeFormatter

@Composable
fun ViewAuditLogSheet(
    auditLogDetails: AuditLogListItem.TransactionLog
) {
    val entityTypeLabel = stringResource(id = auditLogDetails.entityType.label)
    val logActionLabel = stringResource(id = auditLogDetails.logAction.label)

    Column(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$entityTypeLabel $logActionLabel",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = auditLogDetails.dateTime,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.7f)
                .padding(bottom = 10.dp)
        )
        if (auditLogDetails.before != null) {
            AuditLogTransactionCard(
                data = auditLogDetails.before,
                compareData = auditLogDetails.after,
                formattedAmount = auditLogDetails.formattedBeforeAmount
            )
        }
        if (auditLogDetails.before != null && auditLogDetails.after != null) {
            Icon(
                imageVector = TablerIcons.ArrowDownCircle,
                contentDescription = "before to after arrow icon",
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(50.dp)
            )
        }
        if (auditLogDetails.after != null) {
            AuditLogTransactionCard(
                data = auditLogDetails.after,
                compareData = null,
                formattedAmount = auditLogDetails.formattedAfterAmount
            )
        }
    }
}

@Composable
fun AuditLogTransactionCard(
    data: TransactionAuditDTO,
    compareData: TransactionAuditDTO?,
    formattedAmount: String
) {
    val showComparisons by remember(key1 = compareData) {
        mutableStateOf(compareData != null)
    }

    val date by remember(key1 = data) {
        mutableStateOf(
            data.transactionDateTime.format(
                DateTimeFormatter.ofPattern("dd LLL yyyy")
            )
        )
    }

    val dateChanged by remember(key1 = data, key2 = compareData) {
        mutableStateOf(
            compareData != null && (data.transactionDateTime.year != compareData.transactionDateTime.year || data.transactionDateTime.monthValue != compareData.transactionDateTime.monthValue || data.transactionDateTime.dayOfMonth != compareData.transactionDateTime.dayOfMonth)
        )
    }

    val time by remember(key1 = data) {
        mutableStateOf(
            data.transactionDateTime.format(
                DateTimeFormatter.ofPattern("hh:mm a")
            )
        )
    }

    val timeChanged by remember(key1 = data, key2 = compareData) {
        mutableStateOf(
            compareData != null && (data.transactionDateTime.hour != compareData.transactionDateTime.hour || data.transactionDateTime.minute != compareData.transactionDateTime.minute)
        )
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = data.transactionTitle,
                style = MaterialTheme.typography.headlineSmall,
                textDecoration = if (showComparisons && data.transactionTitle != compareData?.transactionTitle) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(
                        if (showComparisons && data.transactionTitle != compareData?.transactionTitle) {
                            0.7f
                        } else {
                            1f
                        }
                    )
            )
            if (data.transactionDescription.isNotBlank()) {
                Text(
                    text = data.transactionDescription,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (showComparisons && data.transactionDescription != compareData?.transactionDescription) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.6f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = data.transactionType.icon,
                    contentDescription = "type icon",
                    tint = data.transactionType.color,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (showComparisons && data.transactionAmount != compareData?.transactionAmount) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    modifier = Modifier.alpha(
                        if (showComparisons && data.transactionAmount != compareData?.transactionAmount) {
                            0.7f
                        } else {
                            1f
                        }
                    )
                )
            }
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)
            ) {
                if (data.transactionCounterPartyName.isNotEmpty()) {
                    CustomChip(
                        icon = Constants.DEFAULT_COUNTERPARTY_ICON,
                        label = data.transactionCounterPartyName,
                        enabled = !(showComparisons && data.transactionCounterPartyName != compareData?.transactionCounterPartyName),
                        textDecoration = if (showComparisons && data.transactionCounterPartyName != compareData?.transactionCounterPartyName) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                    )
                }
                CustomChip(
                    icon = Constants.DEFAULT_CATEGORY_ICON,
                    label = data.transactionCategoryName,
                    enabled = !(showComparisons && data.transactionCategoryName != compareData?.transactionCategoryName),
                    textDecoration = if (showComparisons && data.transactionCategoryName != compareData?.transactionCategoryName) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
                CustomChip(
                    icon = Constants.DEFAULT_METHOD_ICON,
                    label = data.transactionMethodName,
                    enabled = !(showComparisons && data.transactionMethodName != compareData?.transactionMethodName),
                    textDecoration = if (showComparisons && data.transactionMethodName != compareData?.transactionMethodName) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
                CustomChip(
                    icon = Constants.DEFAULT_ACCOUNT_ICON,
                    label = data.transactionAccountName,
                    enabled = !(showComparisons && data.transactionAccountName != compareData?.transactionAccountName),
                    textDecoration = if (showComparisons && data.transactionAccountName != compareData?.transactionAccountName) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
                CustomChip(
                    icon = TablerIcons.Calendar,
                    label = date,
                    enabled = !(showComparisons && dateChanged),
                    textDecoration = if (showComparisons && dateChanged) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
                CustomChip(
                    icon = TablerIcons.Alarm,
                    label = time,
                    enabled = !(showComparisons && timeChanged),
                    textDecoration = if (showComparisons && timeChanged) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    }
                )
            }
        }
    }
}

@Composable
fun ViewAuditLogSheet(
    auditLogDetails: AuditLogListItem.AccountLog
) {
    val entityTypeLabel = stringResource(id = auditLogDetails.entityType.label)
    val logActionLabel = stringResource(id = auditLogDetails.logAction.label)

    Column(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$entityTypeLabel $logActionLabel",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = auditLogDetails.dateTime,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.7f)
                .padding(bottom = 10.dp)
        )
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = auditLogDetails.after.accountName,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 10.dp)
                    )
                    Icon(
                        imageVector = Constants.DEFAULT_ACCOUNT_ICON,
                        contentDescription = Constants.DEFAULT_ACCOUNT_ICON.name,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.98f)
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = auditLogDetails.formattedBeforeBalance,
                        style = MaterialTheme.typography.displaySmall,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.alpha(0.6f)
                    )
                    Text(
                        text = auditLogDetails.formattedAfterBalance,
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(end = 15.dp)
                    )
                }
            }
        }
    }
}