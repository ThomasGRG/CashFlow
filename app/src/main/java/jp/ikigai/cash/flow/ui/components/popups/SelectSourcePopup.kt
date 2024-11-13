package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import compose.icons.TablerIcons
import compose.icons.tablericons.BuildingBank
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.Source
import jp.ikigai.cash.flow.ui.components.common.SelectableCard
import jp.ikigai.cash.flow.utils.getHighlightedString
import jp.ikigai.cash.flow.utils.getNumberFormatter

@Composable
fun SelectSourcePopup(
    index: Int,
    selectedSourceUUID: String,
    setSelectedSource: (Source) -> Unit,
    sources: List<Source>,
    dismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
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

    val numberFormatter = remember(key1 = configuration) {
        getNumberFormatter(
            ConfigurationCompat.getLocales(configuration).get(0)
        )
    }

    val sourceList by remember(key1 = numberFormatter) {
        mutableStateOf(
            sources.map { source ->
                val highlightedString = getHighlightedString(source.name, "")
                val balance = numberFormatter.format(source.balance)
                Triple(
                    source,
                    highlightedString.plus(AnnotatedString(" - $balance ${source.currency}")),
                    balance
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
                    Triple(
                        it.first,
                        highlightedString.plus(AnnotatedString(" - ${it.third} ${it.first.currency}")),
                        it.third
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