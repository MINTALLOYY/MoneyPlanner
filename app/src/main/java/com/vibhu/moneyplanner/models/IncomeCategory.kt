package com.vibhu.moneyplanner.models

import java.util.UUID

data class IncomeCategory(
    val incomeCategoryId: UUID = UUID.randomUUID(),
    val incomeCategoryName: String
)