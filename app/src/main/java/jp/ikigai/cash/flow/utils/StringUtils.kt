package jp.ikigai.cash.flow.utils

import android.icu.text.SimpleDateFormat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import compose.icons.AllIcons
import compose.icons.TablerIcons
import jp.ikigai.cash.flow.data.Constants
import java.util.Date
import java.util.Locale

fun String.getIconForCategory(): ImageVector {
    return TablerIcons.AllIcons.find { it.name == this } ?: Constants.DEFAULT_CATEGORY_ICON
}

fun getHighlightedString(searchString: String, highlightText: String): AnnotatedString {
    return buildAnnotatedString {
        val startIndex = searchString.indexOf(
            highlightText,
            startIndex = 0,
            ignoreCase = true
        )
        val endIndex = startIndex + highlightText.length
        append(searchString)
        addStyle(
            style = SpanStyle(background = Color.Yellow.copy(alpha = 0.7f), color = Color.Black),
            start = startIndex,
            end = endIndex
        )
    }
}

fun getExportFileName(locale: Locale?): String {
    val date = SimpleDateFormat("yyyy-MM-dd_HH-mm", locale ?: Locale.getDefault()).format(Date())
    return "CashFlow_$date.json"
}