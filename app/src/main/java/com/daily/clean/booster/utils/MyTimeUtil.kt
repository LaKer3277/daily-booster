package com.daily.clean.booster.utils

import java.util.*

object MyTimeUtil {

    fun calculateDays(oldDate: Long, newDate: Long): Int {
        val c1 = Calendar.getInstance()
        val c2 = Calendar.getInstance()

        val minL = Math.min(oldDate, newDate)
        val maxL = Math.max(oldDate, newDate)

        c1.time = Date(minL)
        c2.time = Date(maxL)
        val year1 = c1.get(Calendar.YEAR)
        val year2 = c2.get(Calendar.YEAR)
        if (year2 == year1) {
            //如果没跨年
            return c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR)
        }
        if (year2 > year1) {
            //如果跨年了
            val day1 = c1.get(Calendar.DAY_OF_YEAR)
            val day2 = c2.get(Calendar.DAY_OF_YEAR)
            var days = 0
            for (i in year1..year2) {
                days += if (i == year1) {
                    if (GregorianCalendar().isLeapYear(year1)) {
                        366 - day1
                    } else {
                        365 - day1
                    }
                } else if (i == year2) {
                    day2
                } else {
                    if (GregorianCalendar().isLeapYear(year1)) {
                        366
                    } else {
                        365
                    }
                }
            }
            return days
        }
        return -1
    }
}