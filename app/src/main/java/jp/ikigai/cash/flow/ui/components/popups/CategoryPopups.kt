package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Archive
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Category
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.MultiSelectCard
import jp.ikigai.cash.flow.ui.components.common.SelectableCard
import jp.ikigai.cash.flow.utils.getHighlightedString

@Composable
fun SelectCategoryPopup(
    index: Int,
    selectedCategoryUUID: String,
    setSelectedCategory: (Category) -> Unit,
    categories: List<Category>,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember {
        FocusRequester()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isFocused by interactionSource.collectIsFocusedAsState()

    var searchText by remember {
        mutableStateOf("")
    }

    val categoryList by remember {
        mutableStateOf(
            categories.map { category ->
                Pair(category, getHighlightedString(category.name, ""))
            }
        )
    }

    var filteredCategoryList by remember {
        mutableStateOf(categoryList)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCategoryList = if (searchText.isBlank()) {
            categoryList
        } else {
            categoryList
                .filter {
                    it.first.name.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    Pair(it.first, getHighlightedString(it.first.name, searchText))
                }
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester = focusRequester),
                label = {
                    Text(text = stringResource(id = R.string.search_field_label))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                interactionSource = interactionSource
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .heightIn(max = 230.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredCategoryList,
                key = { categoryPair -> "category-${categoryPair.first.uuid}" }
            ) { categoryPair ->
                SelectableCard(
                    checked = { categoryPair.first.uuid == selectedCategoryUUID },
                    label = categoryPair.second,
                    icon = categoryPair.first.icon,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedCategory(categoryPair.first)
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
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
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isFocused) {
                        keyboardController?.show()
                    } else {
                        focusRequester.requestFocus()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.search_field_label))
            }
        }
    }
}

@Composable
fun MigrateCategoryPopup(
    migrateCount: Int,
    selectCategory: (Category) -> Unit,
    categories: List<Category>,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember {
        FocusRequester()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isFocused by interactionSource.collectIsFocusedAsState()

    var searchText by remember {
        mutableStateOf("")
    }

    val categoryList by remember {
        mutableStateOf(
            categories.map { category ->
                Pair(category, getHighlightedString(category.name, ""))
            }
        )
    }

    var filteredCategoryList by remember {
        mutableStateOf(categoryList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCategoryList = if (searchText.isBlank()) {
            categoryList
        } else {
            categoryList
                .filter {
                    it.first.name.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    Pair(it.first, getHighlightedString(it.first.name, searchText))
                }
        }
    }

    var selectedCategory by remember {
        mutableStateOf(Category())
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester = focusRequester),
                label = {
                    Text(text = stringResource(id = R.string.search_field_label))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                interactionSource = interactionSource
            )
        }
        LazyColumn(
            modifier = Modifier
                .heightIn(max = 230.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredCategoryList,
                key = { categoryPair -> "category-${categoryPair.first.uuid}" }
            ) { categoryPair ->
                SelectableCard(
                    checked = { categoryPair.first.uuid == selectedCategory.uuid },
                    label = categoryPair.second,
                    icon = categoryPair.first.icon,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCategory = categoryPair.first
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isFocused) {
                        keyboardController?.show()
                    } else {
                        focusRequester.requestFocus()
                    }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.search_field_label))
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    selectCategory(selectedCategory)
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedCategory.uuid.isNotEmpty(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        id = R.string.migrate_with_count_button_label,
                        migrateCount
                    )
                )
            }
        }
    }
}

@Composable
fun FilterCategoryPopup(
    selectedCategoryMap: Map<String, Boolean>,
    categories: List<Category>,
    filter: (Map<String, Boolean>) -> Unit,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember {
        FocusRequester()
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isFocused by interactionSource.collectIsFocusedAsState()

    var searchText by remember {
        mutableStateOf("")
    }

    val categoryList by remember {
        mutableStateOf(
            categories.map { category ->
                Pair(category, getHighlightedString(category.name, ""))
            }
        )
    }

    var filteredCategoryList by remember {
        mutableStateOf(categoryList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCategoryList = if (searchText.isBlank()) {
            categoryList
        } else {
            categoryList
                .filter {
                    it.first.name.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map {
                    Pair(it.first, getHighlightedString(it.first.name, searchText))
                }
        }
    }

    val selectedCategories = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedCategories.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredCategoryList
                .map { selectedCategories[it.first.uuid] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedCategories.putAll(selectedCategoryMap)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                },
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester = focusRequester),
                label = {
                    Text(text = stringResource(id = R.string.search_field_label))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                interactionSource = interactionSource
            )
        }
        LazyColumn(
            modifier = Modifier
                .heightIn(max = 230.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredCategoryList,
                key = { categoryPair -> "category-${categoryPair.first.uuid}" }
            ) { categoryPair ->
                MultiSelectCard(
                    checked = {
                        selectedCategories.getOrDefault(categoryPair.first.uuid, true)
                    },
                    label = categoryPair.second,
                    icon = categoryPair.first.icon,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCategories[categoryPair.first.uuid] =
                            !selectedCategories[categoryPair.first.uuid]!!
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredCategoryList.isEmpty()) {
                item(
                    key = "no_results"
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.choose_icon_screen_empty_placeholder_label,
                            searchText
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .animateItem()
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.cancel_button_label)
                )
            }
            FilledTonalIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val allSelected = filteredListSelectedCount == filteredCategoryList.size
                    filteredCategoryList
                        .map { category -> category.first.uuid }
                        .forEach { uuid -> selectedCategories[uuid] = !allSelected }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                AnimatedToggleSelectIcon(
                    deselectVisible = filteredListSelectedCount == filteredCategoryList.size
                )
            }
            FilledTonalIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isFocused) {
                        keyboardController?.show()
                    } else {
                        focusRequester.requestFocus()
                    }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            }
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                    filter(selectedCategories)
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedCount > 0,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        id = R.string.filter_button_with_count_label,
                        selectedCount
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun SelectCategoryPopupPreview() {
    SelectCategoryPopup(
        index = 1,
        selectedCategoryUUID = "asd",
        setSelectedCategory = {},
        categories = listOf(
            Category().apply {
                uuid = "asd"
                name = "Shopping"
                icon = TablerIcons.Archive
            },
            Category().apply {
                uuid = "asdfrg"
                name = "Transportation"
                icon = TablerIcons.Archive
            },
            Category().apply {
                uuid = "iurwuef"
                name = "Personal Care"
                icon = TablerIcons.Archive
            },
            Category().apply {
                uuid = "iurwueadfegf"
                name = "Food & Drinks"
                icon = TablerIcons.Archive
            }
        ),
        dismiss = {}
    )
}