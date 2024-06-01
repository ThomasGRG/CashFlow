package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.entity.TransactionTitle
import jp.ikigai.cash.flow.ui.components.buttons.ToggleButton
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField

@Composable
fun SearchTextFieldPopup(
    value: String,
    setValue: (String) -> Unit,
    enabled: Boolean,
    icon: ImageVector,
    iconDescription: String,
    label: String,
    placeholder: String,
    titles: List<TransactionTitle>,
    dismiss: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember {
        FocusRequester()
    }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(value, selection = TextRange(value.length))
        )
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RoundedCornerOutlinedTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
            },
            enabled = enabled,
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
            label = label,
            placeHolder = placeholder,
            icon = icon,
            iconDescription = iconDescription,
            onDone = {
                keyboardController?.hide()
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .clickable(
                    enabled = enabled,
                    onClick = {
                        focusRequester.requestFocus()
                    }
                )
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            userScrollEnabled = true
        ) {
            items(
                items = titles,
                key = { title -> title.uuid }
            ) { transactionTitle ->
                Row(
                    modifier = Modifier.padding(start = 3.dp, end = 3.dp)
                ) {
                    ToggleButton(
                        label = transactionTitle.title,
                        selected = false,
                        toggle = {
                            textFieldValue = TextFieldValue(
                                text = transactionTitle.title,
                                selection = TextRange(transactionTitle.title.length)
                            )
                        }
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(
                onClick = {
                    dismiss()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            Spacer(modifier = Modifier.width(10.dp))
            FilledTonalButton(
                onClick = {
                    dismiss()
                    setValue(textFieldValue.text)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(35),
            ) {
                Text(text = stringResource(id = R.string.set_button_label))
            }
        }
    }
}

@Preview
@Composable
fun SearchTextFieldPopupPreview() {
    SearchTextFieldPopup(
        value = "",
        setValue = {},
        enabled = true,
        icon = TablerIcons.Typography,
        iconDescription = "title icon",
        label = stringResource(id = R.string.title_field_label),
        placeholder = stringResource(id = R.string.title_placeholder_label),
        titles = listOf(
            TransactionTitle().apply {
                uuid = "asd"
                title = "Grand Hotel"
            },
            TransactionTitle().apply {
                uuid = "iuewf"
                title = "Grand Hotel"
            },
            TransactionTitle().apply {
                uuid = "asdaed"
                title = "Grand Hotel"
            },
            TransactionTitle().apply {
                uuid = "asdwqfg"
                title = "Grand Hotel"
            },
            TransactionTitle().apply {
                uuid = "asdqwe"
                title = "Grand Hotel"
            },
            TransactionTitle().apply {
                uuid = "asdrfdwa"
                title = "Grand Hotel"
            },
        ),
        dismiss = {}
    )
}