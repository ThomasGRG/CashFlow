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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.BuildingBank
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.ui.components.common.AnimatedToggleSelectIcon
import jp.ikigai.cash.flow.ui.components.common.MultiSelectCard
import jp.ikigai.cash.flow.ui.components.common.SelectableCard
import jp.ikigai.cash.flow.utils.getHighlightedString

@Composable
fun SelectSourcePopup(
    index: Int,
    selectedSourceUUID: String,
    setSelectedSource: (Source) -> Unit,
    sources: List<Source>,
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

    val sourceList by remember {
        mutableStateOf(
            sources.map { source ->
                val highlightedString = getHighlightedString(source.name, "")
                Pair(
                    source,
                    highlightedString.plus(AnnotatedString(" - ${source.displayBalance}"))
                )
            }
        )
    }

    var filteredSourceList by remember {
        mutableStateOf(sourceList)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredSourceList = if (searchText.isBlank()) {
            sourceList
        } else {
            sourceList
                .filter {
                    it.first.name.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
                .map {
                    val highlightedString = getHighlightedString(it.first.name, searchText)
                    Pair(
                        it.first,
                        highlightedString.plus(AnnotatedString(" - ${it.first.displayBalance}"))
                    )
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
                items = filteredSourceList,
                key = { sourcePair -> "source-${sourcePair.first.uuid}" }
            ) { sourcePair ->
                SelectableCard(
                    checked = { sourcePair.first.uuid == selectedSourceUUID },
                    label = sourcePair.second,
                    icon = sourcePair.first.icon,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedSource(sourcePair.first)
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
fun FilterSourcePopup(
    selectedSourcesMap: Map<String, Boolean>,
    sources: List<Source>,
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

    val sourceList by remember {
        mutableStateOf(
            sources.map { source ->
                val highlightedString = getHighlightedString(source.name, "")
                Pair(
                    source,
                    highlightedString.plus(AnnotatedString(" - ${source.displayBalance}"))
                )
            }
        )
    }

    var filteredSourceList by remember {
        mutableStateOf(sourceList)
    }

    LaunchedEffect(key1 = searchText) {
        filteredSourceList = if (searchText.isBlank()) {
            sourceList
        } else {
            sourceList
                .filter {
                    it.first.name.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
                .map {
                    val highlightedString = getHighlightedString(it.first.name, searchText)
                    Pair(
                        it.first,
                        highlightedString.plus(AnnotatedString(" - ${it.first.displayBalance}")),
                    )
                }
        }
    }

    val selectedSources = remember {
        mutableStateMapOf<String, Boolean>()
    }

    val selectedCount by remember {
        derivedStateOf {
            selectedSources.filter { it.value }.size
        }
    }

    val filteredListSelectedCount by remember {
        derivedStateOf {
            filteredSourceList
                .map { selectedSources[it.first.uuid] }
                .filter { it == true }
                .size
        }
    }

    LaunchedEffect(Unit) {
        selectedSources.putAll(selectedSourcesMap)
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
                items = filteredSourceList,
                key = { sourcePair -> "source-${sourcePair.first.uuid}" }
            ) { sourcePair ->
                MultiSelectCard(
                    checked = {
                        selectedSources.getOrDefault(sourcePair.first.uuid, true)
                    },
                    label = sourcePair.second,
                    icon = sourcePair.first.icon,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedSources[sourcePair.first.uuid] =
                            !selectedSources[sourcePair.first.uuid]!!
                    },
                    modifier = Modifier.animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredSourceList.isEmpty()) {
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
                    val allSelected = filteredListSelectedCount == filteredSourceList.size
                    filteredSourceList
                        .map { category -> category.first.uuid }
                        .forEach { uuid -> selectedSources[uuid] = !allSelected }
                },
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                AnimatedToggleSelectIcon(
                    deselectVisible = filteredListSelectedCount == filteredSourceList.size
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
                    filter(selectedSources)
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
fun SelectSourcePopupPreview() {
    SelectSourcePopup(
        index = 1,
        selectedSourceUUID = "asd",
        setSelectedSource = {},
        sources = listOf(
            Source().apply {
                uuid = "asd"
                name = "Shopping"
                icon = TablerIcons.BuildingBank
                balance = 937.00
                currency = "INR"
            },
            Source().apply {
                uuid = "asdfrg"
                name = "Transportation"
                icon = TablerIcons.BuildingBank
                balance = 937.65
                currency = "INR"
            },
            Source().apply {
                uuid = "iurwuef"
                name = "Personal Care"
                icon = TablerIcons.BuildingBank
                balance = 937.65
                currency = "INR"
            },
            Source().apply {
                uuid = "iurwueadfegf"
                name = "Food & Drinks"
                icon = TablerIcons.BuildingBank
                balance = 937.65
                currency = "INR"
            }
        ),
        dismiss = {}
    )
}