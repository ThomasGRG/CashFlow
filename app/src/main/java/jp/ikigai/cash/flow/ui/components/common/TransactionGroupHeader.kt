package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun TransactionGroupHeader(
    date: LocalDate,
    amount: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(top = 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${date.dayOfMonth} ${date.month.name} ${date.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = date.dayOfWeek.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(0.8f)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = amount,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TransactionGroupHeader(
    amountRange: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(top = 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = amountRange,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TransactionGroupHeader(
    date: LocalDate,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val alpha by remember(key1 = enabled) {
        mutableFloatStateOf(if (enabled) 1f else 0.38f)
    }

    val subAlpha by remember(key1 = enabled) {
        mutableFloatStateOf(if (enabled) 0.8f else 0.38f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .background(
                color = MaterialTheme.colorScheme.background
            )
            .padding(top = 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Text(
                text = "${date.dayOfMonth} ${date.month.name} ${date.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(alpha)
            )
            Text(
                text = date.dayOfWeek.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.alpha(subAlpha)
            )
        }
        Checkbox(checked = selected, onCheckedChange = null, enabled = enabled)
    }
}

@Preview
@Composable
fun TransactionGroupHeaderPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TransactionGroupHeader(date = LocalDate.now(), amount = "239.01")
        TransactionGroupHeader(
            date = LocalDate.now(),
            selected = true,
            enabled = true,
            onClick = {})
        TransactionGroupHeader(
            date = LocalDate.now(),
            selected = true,
            enabled = false,
            onClick = {})
        TransactionGroupHeader(
            date = LocalDate.now(),
            selected = false,
            enabled = false,
            onClick = {})
        TransactionGroupHeader(
            date = LocalDate.now(),
            selected = false,
            enabled = true,
            onClick = {})
    }
}