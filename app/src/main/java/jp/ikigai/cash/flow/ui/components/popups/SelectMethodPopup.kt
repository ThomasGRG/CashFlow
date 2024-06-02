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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CreditCard
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Method
import jp.ikigai.cash.flow.ui.components.buttons.IconToggleRow

@Composable
fun SelectMethodPopup(
    index: Int,
    selectedMethodUUID: String,
    setSelectedMethod: (Method) -> Unit,
    methods: List<Method>,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

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
                items = methods,
                key = { method -> "method-${method.uuid}" }
            ) { method ->
                IconToggleRow(
                    label = method.name,
                    icon = method.icon,
                    selected = method.uuid == selectedMethodUUID,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedMethod(method)
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
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
fun SelectMethodPopupPreview() {
    SelectMethodPopup(
        index = 1,
        selectedMethodUUID = "asd",
        setSelectedMethod = {},
        methods = listOf(
            Method().apply {
                uuid = "asd"
                name = "Shopping"
                icon = TablerIcons.CreditCard
            },
            Method().apply {
                uuid = "asdfrg"
                name = "Transportation"
                icon = TablerIcons.CreditCard
            },
            Method().apply {
                uuid = "iurwuef"
                name = "Personal Care"
                icon = TablerIcons.CreditCard
            },
            Method().apply {
                uuid = "iurwueadfegf"
                name = "Food & Drinks"
                icon = TablerIcons.CreditCard
            }
        ),
        dismiss = {}
    )
}