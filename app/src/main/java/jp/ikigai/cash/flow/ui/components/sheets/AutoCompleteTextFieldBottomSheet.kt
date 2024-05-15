package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextFieldBottomSheet(
    value: String,
    setValue: (String) -> Unit,
    enabled: Boolean,
    icon: ImageVector,
    iconDescription: String,
    label: String,
    placeholder: String,
    dismiss: () -> Unit,
    sheetState: SheetState,
    items: LazyListScope.((String) -> Unit) -> Unit,
) {
    val focusRequester = remember {
        FocusRequester()
    }

    LaunchedEffect(Unit) {
        delay(500)
        focusRequester.requestFocus()
    }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(value, selection = TextRange(value.length))
        )
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
        ) {
            RoundedCornerOutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                },
                enabled = enabled,
                label = label,
                placeHolder = placeholder,
                modifier = Modifier.focusRequester(focusRequester),
                icon = icon,
                iconDescription = iconDescription,
                onDone = {}
            )
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(start = 10.dp, top = 10.dp)
        ) {
            items {
                textFieldValue = TextFieldValue(text = it, selection = TextRange(it.length))
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
                Text(text = stringResource(id = R.string.cancel_button_label))
            }
            TextButton(
                onClick = {
                    setValue(textFieldValue.text)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(id = R.string.set_button_label))
            }
        }
    }
}