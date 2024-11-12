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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Users
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.CounterParty
import jp.ikigai.cash.flow.ui.components.common.SelectableCard
import jp.ikigai.cash.flow.utils.getHighlightedString

@Composable
fun SelectCounterPartyPopup(
    index: Int,
    selectedCounterPartyUUID: String,
    setSelectedCounterParty: (CounterParty) -> Unit,
    counterParties: List<CounterParty>,
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

    val counterPartyList by remember {
        mutableStateOf(
            counterParties.map { counterParty ->
                Pair(counterParty, getHighlightedString(counterParty.name, ""))
            }
        )
    }

    var filteredCounterPartyList by remember {
        mutableStateOf(counterPartyList)
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        listState.scrollToItem(index)
    }

    LaunchedEffect(key1 = searchText) {
        filteredCounterPartyList = if (searchText.isBlank()) {
            counterPartyList
        } else {
            counterPartyList
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
                items = filteredCounterPartyList,
                key = { counterPartyPair -> "counterParty-${counterPartyPair.first.uuid}" }
            ) { counterPartyPair ->
                SelectableCard(
                    checked = { counterPartyPair.first.uuid == selectedCounterPartyUUID },
                    label = counterPartyPair.second,
                    icon = counterPartyPair.first.icon,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        dismiss()
                        setSelectedCounterParty(counterPartyPair.first)
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
fun SelectCounterPartyPopupPreview() {
    SelectCounterPartyPopup(
        index = 1,
        selectedCounterPartyUUID = "asd",
        setSelectedCounterParty = {},
        counterParties = listOf(
            CounterParty().apply {
                uuid = "asd"
                name = "Shopping"
                icon = TablerIcons.Users
            },
            CounterParty().apply {
                uuid = "asdfrg"
                name = "Transportation"
                icon = TablerIcons.Users
            },
            CounterParty().apply {
                uuid = "iurwuef"
                name = "Personal Care"
                icon = TablerIcons.Users
            },
            CounterParty().apply {
                uuid = "iurwueadfegf"
                name = "Food & Drinks"
                icon = TablerIcons.Users
            }
        ),
        dismiss = {}
    )
}