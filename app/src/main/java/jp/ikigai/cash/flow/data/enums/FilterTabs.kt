package jp.ikigai.cash.flow.data.enums

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.ArrowsSort
import compose.icons.tablericons.BuildingBank
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.CreditCard
import compose.icons.tablericons.Stack
import compose.icons.tablericons.Users
import jp.ikigai.cash.flow.R

enum class FilterTabs(val index: Int, val icon: ImageVector, @StringRes val text: Int) {
    AmountTab(0, TablerIcons.CashBanknote, R.string.amount_label),
    TransactionTypeTab(1, TablerIcons.ArrowsSort, R.string.types_label),
    CategoryTab(2, TablerIcons.Archive, R.string.categories_label),
    CounterPartyTab(3, TablerIcons.Users, R.string.counter_parties_label),
    MethodTab(4, TablerIcons.CreditCard, R.string.methods_label),
    SourceTab(5, TablerIcons.BuildingBank, R.string.sources_label),
    ItemTab(6, TablerIcons.Stack, R.string.items_label)
}