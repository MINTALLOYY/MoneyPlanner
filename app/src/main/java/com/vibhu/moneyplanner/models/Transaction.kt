package com.vibhu.moneyplanner.models

import java.util.Date
import java.util.UUID

data class Transaction(
    val amount: Double,
    val date: Date,
    val isIncome: Boolean,
    val transactionName: String,
    val id: UUID
)
