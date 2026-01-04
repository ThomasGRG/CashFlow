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

package jp.ikigai.cash.flow.data

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R

sealed class Event(@StringRes val message: Int) {
    data object InsufficientBalance : Event(R.string.not_enough_balance_error_label)
    data object InternalError : Event(R.string.internal_error_label)
    data object IOError : Event(R.string.io_error_label)
    data object ExportSuccess : Event(R.string.export_success_label)
    data object ImportSuccess : Event(R.string.import_success_label)
    data object MigrationSuccess : Event(R.string.migration_success_label)
    data object SaveSuccess : Event(R.string.save_success_label)
    data object DeleteSuccess : Event(R.string.delete_success_label)
    data object RestoreSortSuccess : Event(R.string.restore_success_label)
    data object CloneTransactionSuccess : Event(R.string.clone_transaction_success_label)
    data object CreateTemplateFromTransactionSuccess :
        Event(R.string.create_template_from_transaction_success_label)
    data object MinimumTwoFieldsRequired : Event(R.string.minimum_two_fields_required_error_label)
    data object AccountCategoryMethodRequired :
        Event(R.string.category_method_account_required_error_label)

    data object CategoryMethodRequired : Event(R.string.category_method_required_error_label)
    data object AccountCategoryRequired : Event(R.string.category_account_required_error_label)
    data object AccountMethodRequired : Event(R.string.method_account_required_error_label)
    data object CategoryRequired : Event(R.string.category_required_error_label)
    data object CategoryMapRequired : Event(R.string.category_map_required_error_label)
    data object MethodRequired : Event(R.string.method_required_error_label)
    data object MethodMapRequired : Event(R.string.method_map_required_error_label)
    data object AccountRequired : Event(R.string.account_required_error_label)
    data object AccountMapRequired : Event(R.string.account_map_required_error_label)
    data object MappingInvalid : Event(R.string.mapping_invalid_error_label)
}
