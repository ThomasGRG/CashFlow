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

package jp.ikigai.cash.flow.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Search

@Composable
fun CustomFloatingActionButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = 56.dp, minWidth = 56.dp),
        shape = FloatingActionButtonDefaults.shape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = FloatingActionButtonDefaults.containerColor,
            contentColor = contentColorFor(backgroundColor = FloatingActionButtonDefaults.containerColor)
        ),
        elevation = ButtonDefaults.filledTonalButtonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 6.dp,
            focusedElevation = 6.dp,
            hoveredElevation = 8.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        content()
    }
}

@Preview
@Composable
fun CustomFloatingActionButtonPreview() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        CustomFloatingActionButton(enabled = true, onClick = {}) {
            Icon(imageVector = TablerIcons.Search, contentDescription = "")
        }
        CustomFloatingActionButton(enabled = false, onClick = {}) {
            Icon(imageVector = TablerIcons.Search, contentDescription = "")
        }
        FloatingActionButton(onClick = {}) {
            Icon(imageVector = TablerIcons.Search, contentDescription = "")
        }
    }
}