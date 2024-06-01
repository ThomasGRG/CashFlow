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
import compose.icons.tablericons.Archive
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.ui.components.buttons.IconToggleRow

@Composable
fun SelectCategoryPopup(
    index: Int,
    selectedCategoryUUID: String,
    setSelectedCategory: (Category) -> Unit,
    categories: List<Category>,
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
                items = categories,
                key = { category -> "category-${category.uuid}" }
            ) { category ->
                IconToggleRow(
                    label = category.name,
                    icon = category.icon,
                    selected = category.uuid == selectedCategoryUUID,
                    onClick = {
                        dismiss()
                        setSelectedCategory(category)
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
fun SelectCategoryPopupPreview() {
    SelectCategoryPopup(
        index = 1,
        selectedCategoryUUID = "asd",
        setSelectedCategory = {},
        categories = listOf(
            Category().apply {
                uuid = "asd"
                name = "Shopping"
                icon = TablerIcons.Archive
            },
            Category().apply {
                uuid = "asdfrg"
                name = "Transportation"
                icon = TablerIcons.Archive
            },
            Category().apply {
                uuid = "iurwuef"
                name = "Personal Care"
                icon = TablerIcons.Archive
            },
            Category().apply {
                uuid = "iurwueadfegf"
                name = "Food & Drinks"
                icon = TablerIcons.Archive
            }
        ),
        dismiss = {}
    )
}