package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonSelectionSheet(
    index: Int,
    maxHeight: Double,
    sheetState: SheetState,
    dismiss: () -> Unit,
    items: LazyListScope.() -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.animateScrollToItem(index)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight.dp)
                .padding(start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = {
                    dismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
        }
    }
}