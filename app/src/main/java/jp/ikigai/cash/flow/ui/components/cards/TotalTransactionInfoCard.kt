package jp.ikigai.cash.flow.ui.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowUpCircle

@Composable
fun TotalTransactionInfoCard(
    modifier: Modifier,
    currency: String,
    total: String,
    count: String,
    color: Color,
    icon: ImageVector
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = icon.name,
                tint = color,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$total $currency",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$count transactions"
//                text = pluralStringResource(
//                    id = R.plurals.transaction_count_label,
//                    count,
//                    count,
//                ),
            )
        }
    }
}

@Preview
@Composable
fun TotalTransactionInfoCardPreview() {
    TotalTransactionInfoCard(
        modifier = Modifier.fillMaxWidth(),
        currency = "INR",
        total = "233.09",
        count = "2",
        color = Color(0xFFF44336),
        icon = TablerIcons.ArrowUpCircle
    )
}