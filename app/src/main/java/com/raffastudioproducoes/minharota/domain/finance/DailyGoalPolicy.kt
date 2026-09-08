package com.raffastudioproducoes.minharota.domain.finance

import java.time.DayOfWeek
import java.time.LocalDate

/** Pure rule used to spread a pending bill over available working days. */
object DailyGoalPolicy {
    fun amountFor(
        amount: Double,
        today: LocalDate,
        dueDate: LocalDate,
        fixedDaysOff: Set<Int>
    ): Double {
        if (!amount.isFinite() || amount <= 0.0) return 0.0
        val calendarDays = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)
        if (calendarDays <= 0L) return amount

        val workingDays = (1..calendarDays).count { offset ->
            val date = today.plusDays(offset)
            !fixedDaysOff.contains(dayIndex(date.dayOfWeek))
        }
        return if (workingDays == 0) 0.0 else amount / workingDays
    }

    private fun dayIndex(day: DayOfWeek): Int = when (day) {
        DayOfWeek.SUNDAY -> 1
        else -> day.value + 1
    }
}
