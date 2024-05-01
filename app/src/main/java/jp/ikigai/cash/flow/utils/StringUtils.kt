package jp.ikigai.cash.flow.utils

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.AllIcons
import compose.icons.TablerIcons
import jp.ikigai.cash.flow.data.Constants

fun String.getIconForCategory(): ImageVector {
    return TablerIcons.AllIcons.find { it.name == this } ?: Constants.DEFAULT_CATEGORY_ICON
}

fun String.getIconForCounterParty(): ImageVector {
    return TablerIcons.AllIcons.find { it.name == this } ?:Constants.DEFAULT_COUNTERPARTY_ICON
}

fun String.getIconForMethod(): ImageVector {
    return TablerIcons.AllIcons.find { it.name == this } ?:Constants.DEFAULT_METHOD_ICON
}

fun String.getIconForSource(): ImageVector {
    return TablerIcons.AllIcons.find { it.name == this } ?:Constants.DEFAULT_SOURCE_ICON
}