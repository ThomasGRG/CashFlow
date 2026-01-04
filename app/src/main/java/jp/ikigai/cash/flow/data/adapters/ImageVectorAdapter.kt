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

package jp.ikigai.cash.flow.data.adapters

import androidx.compose.ui.graphics.vector.ImageVector
import app.cash.sqldelight.ColumnAdapter
import compose.icons.AllIcons
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive

val ImageVectorAdapter = object : ColumnAdapter<ImageVector, String> {
    override fun decode(databaseValue: String): ImageVector {
        return TablerIcons.AllIcons.find { it.name == databaseValue } ?: TablerIcons.Archive
    }

    override fun encode(value: ImageVector): String {
        return value.name
    }
}