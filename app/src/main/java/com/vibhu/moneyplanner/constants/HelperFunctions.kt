package com.vibhu.moneyplanner.constants

import java.math.BigDecimal
import java.math.RoundingMode

fun roundingTwoDecimals(double: Double): Double {
    return BigDecimal(double).setScale(2, RoundingMode.HALF_UP).toDouble()
}

fun doubleToMoneyString(double: Double): String {
    return "$${"%.2f".format(double)}"
}