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

package jp.ikigai.cash.flow.utils

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun getMonthStartDate(): ZonedDateTime {
    val currentDateTime = ZonedDateTime.now(ZoneId.systemDefault())
    return ZonedDateTime.of(
        currentDateTime.year,
        currentDateTime.monthValue,
        1,
        0,
        0,
        0,
        0,
        ZoneId.of("UTC")
    )
}

fun getMonthEndDate(): ZonedDateTime {
    val currentDateTime = ZonedDateTime.now(ZoneId.systemDefault())
    return ZonedDateTime.of(
        currentDateTime.year,
        currentDateTime.monthValue,
        currentDateTime.month.maxLength(),
        23,
        59,
        59,
        999999999,
        ZoneId.of("UTC")
    )
}

fun ZonedDateTime.getStartOfDay(): ZonedDateTime {
    return ZonedDateTime.of(
        this.year,
        this.monthValue,
        this.dayOfMonth,
        0,
        0,
        0,
        0,
        ZoneId.of("UTC")
    )
}

fun ZonedDateTime.getEndOfDay(): ZonedDateTime {
    return ZonedDateTime.of(
        this.year,
        this.monthValue,
        this.dayOfMonth,
        23,
        59,
        59,
        999999999,
        ZoneId.of("UTC")
    )
}

fun YearMonth.toStartOfMonth(): ZonedDateTime {
    return ZonedDateTime.of(
        this.year,
        this.monthValue,
        1,
        0,
        0,
        0,
        0,
        ZoneId.of("UTC")
    )
}

fun YearMonth.toEndOfMonth(): ZonedDateTime {
    return ZonedDateTime.of(
        this.year,
        this.monthValue,
        this.lengthOfMonth(),
        23,
        59,
        59,
        999999999,
        ZoneId.of("UTC")
    )
}

fun ZonedDateTime.toEpochMilli(): Long {
    return this.toInstant().toEpochMilli()
}

fun Long.toUTCZonedDateTime(): ZonedDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.of("UTC"))
}

fun Long.toZonedDateTime(): ZonedDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())
}

fun ZonedDateTime.getDateString(pattern: String): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

fun ZonedDateTime.getTimeString(): String {
    return this.format(DateTimeFormatter.ofPattern("hh:mm a"))
}