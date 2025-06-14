package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.AllIcons
import compose.icons.TablerIcons
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.ui.components.common.SearchBox
import kotlinx.coroutines.delay

@Composable
fun ChooseIconPopup(
    dismiss: () -> Unit,
    setIcon: (ImageVector) -> Unit
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

    val icons by remember {
        mutableStateOf(TablerIcons.AllIcons)
    }

    var filteredIconList by remember {
        mutableStateOf(icons)
    }

    LaunchedEffect(key1 = searchText) {
        delay(300)
        filteredIconList = if (searchText.isBlank()) {
            icons
        } else {
            icons
                .filter { icon ->
                    icon.name.contains(
                        searchText.trim(),
                        ignoreCase = true
                    )
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(top = 10.dp, start = 20.dp, end = 20.dp, bottom = 10.dp),
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
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(58.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 250.dp)
                .animateContentSize()
        ) {
            items(
                items = filteredIconList,
                key = { icon -> "icon-${icon.name}" }
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = icon.name,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(52.dp)
                        .padding(6.dp)
                        .clickable(
                            enabled = true,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                setIcon(icon)
                                dismiss()
                            },
                        )
                        .animateItem()
                )
            }
            if (searchText.isNotBlank() && filteredIconList.isEmpty()) {
                item(
                    key = "no_results",
                    span = StaggeredGridItemSpan.FullLine
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
                            .padding(horizontal = 10.dp, vertical = 20.dp)
                            .animateItem()
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    dismiss()
                },
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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
                    .height(50.dp)
                    .weight(1f),
                shape = RoundedCornerShape(35),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = stringResource(id = R.string.search_field_label))
            }
        }
    }
}

@Preview
@Composable
fun ChooseIconPopupPreview() {
    ChooseIconPopup(
        dismiss = {},
        setIcon = {}
    )
}