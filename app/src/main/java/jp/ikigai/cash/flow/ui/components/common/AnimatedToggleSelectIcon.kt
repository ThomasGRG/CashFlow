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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import jp.ikigai.cash.flow.R

@Composable
fun AnimatedToggleSelectIcon(
    deselectVisible: Boolean
) {
    Box(
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedVisibility(
            visible = deselectVisible,
            enter = expandHorizontally(expandFrom = Alignment.Start),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_deselect_24),
                contentDescription = "toggle_select_icon"
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.baseline_select_all_24),
            contentDescription = "toggle_select_icon"
        )
    }
}