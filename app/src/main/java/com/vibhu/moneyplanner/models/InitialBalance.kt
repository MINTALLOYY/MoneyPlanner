package com.vibhu.moneyplanner.models

import java.util.Date
import java.util.UUID

data class InitialBalance(
    val initialAmount: Double,
    val initialDate: Date,
    val userId: UUID = UUID.randomUUID()
)
