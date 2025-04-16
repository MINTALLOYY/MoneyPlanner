package com.vibhu.moneyplanner

import java.util.Date
import java.util.UUID

data class Expense(
    val expenseId: UUID = UUID.randomUUID(),
    val name: String, 
    val amount: Double,
    val categoryId: UUID,
    val expenseDate: Date
)