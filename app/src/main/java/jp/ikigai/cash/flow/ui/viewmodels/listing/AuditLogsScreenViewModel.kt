/*
CashFlow - Expense Tracker
Copyright (C) 2025 ThomasGRG

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 3 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package jp.ikigai.cash.flow.ui.viewmodels.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import jp.ikigai.cash.flow.Account
import jp.ikigai.cash.flow.AuditLog
import jp.ikigai.cash.flow.CashFlowDatabase
import jp.ikigai.cash.flow.R
import jp.ikigai.cash.flow.data.Database
import jp.ikigai.cash.flow.data.adapters.ZonedDateTimeJSONAdapter
import jp.ikigai.cash.flow.data.dto.audit.AuditLogListItem
import jp.ikigai.cash.flow.data.dto.audit.TransactionAuditDTO
import jp.ikigai.cash.flow.data.enums.EntityType
import jp.ikigai.cash.flow.data.enums.LogAction
import jp.ikigai.cash.flow.data.enums.SortDirection
import jp.ikigai.cash.flow.ui.screenStates.common.SortConfigState
import jp.ikigai.cash.flow.ui.screenStates.listing.audit.AuditLogsScreenFiltersState
import jp.ikigai.cash.flow.ui.screenStates.listing.audit.AuditLogsScreenState
import jp.ikigai.cash.flow.utils.getCurrencyFormatterMap
import jp.ikigai.cash.flow.utils.getDateString
import jp.ikigai.cash.flow.utils.toEpochMilli
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalStdlibApi::class)
class AuditLogsScreenViewModel(
    private val database: CashFlowDatabase = Database.database
) : ViewModel() {

    private val datePattern = "dd-LLL-yyyy"

    private var currencyFormatterMap = getCurrencyFormatterMap()

    private val _state = MutableStateFlow(AuditLogsScreenState())
    val state: StateFlow<AuditLogsScreenState> = _state.asStateFlow()

    private val _filtersState = MutableStateFlow(AuditLogsScreenFiltersState())
    val filtersState: StateFlow<AuditLogsScreenFiltersState> = _filtersState.asStateFlow()

    private val _sortConfigState = MutableStateFlow(
        SortConfigState(sortField = "dateTime")
    )
    val sortConfigState: StateFlow<SortConfigState> = _sortConfigState.asStateFlow()

    init {
        val moshi = Moshi
            .Builder()
            .add(ZonedDateTimeJSONAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val transactionAdapter = moshi.adapter<TransactionAuditDTO>()
        val accountAdapter = moshi.adapter<Account>()
        getLogs(transactionAdapter, accountAdapter)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getLogs(
        transactionAdapter: JsonAdapter<TransactionAuditDTO>,
        accountAdapter: JsonAdapter<Account>
    ) = viewModelScope.launch {
        combine(
            _filtersState
                .onEach {
                    _state.update {
                        it.copy(loading = true)
                    }
                },
            _sortConfigState
                .onEach {
                    _state.update {
                        it.copy(loading = true)
                    }
                }
        ) { filters, sortConfig ->
            Pair(filters, sortConfig)
        }.flatMapLatest { (filters, sortConfig) ->
            database
                .auditLogQueries
                .getAll(
                    startDate = filters.startDate?.toEpochMilli() ?: 0L,
                    endDate = filters.endDate?.toEpochMilli() ?: Long.MAX_VALUE,
                    sortField = sortConfig.sortField,
                    sortDirection = sortConfig.sortDirection.name
                )
                .asFlow()
                .mapToList(Dispatchers.IO)
        }.collectLatest { logs ->
            val auditLogs = logs.groupBy(
                keySelector = { log ->
                    Instant
                        .ofEpochSecond(log.dateTime)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                },
                valueTransform = { log ->
                    getAuditLogDetails(log, transactionAdapter, accountAdapter)
                }
            )

            _state.update {
                it.copy(
                    auditLogs = auditLogs,
                    loading = false
                )
            }
        }
    }

    private fun getAuditLogDetails(
        auditLog: AuditLog,
        transactionAdapter: JsonAdapter<TransactionAuditDTO>,
        accountAdapter: JsonAdapter<Account>
    ): AuditLogListItem {
        val entityType = EntityType.valueOf(auditLog.entityType)
        val dateTime = Instant.ofEpochSecond(auditLog.dateTime).atZone(ZoneId.systemDefault())

        if (entityType == EntityType.TRANSACTION) {
            val before = if (auditLog.beforeJSON.isNotEmpty()) {
                transactionAdapter.fromJson(auditLog.beforeJSON)
            } else null

            val after = if (auditLog.afterJSON.isNotEmpty()) {
                transactionAdapter.fromJson(auditLog.afterJSON)
            } else null

            return AuditLogListItem.TransactionLog(
                logId = auditLog.logId,
                entityId = auditLog.entityId,
                entityType = EntityType.valueOf(auditLog.entityType),
                logAction = LogAction.valueOf(auditLog.logAction),
                dateTime = dateTime.format(DateTimeFormatter.ofPattern("dd LLL yyyy hh:mm a")),
                time = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                formattedBeforeAmount = if (before != null) {
                    val beforeAmountFormatter = currencyFormatterMap.getValue(
                        before.transactionCurrency
                    )
                    beforeAmountFormatter.format(before.transactionAmount).toString()
                } else "",
                before = before,
                formattedAfterAmount = if (after != null) {
                    val afterAmountFormatter = currencyFormatterMap.getValue(
                        after.transactionCurrency
                    )
                    afterAmountFormatter.format(after.transactionAmount).toString()
                } else "",
                after = after
            )
        } else {
            val before = accountAdapter.fromJson(auditLog.beforeJSON)!!
            val after = accountAdapter.fromJson(auditLog.afterJSON)!!

            val beforeBalanceFormatter = currencyFormatterMap.getValue(before.currency)
            val afterBalanceFormatter = currencyFormatterMap.getValue(after.currency)

            return AuditLogListItem.AccountLog(
                logId = auditLog.logId,
                entityId = auditLog.entityId,
                entityType = EntityType.valueOf(auditLog.entityType),
                logAction = LogAction.valueOf(auditLog.logAction),
                dateTime = dateTime.format(DateTimeFormatter.ofPattern("dd LLL yyyy hh:mm a")),
                time = dateTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
                formattedBeforeBalance = beforeBalanceFormatter.format(before.balance).toString(),
                before = before,
                formattedAfterBalance = afterBalanceFormatter.format(after.balance).toString(),
                after = after
            )
        }
    }

    fun setStartDateAndEndDate(startDate: ZonedDateTime?, endDate: ZonedDateTime?) {
        _filtersState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                startDateString = startDate?.getDateString(datePattern) ?: "",
                endDateString = endDate?.getDateString(datePattern) ?: "",
                dateRangeStringRes = if (startDate == null && endDate == null) {
                    R.string.all_time_date_range_label
                } else {
                    R.string.date_range_label
                }
            )
        }
    }

    fun setSortDirection(sortDirection: SortDirection) {
        _sortConfigState.update {
            it.copy(
                sortDirection = sortDirection
            )
        }
    }

    fun setLocale(locale: Locale?) {
        currencyFormatterMap = getCurrencyFormatterMap(locale)
        _state.update {
            it.copy(
                locale = locale
            )
        }
    }
}