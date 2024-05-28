package jp.ikigai.cash.flow.ui.components.popups

import android.icu.util.Currency
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.ui.components.buttons.ToggleRow

@Composable
fun CurrencyPopup(
    index: Int,
    selectedCurrency: String,
    setSelectedCurrency: (String) -> Unit,
    currencies: List<Currency>,
    dismiss: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .heightIn(max = 200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = currencies,
                key = { currency -> "currency-${currency.currencyCode}" }
            ) { currency ->
                ToggleRow(
                    identifier = currency.currencyCode,
                    label = "${currency.displayName} (${currency.currencyCode})",
                    selected = currency.currencyCode == selectedCurrency,
                    onClick = {
                        setSelectedCurrency(it)
                        dismiss()
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = {
                    dismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
        }
    }
}

@Preview
@Composable
fun CurrencyPopupPreview() {
    CurrencyPopup(
        index = 1,
        selectedCurrency = "INR",
        setSelectedCurrency = {},
        currencies = Constants.currencyList,
        dismiss = {}
    )
}