/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui.screens.common.restore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.InfoCircle
import jp.ikigai.cash.flow.CategoryWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TempCategory
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.ui.components.cards.MapCategoryCard

@Composable
fun ImportScreenMapCategoriesContent(
    dbCategories: List<CategoryWithTransactionMetadata>,
    tempCategories: List<TempCategory>,
    categoryMappings: Map<Long, CategoryWithTransactionMetadata>,
    selectedTempCategories: Set<Long>,
    conflictingTempCategories: Set<Long>,
    selectCategory: (Long) -> Unit,
    toggleSelected: (Long) -> Unit,
    setCategoryMapping: (Long, CategoryWithTransactionMetadata) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        stickyHeader(
            key = "info",
            contentType = "info_header",
        ) {
            if (dbCategories.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 5.dp, bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.InfoCircle,
                        contentDescription = "info icon",
                    )
                    Text(
                        text = stringResource(id = R.string.no_categories_available_for_mapping_label)
                    )
                }
            }
        }
        items(
            items = tempCategories,
            key = { tempCategory -> tempCategory.tempCategoryId }
        ) { tempCategory ->
            MapCategoryCard(
                tempCategory = tempCategory,
                mappedCategory = categoryMappings[
                    tempCategory.tempCategoryId
                ] ?: CategoryWithTransactionMetadata(
                    categoryId = 0L,
                    categoryName = "",
                    icon = Constants.DEFAULT_CATEGORY_ICON,
                    transactionCount = 0L,
                    lastUsed = null
                ),
                modifier = Modifier.animateItem(),
                canSelect = dbCategories.isNotEmpty(),
                selected = selectedTempCategories.contains(tempCategory.tempCategoryId),
                conflicting = conflictingTempCategories.contains(tempCategory.tempCategoryId),
                selectCategory = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectCategory(tempCategory.tempCategoryId)
                },
                toggleSelected = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    toggleSelected(tempCategory.tempCategoryId)
                },
                clearSelectedCategory = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setCategoryMapping(
                        tempCategory.tempCategoryId,
                        CategoryWithTransactionMetadata(
                            categoryId = 0L,
                            categoryName = "",
                            icon = Constants.DEFAULT_CATEGORY_ICON,
                            transactionCount = 0L,
                            lastUsed = null
                        )
                    )
                }
            )
        }
    }
}
