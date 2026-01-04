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

package jp.ikigai.cash.flow.data.enums

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R

enum class LogAction(
    @StringRes val label: Int
) {
    CREATE(R.string.create_label),
    UPDATE(R.string.update_label),
    DELETE(R.string.delete_label),
    BALANCE_UPDATE(R.string.balance_updated_label)
}