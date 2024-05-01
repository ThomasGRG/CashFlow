package jp.ikigai.cash.flow.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.CurrencyDollar
import compose.icons.tablericons.Users
import jp.ikigai.cash.flow.data.dto.Filters

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    filters: Filters,
    setFilters: (Filters) -> Unit,
    dismiss: () -> Unit,
    maxHeight: Double,
    sheetState: SheetState,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val transactionTypes by remember(filters.transactionTypes) {
        mutableStateOf(filters.transactionTypes)
    }

    var selectedTransactionTypes by remember(filters.selectedTransactionTypes) {
        mutableStateOf(filters.selectedTransactionTypes)
    }

    val categories by remember(filters.categories) {
        mutableStateOf(filters.categories)
    }

    var selectedCategories by remember(filters.selectedCategories) {
        mutableStateOf(filters.selectedCategories)
    }

    val counterParties by remember(filters.counterParties) {
        mutableStateOf(filters.counterParties)
    }

    var selectedCounterParties by remember(filters.selectedCounterParties) {
        mutableStateOf(filters.selectedCounterParties)
    }

    val methods by remember(filters.methods) {
        mutableStateOf(filters.methods)
    }

    var selectedMethods by remember(filters.selectedMethods) {
        mutableStateOf(filters.selectedMethods)
    }

    val sources by remember(filters.sources) {
        mutableStateOf(filters.sources)
    }

    var selectedSources by remember(filters.selectedSources) {
        mutableStateOf(filters.selectedSources)
    }

    var fromAmount by remember {
        mutableStateOf(filters.filterAmountMin)
    }

    var toAmount by remember {
        mutableStateOf(filters.filterAmountMax)
    }

    // TODO: Replace flow row with contextual flow row when stable and use tabs to separate items
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            dismiss()
        },
        shape = RoundedCornerShape(10)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight.dp)
                .verticalScroll(scrollState)
                .padding(10.dp)
        ) {
            Text(
                text = "Amount",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 4.dp)
                ) {
                    OutlinedTextField(
                        value = "$fromAmount",
                        onValueChange = {
                            val amount = it.toDoubleOrNull()
                            fromAmount = amount ?: 0.0
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrect = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = TablerIcons.CurrencyDollar,
                                contentDescription = "amount icon",
                            )
                        },
                        label = {
                            Text(
                                text = "From"
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp, end = 4.dp)
                ) {
                    OutlinedTextField(
                        value = "$toAmount",
                        onValueChange = {
                            val amount = it.toDoubleOrNull()
                            toAmount = amount ?: 0.0
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrect = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = TablerIcons.CurrencyDollar,
                                contentDescription = "amount icon",
                            )
                        },
                        label = {
                            Text(
                                text = "To"
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                    )
                }
            }
            Text(
                text = "Transaction types",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                transactionTypes.forEach { transactionType ->
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                    ) {
                        FilterChip(
                            selected = selectedTransactionTypes.contains(transactionType.id),
                            onClick = {
                                val types = selectedTransactionTypes.toMutableList()
                                if (selectedTransactionTypes.contains(transactionType.id)) {
                                    if (selectedTransactionTypes.size > 1) {
                                        types.remove(transactionType.id)
                                    }
                                } else {
                                    types.add(transactionType.id)
                                }
                                selectedTransactionTypes = types
                            },
                            label = {
                                Text(text = transactionType.name)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = transactionType.icon,
                                    contentDescription = transactionType.icon.name
                                )
                            }
                        )
                    }
                }
            }
            Text(
                text = "Categories",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                categories.forEach { category ->
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategories.contains(category.uuid),
                            onClick = {
                                val categoryList = selectedCategories.toMutableList()
                                if (selectedCategories.contains(category.uuid)) {
                                    if (selectedCategories.size > 1) {
                                        categoryList.remove(category.uuid)
                                    }
                                } else {
                                    categoryList.add(category.uuid)
                                }
                                selectedCategories = categoryList
                            },
                            label = {
                                Text(text = category.name)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.icon.name
                                )
                            }
                        )
                    }
                }
            }
            Text(
                text = "Counter parties",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                ) {
                    FilterChip(
                        selected = selectedCounterParties.contains(""),
                        onClick = {
                            val counterPartyList = selectedCounterParties.toMutableList()
                            if (selectedCounterParties.contains("")) {
                                if (selectedCounterParties.size > 1) {
                                    counterPartyList.remove("")
                                }
                            } else {
                                counterPartyList.add("")
                            }
                            selectedCounterParties = counterPartyList
                        },
                        label = {
                            Text(text = "No counter party")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = TablerIcons.Users,
                                contentDescription = TablerIcons.Users.name
                            )
                        }
                    )
                }
                counterParties.forEach { counterParty ->
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                    ) {
                        FilterChip(
                            selected = selectedCounterParties.contains(counterParty.uuid),
                            onClick = {
                                val counterPartyList = selectedCounterParties.toMutableList()
                                if (selectedCounterParties.contains(counterParty.uuid)) {
                                    if (selectedCounterParties.size > 1) {
                                        counterPartyList.remove(counterParty.uuid)
                                    }
                                } else {
                                    counterPartyList.add(counterParty.uuid)
                                }
                                selectedCounterParties = counterPartyList
                            },
                            label = {
                                Text(text = counterParty.name)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = counterParty.icon,
                                    contentDescription = counterParty.icon.name
                                )
                            }
                        )
                    }
                }
            }
            Text(
                text = "Methods",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                methods.forEach { method ->
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                    ) {
                        FilterChip(
                            selected = selectedMethods.contains(method.uuid),
                            onClick = {
                                val methodList = selectedMethods.toMutableList()
                                if (selectedMethods.contains(method.uuid)) {
                                    if (selectedMethods.size > 1) {
                                        methodList.remove(method.uuid)
                                    }
                                } else {
                                    methodList.add(method.uuid)
                                }
                                selectedMethods = methodList
                            },
                            label = {
                                Text(text = method.name)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = method.icon,
                                    contentDescription = method.icon.name
                                )
                            }
                        )
                    }
                }
            }
            Text(
                text = "Sources",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                sources.forEach { source ->
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                    ) {
                        FilterChip(
                            selected = selectedSources.contains(source.uuid),
                            onClick = {
                                val sourceList = selectedSources.toMutableList()
                                if (selectedSources.contains(source.uuid)) {
                                    if (selectedSources.size > 1) {
                                        sourceList.remove(source.uuid)
                                    }
                                } else {
                                    sourceList.add(source.uuid)
                                }
                                selectedSources = sourceList
                            },
                            label = {
                                Text(text = source.name)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = source.icon,
                                    contentDescription = source.icon.name
                                )
                            }
                        )
                    }
                }
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
                Text(text = "Cancel")
            }
            TextButton(
                onClick = {
                    setFilters(
                        filters.copy(
                            selectedCategories = selectedCategories,
                            selectedCounterParties = selectedCounterParties,
                            selectedMethods = selectedMethods,
                            selectedSources = selectedSources,
                            selectedTransactionTypes = selectedTransactionTypes,
                            filterAmountMin = fromAmount,
                            filterAmountMax = toAmount,
                        )
                    )
                    dismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Filter")
            }
        }
    }
}