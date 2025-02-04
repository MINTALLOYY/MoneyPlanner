package com.vibhu.moneyplanner

import java.util.Date
import java.util.UUID

data class Expense(
    val expenseId: UUID = UUID.randomUUID(),
    val expenseName: String,
    val expenseAmount: Double,
    val categoryId: UUID, // UUID of the associated category
    val expenseDate: Date // Date of the expense
)