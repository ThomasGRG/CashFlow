package jp.ikigai.cash.flow.ui.screens.common.restore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.InfoCircle
import jp.ikigai.cash.flow.MethodWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TempMethod
import jp.ikigai.cash.flow.ui.components.cards.MapMethodCard

@Composable
fun ImportScreenMapMethodsContent(
    dbMethods: List<MethodWithTransactionMetadata>,
    tempMethods: List<TempMethod>,
    methodMappings: Map<Long, MethodWithTransactionMetadata>,
    selectedTempMethods: Set<Long>,
    conflictingTempMethods: Set<Long>,
    selectMethod: (Long) -> Unit,
    toggleSelected: (Long) -> Unit,
    setMethodMapping: (Long, MethodWithTransactionMetadata) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        stickyHeader(
            key = "info",
            contentType = "info_header",
        ) {
            if (dbMethods.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 5.dp, bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = TablerIcons.InfoCircle,
                        contentDescription = "info icon",
                    )
                    Text(
                        text = stringResource(id = R.string.no_methods_available_for_mapping_label)
                    )
                }
            }
        }
        items(
            items = tempMethods,
            key = { tempMethod -> tempMethod.tempMethodId }
        ) { tempMethod ->
            MapMethodCard(
                tempMethod = tempMethod,
                mappedMethod = methodMappings[
                    tempMethod.tempMethodId
                ] ?: MethodWithTransactionMetadata(
                    methodId = 0L,
                    methodName = "",
                    transactionCount = 0L,
                    lastUsed = null
                ),
                modifier = Modifier.animateItem(),
                canSelect = dbMethods.isNotEmpty(),
                selected = selectedTempMethods.contains(
                    tempMethod.tempMethodId
                ),
                conflicting = conflictingTempMethods.contains(
                    tempMethod.tempMethodId
                ),
                selectMethod = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectMethod(tempMethod.tempMethodId)
                },
                toggleSelected = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    toggleSelected(tempMethod.tempMethodId)
                },
                clearSelectedMethod = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    setMethodMapping(
                        tempMethod.tempMethodId,
                        MethodWithTransactionMetadata(
                            methodId = 0L,
                            methodName = "",
                            transactionCount = 0L,
                            lastUsed = null
                        )
                    )
                }
            )
        }
    }
}