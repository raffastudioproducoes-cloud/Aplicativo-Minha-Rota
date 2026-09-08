package com.raffastudioproducoes.minharota.domain.finance

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyGoalPolicyTest {
    private val today = LocalDate.of(2026, 8, 10) // Monday

    @Test
    fun `future bill is divided only by working days`() {
        val result = DailyGoalPolicy.amountFor(300.0, today, today.plusDays(3), setOf(3))
        assertEquals(150.0, result, 0.001)
    }

    @Test
    fun `all remaining days off produce no automatic goal`() {
        val result = DailyGoalPolicy.amountFor(300.0, today, today.plusDays(3), setOf(2, 3, 4, 5))
        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun `overdue bill is charged in full`() {
        val result = DailyGoalPolicy.amountFor(300.0, today, today.minusDays(1), emptySet())
        assertEquals(300.0, result, 0.001)
    }

    @Test
    fun `invalid amount is ignored`() {
        assertEquals(0.0, DailyGoalPolicy.amountFor(-1.0, today, today, emptySet()), 0.001)
        assertEquals(0.0, DailyGoalPolicy.amountFor(Double.NaN, today, today, emptySet()), 0.001)
    }
}
