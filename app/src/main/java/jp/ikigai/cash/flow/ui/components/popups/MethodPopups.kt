package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Constants
import jp.ikigai.cash.flow.data.store.entity.Method
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.MultiSelectCard
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import jp.ikigai.cash.flow.ui.components.common.SelectableCard
import jp.ikigai.cash.flow.utils.getHighlightedString

@Composable
fun SelectMethodPopup(
    index: Int,
    selectedMethodId: Long,
    setSelectedMethod: (Method) -> Unit,
    methods: List<Method>,
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

    val methodList by remember {
        mutableStateOf(
            methods.map { method ->
                Pair(method, getHighlightedString(method.name, ""))
            }
        )
    }

    var filteredMethodList by remember {
        mutableStateOf(methodList)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredMethodList = if (searchText.isBlank()) {
            methodList
        } else {
            methodList
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
        SearchBox(
            searchText = searchText,
            setSearchText = {
                searchText = it
            },
            focusRequester = focusRequester,
            interactionSource = interactionSource
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.height(230.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredMethodList,
                key = { (method, _) -> "method-${method.id}" }
            ) { (method, annotatedName) ->
                SelectableCard(
                    checked = { method.id == selectedMethodId },
                    label = annotatedName,
                    icon = Constants.DEFAULT_METHOD_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedMethod(method)
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredMethodList.isEmpty()) {
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
fun MigrateMethodPopup(
    migrateCount: Int,
    selectMethod: (Method) -> Unit,
    methods: List<Method>,
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

    val methodList by remember {
        mutableStateOf(
            methods.map { method ->
                Pair(method, getHighlightedString(method.name, ""))
            }
        )
    }

    var filteredMethodList by remember {
        mutableStateOf(methodList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredMethodList = if (searchText.isBlank()) {
            methodList
        } else {
            methodList
                .filter { (method, _) ->
                    method.name.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map { (method, _) ->
                    Pair(method, getHighlightedString(method.name, searchText))
                }
        }
    }

    var selectedMethod by remember {
        mutableStateOf(Method())
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchBox(
            searchText = searchText,
            setSearchText = {
                searchText = it
            },
            focusRequester = focusRequester,
            interactionSource = interactionSource
        )
        LazyColumn(
            modifier = Modifier.height(230.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredMethodList,
                key = { (method, _) -> "method-${method.id}" }
            ) { (method, annotatedName) ->
                SelectableCard(
                    checked = { method.id == selectedMethod.id },
                    label = annotatedName,
                    icon = Constants.DEFAULT_METHOD_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedMethod = method
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredMethodList.isEmpty()) {
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
                    selectMethod(selectedMethod)
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
                enabled = selectedMethod.id > 0,
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
fun FilterMethodPopup(
    selectedMethodsMap: Map<Long, Boolean>,
    methods: List<Method>,
    filter: (Map<Long, Boolean>) -> Unit,
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

    val methodList by remember {
        mutableStateOf(
            methods.map { method ->
                Pair(method, getHighlightedString(method.name, ""))
            }
        )
    }

    var filteredMethodList by remember {
        mutableStateOf(methodList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredMethodList = if (searchText.isBlank()) {
            methodList
        } else {
            methodList
                .filter { (method, _) ->
                    method.name.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (method, _) ->
                    Pair(method, getHighlightedString(method.name, searchText))
                }
        }
    }

    val selectedMethods = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedMethods.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredMethodList
                .map { (method, _) -> selectedMethods[method.id] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedMethods.putAll(selectedMethodsMap)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchBox(
            searchText = searchText,
            setSearchText = {
                searchText = it
            },
            focusRequester = focusRequester,
            interactionSource = interactionSource
        )
        LazyColumn(
            modifier = Modifier.height(230.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredMethodList,
                key = { (method, _) -> "method-${method.id}" }
            ) { (method, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedMethods.getOrDefault(method.id, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_METHOD_ICON,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedMethods[method.id] = !selectedMethods[method.id]!!
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredMethodList.isEmpty()) {
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val allSelected = filteredListSelectedCount == filteredMethodList.size
                    filteredMethodList
                        .map { (method, _) -> method.id }
                        .forEach { id -> selectedMethods[id] = !allSelected }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                AnimatedToggleSelectIcon(
                    deselectVisible = filteredListSelectedCount == filteredMethodList.size
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
                    filter(selectedMethods)
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

@Composable
fun FilterMethodPopup(
    selectedMethodIds: Set<Long>,
    methods: List<Method>,
    filter: (Set<Long>) -> Unit,
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

    val methodList by remember {
        mutableStateOf(
            methods.map { method ->
                Pair(method, getHighlightedString(method.name, ""))
            }
        )
    }

    var filteredMethodList by remember {
        mutableStateOf(methodList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredMethodList = if (searchText.isBlank()) {
            methodList
        } else {
            methodList
                .filter { (method, _) ->
                    method.name.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map { (method, _) ->
                    Pair(method, getHighlightedString(method.name, searchText))
                }
        }
    }

    val selectedMethods = remember {
        mutableStateMapOf<Long, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedMethods.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredMethodList
                .map { (method, _) -> selectedMethods[method.id] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        methods.forEach { method ->
            selectedMethods[method.id] = selectedMethodIds.contains(method.id)
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SearchBox(
            searchText = searchText,
            setSearchText = {
                searchText = it
            },
            focusRequester = focusRequester,
            interactionSource = interactionSource
        )
        LazyColumn(
            modifier = Modifier.height(230.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = filteredMethodList,
                key = { (method, _) -> "method-${method.id}" }
            ) { (method, annotatedName) ->
                MultiSelectCard(
                    checked = {
                        selectedMethods.getOrDefault(method.id, true)
                    },
                    label = annotatedName,
                    icon = Constants.DEFAULT_METHOD_ICON,
                    onClick = { newCheckState ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedMethods[method.id] = newCheckState
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredMethodList.isEmpty()) {
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
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            FilledTonalIconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    val allSelected = filteredListSelectedCount == filteredMethodList.size
                    filteredMethodList
                        .map { (method, _) -> method.id }
                        .forEach { id -> selectedMethods[id] = !allSelected }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                AnimatedToggleSelectIcon(
                    deselectVisible = filteredListSelectedCount == filteredMethodList.size
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
                    filter(
                        selectedMethods.filter { entry -> entry.value }.keys
                    )
                    dismiss()
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
