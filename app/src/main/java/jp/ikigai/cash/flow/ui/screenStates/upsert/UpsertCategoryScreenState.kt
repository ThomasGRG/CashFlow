package jp.ikigai.cash.flow.ui.screenStates.upsert

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import jp.ikigai.cash.flow.Category
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import java.util.Locale

data class UpsertCategoryScreenState(
    val category: Category = Category(
        0L,
        "",
        TablerIcons.Archive
    ),
    val name: String = "",
    val nameValid: Boolean = true,
    @StringRes val nameErrorStringRes: Int = R.string.name_empty_error_label,
    val selectedIcon: ImageVector = Constants.DEFAULT_CATEGORY_ICON,
    val hasUnsavedChanges: Boolean = false,
    val transactionCount: Long = 0,
    val formattedTransactionCount: String = "",
    val loading: Boolean = true,
    val enabled: Boolean = false,
    val locale: Locale? = null
)
