package jp.ikigai.cash.flow.ui.screens.common.restore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import jp.ikigai.cash.flow.CounterPartyWithTransactionMetadata
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.TempCounterParty
import jp.ikigai.cash.flow.ui.components.cards.MapCounterPartyCard

@Composable
fun ImportScreenMapCounterPartiesContent(
    dbCounterParties: List<CounterPartyWithTransactionMetadata>,
    tempCounterParties: List<TempCounterParty>,
    counterPartyMappings: Map<Long, CounterPartyWithTransactionMetadata>,
    selectedTempCounterParties: Set<Long>,
    conflictingTempCounterParties: Set<Long>,
    selectCounterParty: (Long) -> Unit,
    toggleSelected: (Long) -> Unit,
    setCounterPartyMapping: (Long, CounterPartyWithTransactionMetadata) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                if (dbCounterParties.isEmpty()) {
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
                            text = stringResource(id = R.string.no_counter_parties_available_for_mapping_label)
                        )
                    }
                }
            }
            items(
                items = tempCounterParties,
                key = { tempCounterParty -> tempCounterParty.tempCounterPartyId }
            ) { tempCounterParty ->
                MapCounterPartyCard(
                    tempCounterParty = tempCounterParty,
                    mappedCounterParty = counterPartyMappings[
                        tempCounterParty.tempCounterPartyId
                    ] ?: CounterPartyWithTransactionMetadata(
                        counterPartyId = 0L,
                        counterPartyName = "",
                        transactionCount = 0L,
                        lastUsed = null
                    ),
                    modifier = Modifier.animateItem(),
                    canSelect = dbCounterParties.isNotEmpty(),
                    selected = selectedTempCounterParties.contains(
                        tempCounterParty.tempCounterPartyId
                    ),
                    conflicting = conflictingTempCounterParties.contains(
                        tempCounterParty.tempCounterPartyId
                    ),
                    selectCounterParty = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectCounterParty(tempCounterParty.tempCounterPartyId)
                    },
                    toggleSelected = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        toggleSelected(tempCounterParty.tempCounterPartyId)
                    },
                    clearSelectedCounterParty = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        setCounterPartyMapping(
                            tempCounterParty.tempCounterPartyId,
                            CounterPartyWithTransactionMetadata(
                                counterPartyId = 0L,
                                counterPartyName = "",
                                transactionCount = 0L,
                                lastUsed = null
                            )
                        )
                    }
                )
            }
        }
        if (tempCounterParties.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_counter_parties_to_import_placeholder_label),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}