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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.dto.TemplateWithChips
import jp.ikigai.cash.flow.ui.components.cards.TransactionTemplateCard

@Composable
fun ImportScreenSelectTemplatesContent(
    tempTransactionTemplatesWithIcons: List<TemplateWithChips>,
    enabledTempTransactionTemplates: Set<Long>,
    selectedTempTransactionTemplates: Set<Long>,
    toggleTransactionTemplateSelected: (Long) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = tempTransactionTemplatesWithIcons.isEmpty(),
            modifier = Modifier.align(alignment = Alignment.Center)
        ) {
            Text(text = stringResource(id = R.string.no_templates_to_import_placeholder_label))
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = tempTransactionTemplatesWithIcons,
                key = { templateWithChips -> templateWithChips.id }
            ) { templateWithChips ->
                TransactionTemplateCard(
                    checked = enabledTempTransactionTemplates.contains(
                        templateWithChips.id
                    ) && selectedTempTransactionTemplates.contains(
                        templateWithChips.id
                    ),
                    enabled = enabledTempTransactionTemplates.contains(
                        templateWithChips.id
                    ),
                    modifier = Modifier.animateItem(),
                    templateWithChips = templateWithChips,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        toggleTransactionTemplateSelected(
                            templateWithChips.id
                        )
                    }
                )
            }
        }
    }
}