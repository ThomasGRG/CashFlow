package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.Users
import jp.ikigai.cash.flow.data.dto.Filters
import jp.ikigai.cash.flow.data.enums.FilterTabs
import jp.ikigai.cash.flow.ui.components.buttons.IconToggleButton
import jp.ikigai.cash.flow.ui.components.buttons.IconToggleRow
import jp.ikigai.cash.flow.ui.components.buttons.ToggleRow
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun FilterSheet(
    filters: Filters,
    setFilters: (Filters) -> Unit,
    dismiss: () -> Unit,
    maxHeight: Double,
    sheetState: SheetState,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        pageCount = { FilterTabs.values().size },
        initialPage = 0
    )

    val selectedTabIndex by remember {
        derivedStateOf { pagerState.currentPage }
    }

    val transactionTypes by remember(filters.transactionTypes) {
        mutableStateOf(filters.transactionTypes)
    }

    var selectedTransactionTypes by remember(filters.selectedTransactionTypes) {
        mutableStateOf(filters.selectedTransactionTypes)
    }

    val categories by remember(filters.categories) {
        mutableStateOf(filters.categories)
    }

    var selectedCategories by remember(filters.selectedCategories) {
        mutableStateOf(filters.selectedCategories)
    }

    val counterParties by remember(filters.counterParties) {
        mutableStateOf(filters.counterParties)
    }

    var selectedCounterParties by remember(filters.selectedCounterParties) {
        mutableStateOf(filters.selectedCounterParties)
    }

    var includeNoCounterPartyTransactions by remember(filters.includeNoCounterPartyTransactions) {
        mutableStateOf(filters.includeNoCounterPartyTransactions)
    }

    val methods by remember(filters.methods) {
        mutableStateOf(filters.methods)
    }

    var selectedMethods by remember(filters.selectedMethods) {
        mutableStateOf(filters.selectedMethods)
    }

    val sources by remember(filters.sources) {
        mutableStateOf(filters.sources)
    }

    var selectedSources by remember(filters.selectedSources) {
        mutableStateOf(filters.selectedSources)
    }

    val items by remember(filters.items) {
        mutableStateOf(filters.items)
    }

    var selectedItems by remember(filters.selectedItems) {
        mutableStateOf(filters.selectedItems)
    }

    var includeNoItemTransactions by remember(filters.includeNoItemTransactions) {
        mutableStateOf(filters.includeNoItemTransactions)
    }

    var fromAmount by remember {
        mutableStateOf(filters.filterAmountMin)
    }

    var displayFromAmount by remember {
        mutableStateOf(filters.filterAmountMin.toString())
    }

    var toAmount by remember {
        mutableStateOf(filters.filterAmountMax)
    }

    var displayToAmount by remember {
        mutableStateOf(filters.filterAmountMax.toString())
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight.dp)
        ) {
            SecondaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterTabs.values().forEach { tab ->
                    LeadingIconTab(
                        selected = selectedTabIndex == tab.index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(tab.index)
                            }
                        },
                        text = {
                            Text(text = tab.text)
                        },
                        icon = {
                            Icon(imageVector = tab.icon, contentDescription = tab.icon.name)
                        }
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, top = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            RoundedCornerOutlinedTextField(
                                value = displayFromAmount,
                                onValueChange = {
                                    displayFromAmount = it
                                    val newFromAmount = it.toDoubleOrNull()
                                    fromAmount = newFromAmount ?: 0.0
                                },
                                enabled = true,
                                label = "Minimum amount",
                                placeHolder = "Enter minimum amount",
                                icon = TablerIcons.CashBanknote,
                                iconDescription = "minimum amount icon",
                                onDone = {
                                    keyboardController?.hide()
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            RoundedCornerOutlinedTextField(
                                value = displayToAmount,
                                onValueChange = {
                                    displayToAmount = it
                                    val newToAmount = it.toDoubleOrNull()
                                    toAmount = newToAmount ?: 0.0
                                },
                                enabled = true,
                                label = "Maximum amount",
                                placeHolder = "Enter maximum amount",
                                icon = TablerIcons.CashBanknote,
                                iconDescription = "maximum amount icon",
                                onDone = {
                                    keyboardController?.hide()
                                }
                            )
                        }
                    }

                    1 -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp, top = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            transactionTypes.forEach { transactionType ->
                                Spacer(modifier = Modifier.width(5.dp))
                                IconToggleButton(
                                    label = transactionType.name,
                                    icon = transactionType.icon,
                                    selected = selectedTransactionTypes.contains(transactionType.id),
                                    toggle = {
                                        val types = selectedTransactionTypes.toMutableList()
                                        if (selectedTransactionTypes.contains(transactionType.id)) {
                                            if (selectedTransactionTypes.size > 1) {
                                                types.remove(transactionType.id)
                                            }
                                        } else {
                                            types.add(transactionType.id)
                                        }
                                        selectedTransactionTypes = types
                                    }
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                        }
                    }

                    2 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 20.dp)
                        ) {
                            items(
                                items = categories,
                                key = { category -> category.uuid }
                            ) { category ->
                                IconToggleRow(
                                    label = category.name,
                                    icon = category.icon,
                                    selected = selectedCategories.contains(category.uuid),
                                    onClick = {
                                        val categoryList = selectedCategories.toMutableList()
                                        if (selectedCategories.contains(category.uuid)) {
                                            if (selectedCategories.size > 1) {
                                                categoryList.remove(category.uuid)
                                            }
                                        } else {
                                            categoryList.add(category.uuid)
                                        }
                                        selectedCategories = categoryList
                                    }
                                )
                            }
                        }
                    }

                    3 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 20.dp)
                        ) {
                            item(
                                key = "noCounterPartyChip"
                            ) {
                                IconToggleRow(
                                    selected = includeNoCounterPartyTransactions,
                                    onClick = {
                                        includeNoCounterPartyTransactions =
                                            if (selectedCounterParties.isNotEmpty()) {
                                                !includeNoCounterPartyTransactions
                                            } else {
                                                true
                                            }
                                    },
                                    label = "No counter party",
                                    icon = TablerIcons.Users
                                )
                            }
                            items(
                                items = counterParties,
                                key = { counterParty -> counterParty.uuid }
                            ) { counterParty ->
                                IconToggleRow(
                                    label = counterParty.name,
                                    icon = counterParty.icon,
                                    selected = selectedCounterParties.contains(counterParty.uuid),
                                    onClick = {
                                        val counterPartyList =
                                            selectedCounterParties.toMutableList()
                                        if (selectedCounterParties.contains(counterParty.uuid)) {
                                            if (includeNoCounterPartyTransactions) {
                                                counterPartyList.remove(counterParty.uuid)
                                            } else if (selectedCounterParties.size > 1) {
                                                counterPartyList.remove(counterParty.uuid)
                                            }
                                        } else {
                                            counterPartyList.add(counterParty.uuid)
                                        }
                                        selectedCounterParties = counterPartyList
                                    }
                                )
                            }
                        }
                    }

                    4 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 20.dp)
                        ) {
                            items(
                                items = methods,
                                key = { method -> method.uuid }
                            ) { method ->
                                IconToggleRow(
                                    label = method.name,
                                    icon = method.icon,
                                    selected = selectedMethods.contains(method.uuid),
                                    onClick = {
                                        val methodList = selectedMethods.toMutableList()
                                        if (selectedMethods.contains(method.uuid)) {
                                            if (selectedMethods.size > 1) {
                                                methodList.remove(method.uuid)
                                            }
                                        } else {
                                            methodList.add(method.uuid)
                                        }
                                        selectedMethods = methodList
                                    }
                                )
                            }
                        }
                    }

                    5 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 20.dp)
                        ) {
                            items(
                                items = sources,
                                key = { source -> source.uuid }
                            ) { source ->
                                IconToggleRow(
                                    label = source.name,
                                    icon = source.icon,
                                    selected = selectedSources.contains(source.uuid),
                                    onClick = {
                                        val sourceList = selectedSources.toMutableList()
                                        if (selectedSources.contains(source.uuid)) {
                                            if (selectedSources.size > 1) {
                                                sourceList.remove(source.uuid)
                                            }
                                        } else {
                                            sourceList.add(source.uuid)
                                        }
                                        selectedSources = sourceList
                                    }
                                )
                            }
                        }
                    }

                    6 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 20.dp)
                        ) {
                            item(
                                key = "noItemChip"
                            ) {
                                ToggleRow(
                                    selected = includeNoItemTransactions,
                                    onClick = {
                                        includeNoItemTransactions = if (selectedItems.isNotEmpty()) {
                                            !includeNoItemTransactions
                                        } else {
                                            true
                                        }
                                    },
                                    label = "No items",
                                    identifier = ""
                                )
                            }
                            items(
                                items = items,
                                key = { item -> item.uuid }
                            ) { item ->
                                ToggleRow(
                                    identifier = item.uuid,
                                    label = item.name,
                                    selected = selectedItems.contains(item.uuid),
                                    onClick = { uuid ->
                                        val itemList = selectedItems.toMutableList()
                                        if (selectedItems.contains(uuid)) {
                                            if (includeNoItemTransactions) {
                                                itemList.remove(uuid)
                                            } else if (selectedItems.size > 1) {
                                                itemList.remove(uuid)
                                            }
                                        } else {
                                            itemList.add(uuid)
                                        }
                                        selectedItems = itemList
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = {
                    dismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Cancel")
            }
            TextButton(
                onClick = {
                    setFilters(
                        filters.copy(
                            selectedCategories = selectedCategories,
                            selectedCounterParties = selectedCounterParties,
                            includeNoCounterPartyTransactions = includeNoCounterPartyTransactions,
                            selectedMethods = selectedMethods,
                            selectedSources = selectedSources,
                            selectedItems = selectedItems,
                            includeNoItemTransactions = includeNoItemTransactions,
                            selectedTransactionTypes = selectedTransactionTypes,
                            filterAmountMin = fromAmount,
                            filterAmountMax = toAmount,
                        )
                    )
                    dismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Filter")
            }
        }
    }
}