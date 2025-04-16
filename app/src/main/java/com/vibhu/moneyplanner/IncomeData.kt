package com.vibhu.moneyplanner

import com.vibhu.moneyplanner.models.IncomeCategory
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.vibhu.moneyplanner.IncomeCategoryData.Companion.COLUMN_INCOME_CATEGORY_ID
import com.vibhu.moneyplanner.IncomeCategoryData.Companion.TABLE_INCOME_CATEGORIES
import com.vibhu.moneyplanner.categoryexpense.ExpenseData.Companion.COLUMN_EXPENSE_DATE
import com.vibhu.moneyplanner.models.Income
import java.text.SimpleDateFormat
import java.util.*

class IncomeData(context: Context) {

    companion object {
        const val TABLE_INCOMES = "income"
        const val COLUMN_INCOME_ID = "income_id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_INCOME_DATE = "received_date"
        const val COLUMN_INCOME_CATEGORY_ID = "income_category_id"
        const val COLUMN_INCOME_NAME = "income_name"
    }

    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


    private fun getIncomeCategoryFromCursor(cursor: Cursor): IncomeCategory {
        val id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("income_category_id")))
        val name = cursor.getString(cursor.getColumnIndexOrThrow("income_category_name"))
        return IncomeCategory(id, name)
    }

    fun getIncomesInDateRange(startDate: Date, endDate: Date): List<Income> {
        val incomes = mutableListOf<Income>()
        val selection = "${COLUMN_INCOME_DATE} BETWEEN ? AND ?"
        val selectionArgs = arrayOf(startDate.time.toString(), endDate.time.toString())
        val cursor = db.query(
            TABLE_INCOMES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                incomes.add(getIncomeFromCursor(it))
            }
        }
        return incomes
    }


    fun deleteIncome(incomeId: UUID) {
        val selection = "$COLUMN_INCOME_ID =?"
        val selectionArgs = arrayOf(incomeId.toString())
        db.delete(TABLE_INCOMES, selection, selectionArgs)
    }

    // Income functions:
    fun addIncome(income: Income) {
        val values = ContentValues().apply {
            put("income_id", income.incomeId.toString())
            put("amount", income.amount)
            put("income_category_id", income.incomeCategoryId.toString())
            put("received_date", income.receivedDate.time)
            put("income_name", income.incomeLogName)
        }
        db.insert("income", null, values)
    }

    fun updateIncome(income: Income) {
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, income.amount)
            put(COLUMN_INCOME_DATE, income.receivedDate.time)
            put(COLUMN_INCOME_CATEGORY_ID, income.incomeCategoryId.toString())
        }

        val selection = "${COLUMN_INCOME_ID} = ?"
        val selectionArgs = arrayOf(income.incomeId.toString())

        db.update(TABLE_INCOMES, values, selection, selectionArgs)
    }

    fun getAllIncomes(): List<Income> {
        val incomes = mutableListOf<Income>()
        val cursor: Cursor = db.query(TABLE_INCOMES, null, null, null, null, null, COLUMN_INCOME_DATE + " DESC")
        cursor.use {
            while (it.moveToNext()) {
                incomes.add(getIncomeFromCursor(it))
            }
        }
        return incomes
    }

    private fun getIncomeFromCursor(cursor: Cursor): Income {
        val id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INCOME_ID)))
        val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
        val categoryId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(
            COLUMN_INCOME_CATEGORY_ID)))
        val receivedDate = Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_INCOME_DATE)))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INCOME_NAME))
        return Income(id, amount, categoryId, receivedDate, name)
    }

    fun getBiggestIncomeSourceOutOfSources(incomeCategories: List<IncomeCategory>, dateFromToday: Date? = null): IncomeCategory{
        var listOfEarningsPerCategory = mutableListOf<Double>()
        for(incomeCategory in incomeCategories){
            if(dateFromToday == null) listOfEarningsPerCategory.add(getIncomesByCategoryId(incomeCategory.incomeCategoryId).sumOf{ it.amount })
            else listOfEarningsPerCategory.add(getIncomesByCategoryId(incomeCategory.incomeCategoryId).filter{ it.receivedDate >= dateFromToday }.sumOf{ it.amount })
        }
        return incomeCategories[listOfEarningsPerCategory.indexOf(listOfEarningsPerCategory.max())]
    }

    fun getTotalEarnedInSource(incomeCategoryId: UUID, dateFromToday: Date? = null): Double{
        if(dateFromToday == null) return getIncomesByCategoryId(incomeCategoryId).sumOf{ it.amount }
        return getIncomesByCategoryId(incomeCategoryId).filter{ it.receivedDate >= dateFromToday }.sumOf{ it.amount }
    }

    fun getIncomesByCategoryId(incomeCategoryId: UUID): List<Income> {
        val incomes = mutableListOf<Income>()
        val cursor = db.query(
            TABLE_INCOMES,
            null,
            "income_category_id = ?",
            arrayOf(incomeCategoryId.toString()),
            null,
            null,
            COLUMN_INCOME_DATE + " DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                incomes.add(getIncomeFromCursor(it))
            }
        }
        return incomes
    }

    fun getIncomeById(incomeId: UUID): Income? {
        val selection = "${IncomeData.COLUMN_INCOME_ID} = ?"
        val selectionArgs = arrayOf(incomeId.toString())

        val cursor = db.query(
            IncomeData.TABLE_INCOMES,
            null, // Select all columns
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return getIncomeFromCursor(it) // Helper function to create Income object from Cursor
            }
        }
        return null // Return null if no income is found
    }

    fun getTotalIncomeAmount(dateFromToday: Date? = null): Double{
        val incomes = getAllIncomes()
        if(dateFromToday == null) return incomes.sumOf{ it.amount }
        return incomes.filter{ it.receivedDate >= dateFromToday }.sumOf{ it.amount}
    }

    fun getSizeOfIncomesInCategory(incomeCategoryId: UUID): Int {
        return getIncomesByCategoryId(incomeCategoryId).size
    }

    fun close() {
        dbHelper.close()
    }
}