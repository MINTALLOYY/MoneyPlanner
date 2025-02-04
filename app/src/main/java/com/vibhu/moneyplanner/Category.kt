package com.vibhu.moneyplanner


import java.util.UUID

data class Category(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val budget: Double
)