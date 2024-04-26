package jp.ikigai.cash.flow.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun ZonedDateTime.toLocalMilli(): Long {
    return this.toInstant().toEpochMilli() + this.getOffSetInMilli()
}

fun ZonedDateTime.getOffSetInMilli(): Int {
    return this.offset.totalSeconds * 1000
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

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}

fun ZonedDateTime.getDateString(): String {
    return this.withZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE, dd-LLL-yyyy"))
}

fun LocalDate.getDateString(): String {
    return this.format(DateTimeFormatter.ofPattern("dd-LLL-yyyy"))
}

fun LocalDate.getStartOfDayInEpochMilli(): Long {
    return this.toEpochDay() * 86400000
}

fun LocalDate.getEndOfDayInEpochMilli(): Long {
    return ((this.toEpochDay() * 86400) - 1) * 1000
}

fun ZonedDateTime.getTimeString(): String {
    return this.withZoneSameInstant(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("hh:mm a"))
}