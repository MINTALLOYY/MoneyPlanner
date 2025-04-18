package com.vibhu.moneyplanner.models

import java.util.UUID

data class Category(
    val categoryId: UUID = UUID.randomUUID(),
    val categoryName: String,
)