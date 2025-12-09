package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R

@Composable
fun BalanceCard(
    modifier: Modifier = Modifier,
    balance: String,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        Text(
            text = stringResource(id = R.string.total_balance_label),
            modifier = Modifier.padding(top = 10.dp, start = 10.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, bottom = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = balance,
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}