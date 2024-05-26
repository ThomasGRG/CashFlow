package jp.ikigai.cash.flow.ui.components.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField

@Composable
fun UpsertItemPopup(
    name: String,
    items: List<String>,
    enabled: Boolean,
    save: (String) -> Unit,
    dismiss: () -> Unit,
    keyboardController: SoftwareKeyboardController?
) {
    val focusRequester = remember {
        FocusRequester()
    }

    var nameFieldValue by remember {
        mutableStateOf(
            TextFieldValue(name)
        )
    }

    var nameValid by remember {
        mutableStateOf(true)
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RoundedCornerOutlinedTextField(
            enabled = enabled,
            value = nameFieldValue,
            onValueChange = {
                nameFieldValue = it
                nameValid = true
            },
            label = stringResource(id = R.string.name_field_label),
            placeHolder = stringResource(id = R.string.name_placeholder_label),
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.0.dp),
            icon = TablerIcons.Typography,
            iconDescription = "name icon",
            isError = !nameValid,
            errorHint = stringResource(id = R.string.name_in_use_label),
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    dismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = {
                    if (nameFieldValue.text != name && items.contains(nameFieldValue.text)) {
                        nameValid = false
                    } else {
                        dismiss()
                        save(nameFieldValue.text)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = nameFieldValue.text.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.save_button_label))
            }
        }
    }
}