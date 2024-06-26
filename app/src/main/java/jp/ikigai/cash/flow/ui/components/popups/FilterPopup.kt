package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LeadingIconTab
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CashBanknote
import compose.icons.tablericons.Users
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.dto.Filters
import jp.ikigai.cash.flow.data.enums.FilterTabs
import jp.ikigai.cash.flow.ui.components.buttons.IconToggleRow
import jp.ikigai.cash.flow.ui.components.buttons.ToggleRow
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterPopup(
    filters: Filters,
    setFilters: (Filters) -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
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

    val filterEnabled by remember(
        selectedCategories,
        selectedCounterParties,
        selectedItems,
        selectedMethods,
        selectedSources
    ) {
        derivedStateOf {
            selectedCategories.containsValue(true) &&
                    (selectedCounterParties.containsValue(true) || includeNoCounterPartyTransactions) &&
                    (selectedItems.containsValue(true) || includeNoItemTransactions) &&
                    selectedMethods.containsValue(true) &&
                    selectedSources.containsValue(true)
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(top = 20.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ) {
            FilterTabs.values().forEach { tab ->
                LeadingIconTab(
                    selected = selectedTabIndex == tab.index,
                    onClick = {
                        scope.launch {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            pagerState.scrollToPage(tab.index)
                        }
                    },
                    text = {
                        Text(text = stringResource(id = tab.text))
                    },
                    icon = {
                        Icon(imageVector = tab.icon, contentDescription = tab.icon.name)
                    }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .heightIn(max = 200.dp),
        ) { pageIndex ->
            when (pageIndex) {
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                            label = stringResource(id = R.string.minimum_amount_field_label),
                            placeHolder = stringResource(id = R.string.minimum_amount_placeholder_label),
                            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                3.dp
                            ),
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
                            label = stringResource(id = R.string.maximum_amount_field_label),
                            placeHolder = stringResource(id = R.string.maximum_amount_placeholder_label),
                            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                3.dp
                            ),
                            icon = TablerIcons.CashBanknote,
                            iconDescription = "maximum amount icon",
                            onDone = {
                                keyboardController?.hide()
                            }
                        )
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        transactionTypes.forEach { transactionType ->
                            IconToggleRow(
                                label = transactionType.name,
                                icon = transactionType.icon,
                                selected = selectedTransactionTypes.contains(transactionType.id),
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        }
                    }
                }

                2 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        items(
                            items = categories,
                            key = { category -> category.uuid }
                        ) { category ->
                            IconToggleRow(
                                label = category.name,
                                icon = category.icon,
                                selected = selectedCategories.getOrDefault(
                                    category.uuid,
                                    false
                                ),
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val categoryList = selectedCategories.toMutableMap()
                                    categoryList[category.uuid] =
                                        !categoryList.getOrDefault(category.uuid, true)
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
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        item(
                            key = "noCounterPartyChip"
                        ) {
                            IconToggleRow(
                                selected = includeNoCounterPartyTransactions,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    includeNoCounterPartyTransactions =
                                        if (selectedCounterParties.containsValue(true)) {
                                            !includeNoCounterPartyTransactions
                                        } else {
                                            true
                                        }
                                },
                                label = stringResource(id = R.string.no_counter_party_label),
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
                                selected = selectedCounterParties.getOrDefault(
                                    counterParty.uuid,
                                    false
                                ),
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val counterPartyList =
                                        selectedCounterParties.toMutableMap()
                                    counterPartyList[counterParty.uuid] =
                                        !counterPartyList.getOrDefault(counterParty.uuid, true)
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
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        items(
                            items = methods,
                            key = { method -> method.uuid }
                        ) { method ->
                            IconToggleRow(
                                label = method.name,
                                icon = method.icon,
                                selected = selectedMethods.getOrDefault(method.uuid, false),
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val methodList = selectedMethods.toMutableMap()
                                    methodList[method.uuid] =
                                        !methodList.getOrDefault(method.uuid, true)
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
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        items(
                            items = sources,
                            key = { source -> source.uuid }
                        ) { source ->
                            IconToggleRow(
                                label = source.name,
                                icon = source.icon,
                                selected = selectedSources.getOrDefault(source.uuid, false),
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val sourceList = selectedSources.toMutableMap()
                                    sourceList[source.uuid] =
                                        !sourceList.getOrDefault(source.uuid, true)
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
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        item(
                            key = "noItemChip"
                        ) {
                            ToggleRow(
                                selected = includeNoItemTransactions,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    includeNoItemTransactions =
                                        if (selectedItems.containsValue(true)) {
                                            !includeNoItemTransactions
                                        } else {
                                            true
                                        }
                                },
                                label = stringResource(id = R.string.no_item_label),
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
                                selected = selectedItems.getOrDefault(item.uuid, false),
                                onClick = { uuid ->
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val itemList = selectedItems.toMutableMap()
                                    itemList[uuid] = !itemList.getOrDefault(uuid, true)
                                    selectedItems = itemList
                                }
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 10.dp, start = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalButton(
                enabled = filterEnabled,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.filter_button_label))
            }
        }
    }
}

@Preview
@Composable
fun FilterPopupPreview() {
    FilterPopup(filters = Filters(), setFilters = {}, dismiss = {})
}