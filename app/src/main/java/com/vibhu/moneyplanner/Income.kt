package com.vibhu.moneyplanner

import java.util.Date
import java.util.UUID

data class Income(
    val incomeId: UUID = UUID.randomUUID(),
    val amount: Double,
    val incomeCategoryId: UUID,
    val receivedDate: Date
)