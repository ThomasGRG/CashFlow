package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Typography
import jp.ikigai.cash.flow.ui.components.common.RoundedCornerOutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertItemSheet(
    name: String,
    items: List<String>,
    enabled: Boolean,
    save: (String) -> Unit,
    dismiss: () -> Unit,
    sheetState: SheetState,
    keyboardController: SoftwareKeyboardController?
) {
    var nameFieldValue by remember {
        mutableStateOf(
            TextFieldValue(name)
        )
    }

    var nameValid by remember {
        mutableStateOf(true)
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RoundedCornerOutlinedTextField(
                enabled = enabled,
                value = nameFieldValue,
                onValueChange = {
                    nameFieldValue = it
                    nameValid = true
                },
                label = "Name",
                placeHolder = "Enter item name",
                icon = TablerIcons.Typography,
                iconDescription = "name icon",
                isError = !nameValid,
                errorHint = "Name already in use",
                onDone = {
                    keyboardController?.hide()
                }
            )
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
                Text(text = "Cancel")
            }
            TextButton(
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
                Text(text = "Save")
            }
        }
    }
}