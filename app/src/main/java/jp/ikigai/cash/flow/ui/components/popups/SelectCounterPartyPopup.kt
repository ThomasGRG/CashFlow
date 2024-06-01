package jp.ikigai.cash.flow.ui.components.popups

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
import compose.icons.TablerIcons
import compose.icons.tablericons.Users
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.ui.components.buttons.IconToggleRow

@Composable
fun SelectCounterPartyPopup(
    index: Int,
    selectedCounterPartyUUID: String,
    setSelectedCounterParty: (CounterParty) -> Unit,
    counterParties: List<CounterParty>,
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
                items = counterParties,
                key = { counterParty -> "counterParty-${counterParty.uuid}" }
            ) { counterParty ->
                IconToggleRow(
                    label = counterParty.name,
                    icon = counterParty.icon,
                    selected = counterParty.uuid == selectedCounterPartyUUID,
                    onClick = {
                        dismiss()
                        if (counterParty.uuid == selectedCounterPartyUUID) {
                            setSelectedCounterParty(CounterParty())
                        } else {
                            setSelectedCounterParty(counterParty)
                        }
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
fun SelectCounterPartyPopupPreview() {
    SelectCounterPartyPopup(
        index = 1,
        selectedCounterPartyUUID = "asd",
        setSelectedCounterParty = {},
        counterParties = listOf(
            CounterParty().apply {
                uuid = "asd"
                name = "Shopping"
                icon = TablerIcons.Users
            },
            CounterParty().apply {
                uuid = "asdfrg"
                name = "Transportation"
                icon = TablerIcons.Users
            },
            CounterParty().apply {
                uuid = "iurwuef"
                name = "Personal Care"
                icon = TablerIcons.Users
            },
            CounterParty().apply {
                uuid = "iurwueadfegf"
                name = "Food & Drinks"
                icon = TablerIcons.Users
            }
        ),
        dismiss = {}
    )
}