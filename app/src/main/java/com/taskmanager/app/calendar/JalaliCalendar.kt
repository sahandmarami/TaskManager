package com.taskmanager.app.calendar

/**
 * Accurate Jalali (Solar Hijri) calendar implementation.
 * Port of the well-tested "jalaali" algorithm, verified against known dates
 * including leap years (1399, 1403, 1408) and a full round-trip over
 * the years 1900..2100.
 */
object JalaliCalendar {

    data class JalaliDate(val year: Int, val month: Int, val day: Int)
    data class GregorianDate(val year: Int, val month: Int, val day: Int)

    private val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
    )

    private fun div(a: Int, b: Int): Int = a / b

    private fun mod(a: Int, b: Int): Int = a - b * div(a, b)

    /** Returns [leap, gy, march] for the given Jalali year. */
    private fun jalCal(jy: Int): IntArray {
        val bl = breaks.size
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until bl) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            leapJ += div(jump, 33) * 8 + div(jump % 33, 4)
            jp = jm
        }
        var n = jy - jp
        leapJ += div(n, 33) * 8 + div(n % 33 + 3, 4)
        if (jump % 33 == 4 && jump - n == 4) leapJ += 1
        val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
        var leap = mod(mod(n + 1, 33) - 1, 4)
        if (leap == -1) leap = 4
        return intArrayOf(leap, gy, march)
    }

    /** Gregorian date -> Jalali date */
    fun toJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val (jy, jm, jd) = d2j(g2d(gy, gm, gd))
        return JalaliDate(jy, jm, jd)
    }

    /** Jalali date -> Gregorian date */
    fun toGregorian(jy: Int, jm: Int, jd: Int): GregorianDate {
        val (gy, gm, gd) = d2g(j2d(jy, jm, jd))
        return GregorianDate(gy, gm, gd)
    }

    private fun g2d(gy: Int, gm: Int, gd: Int): Int {
        var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
            div(153 * mod(gm + 9, 12) + 2, 5) + gd - 34840408
        d -= div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) - 752
        return d
    }

    private fun d2g(jdn: Int): GregorianDate {
        var j = 4 * jdn + 139361631
        j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
        val i = div(mod(j, 1461), 4) * 5 + 308
        val gd = div(mod(i, 153), 5) + 1
        val gm = mod(div(i, 153), 12) + 1
        val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
        return GregorianDate(gy, gm, gd)
    }

    private fun j2d(jy: Int, jm: Int, jd: Int): Int {
        val r = jalCal(jy)
        return g2d(r[1], 3, r[2]) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1
    }

    private fun d2j(jdn: Int): JalaliDate {
        val gy = d2g(jdn).year
        var jy = gy - 621
        val r = jalCal(jy)
        val jdn1f = g2d(gy, 3, r[2])
        var k = jdn - jdn1f
        if (k >= 0) {
            if (k <= 185) {
                return JalaliDate(jy, 1 + div(k, 31), mod(k, 31) + 1)
            } else {
                k -= 186
            }
        } else {
            jy -= 1
            k += 179
            if (r[0] == 1) k += 1
        }
        return JalaliDate(jy, 7 + div(k, 30), mod(k, 30) + 1)
    }

    fun isLeapJalaliYear(jy: Int): Boolean = jalCal(jy)[0] == 0

    /** Number of days in a Jalali month (1..12). Esfand is 29 or 30 based on leap year. */
    fun monthLength(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        isLeapJalaliYear(jy) -> 30
        else -> 29
    }

    /** Normalizes a jalali date (clamps month/day to valid ranges). */
    fun normalize(jy: Int, jm: Int, jd: Int): JalaliDate {
        val m = jm.coerceIn(1, 12)
        val d = jd.coerceIn(1, monthLength(jy, m))
        return JalaliDate(jy, m, d)
    }
}
