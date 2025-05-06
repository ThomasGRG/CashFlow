package jp.ikigai.cash.flow.data

import androidx.annotation.StringRes
import jp.ikigai.cash.flow.R

sealed class Event(@StringRes val message: Int) {
    object InternalError: Event(R.string.internal_error_label)
    object IOError : Event(R.string.io_error_label)
    object MetadataFixSuccess : Event(R.string.metadata_fix_success_label)
    object ExportSuccess : Event(R.string.export_success_label)
    object ImportSuccess : Event(R.string.import_success_label)
    object MigrationSuccess : Event(R.string.migration_success_label)
    object SaveSuccess : Event(R.string.save_success_label)
    object DeleteSuccess : Event(R.string.delete_success_label)
    object MinimumTwoFieldsRequired : Event(R.string.minimum_two_fields_required_error_label)
    object CategoryMethodSourceRequired : Event(R.string.category_method_source_required_error_label)
    object CategoryMethodRequired : Event(R.string.category_method_required_error_label)
    object CategorySourceRequired : Event(R.string.category_source_required_error_label)
    object MethodSourceRequired : Event(R.string.method_source_required_error_label)
    object CategoryRequired : Event(R.string.category_required_error_label)
    object CategoryMapRequired : Event(R.string.category_map_required_error_label)
    object MethodRequired : Event(R.string.method_required_error_label)
    object MethodMapRequired : Event(R.string.method_map_required_error_label)
    object SourceRequired : Event(R.string.source_required_error_label)
    object SourceMapRequired : Event(R.string.source_map_required_error_label)
    object MappingInvalid : Event(R.string.mapping_invalid_error_label)
}
