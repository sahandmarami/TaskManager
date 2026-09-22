package com.taskmanager.app.calendar

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.Instant

/** Persian (Farsi) digit conversion + Jalali date/time formatting helpers. */

fun Int.toPersianDigits(): String = this.toString().map { c ->
    if (c in '0'..'9') ('۰' + (c - '0')) else c
}.joinToString("")

fun Long.toPersianDigits(): String = this.toInt().toPersianDigits()

fun String.toPersianDigits(): String = this.map { c ->
    if (c in '0'..'9') ('۰' + (c - '0')) else c
}.joinToString("")

object PersianCalendar {

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    // Week starts on Saturday (شنبه) in the Iranian calendar
    val weekdayNames = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
    )

    val shortWeekdayNames = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    /** Index in Persian week (0=Saturday .. 6=Friday) for a java.time DayOfWeek. */
    fun persianWeekIndex(d: DayOfWeek): Int = (d.value + 1) % 7

    fun weekdayShortName(d: DayOfWeek): String = shortWeekdayNames[persianWeekIndex(d)]

    fun weekdayName(date: LocalDate): String = weekdayNames[persianWeekIndex(date.dayOfWeek)]

    /** Today as JalaliDate using the device timezone. */
    fun todayJalali(): JalaliCalendar.JalaliDate {
        val today = LocalDate.now()
        return JalaliCalendar.toJalali(today.year, today.monthValue, today.dayOfMonth)
    }

    /** JalaliDate -> LocalDate (gregorian equivalent day). */
    fun toLocalDate(j: JalaliCalendar.JalaliDate): LocalDate {
        val g = JalaliCalendar.toGregorian(j.year, j.month, j.day)
        return LocalDate.of(g.year, g.month, g.day)
    }

    /** LocalDate -> JalaliDate */
    fun fromLocalDate(d: LocalDate): JalaliCalendar.JalaliDate =
        JalaliCalendar.toJalali(d.year, d.monthValue, d.dayOfMonth)

    /** "۲۵ مهر ۱۴۰۵" */
    fun formatJalali(j: JalaliCalendar.JalaliDate): String =
        "${j.day.toPersianDigits()} ${monthNames[j.month - 1]} ${j.year.toPersianDigits()}"

    /** "دوشنبه ۳۱ شهریور ۱۴۰۵" */
    fun formatJalaliWithWeekday(j: JalaliCalendar.JalaliDate): String =
        "${weekdayName(toLocalDate(j))} ${formatJalali(j)}"

    /** "مهر ۱۴۰۵" */
    fun formatMonthYear(jy: Int, jm: Int): String =
        "${monthNames[jm - 1]} ${jy.toPersianDigits()}"

    /** "۱۸:۳۰" from hour/minute */
    fun formatTime(hour: Int, minute: Int): String =
        "%02d:%02d".format(hour, minute).toPersianDigits()

    fun formatTimeFromMillis(millis: Long): String {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
        return formatTime(dt.hour, dt.minute)
    }

    /** Start of the given jalali day in epoch millis. */
    fun startOfDayMillis(j: JalaliCalendar.JalaliDate): Long =
        toLocalDate(j).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun endOfDayMillis(j: JalaliCalendar.JalaliDate): Long =
        toLocalDate(j).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

    /** epoch millis -> JalaliDate */
    fun fromMillis(millis: Long): JalaliCalendar.JalaliDate {
        val d = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
        return JalaliCalendar.toJalali(d.year, d.monthValue, d.dayOfMonth)
    }
}
