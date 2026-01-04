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

package jp.ikigai.cash.flow.data.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ZonedDateTimeJSONAdapter {

    @ToJson
    fun toJson(zonedDateTime: ZonedDateTime): Long {
        return zonedDateTime.toInstant().toEpochMilli()
    }

    @FromJson
    fun fromJson(epochMilli: Long): ZonedDateTime {
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault())
    }
}