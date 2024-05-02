package jp.ikigai.cash.flow.data.enums

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import compose.icons.tablericons.ArrowsSort
import compose.icons.tablericons.BuildingBank
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.CreditCard
import compose.icons.tablericons.Stack
import compose.icons.tablericons.Users

enum class FilterTabs(val index: Int, val icon: ImageVector, val text: String) {
    AmountTab(0, TablerIcons.CashBanknote, "Amount"),
    TransactionTypeTab(1, TablerIcons.ArrowsSort, "Transaction types"),
    CategoryTab(2, TablerIcons.Archive, "Categories"),
    CounterPartyTab(3, TablerIcons.Users, "Counter parties"),
    MethodTab(4, TablerIcons.CreditCard, "Methods"),
    SourceTab(5, TablerIcons.BuildingBank, "Sources"),
    ItemTab(6, TablerIcons.Stack, "Items")
}