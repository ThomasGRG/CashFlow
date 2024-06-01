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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.BuildingBank
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.ui.components.buttons.IconToggleRow
import jp.ikigai.cash.flow.utils.getNumberFormatter

@Composable
fun SelectSourcePopup(
    index: Int,
    selectedSourceUUID: String,
    setSelectedSource: (Source) -> Unit,
    sources: List<Source>,
    dismiss: () -> Unit,
) {
    val numberFormatter = remember {
        getNumberFormatter()
    }

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
                items = sources,
                key = { source -> "source-${source.uuid}" }
            ) { source ->
                IconToggleRow(
                    label = "${source.name} (${numberFormatter.format(source.balance)} ${source.currency})",
                    icon = source.icon,
                    selected = source.uuid == selectedSourceUUID,
                    onClick = {
                        dismiss()
                        setSelectedSource(source)
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
fun SelectSourcePopupPreview() {
    SelectSourcePopup(
        index = 1,
        selectedSourceUUID = "asd",
        setSelectedSource = {},
        sources = listOf(
            Source().apply {
                uuid = "asd"
                name = "Shopping"
                icon = TablerIcons.BuildingBank
                balance = 937.00
                currency = "INR"
            },
            Source().apply {
                uuid = "asdfrg"
                name = "Transportation"
                icon = TablerIcons.BuildingBank
                balance = 937.65
                currency = "INR"
            },
            Source().apply {
                uuid = "iurwuef"
                name = "Personal Care"
                icon = TablerIcons.BuildingBank
                balance = 937.65
                currency = "INR"
            },
            Source().apply {
                uuid = "iurwueadfegf"
                name = "Food & Drinks"
                icon = TablerIcons.BuildingBank
                balance = 937.65
                currency = "INR"
            }
        ),
        dismiss = {}
    )
}