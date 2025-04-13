package com.vibhu.moneyplanner.categoryexpense

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.vibhu.moneyplanner.DatabaseHelper
import com.vibhu.moneyplanner.IncomeCategoryData.Companion.COLUMN_INCOME_AMOUNT
import com.vibhu.moneyplanner.IncomeCategoryData.Companion.COLUMN_INCOME_CATEGORY_ID
import com.vibhu.moneyplanner.IncomeCategoryData.Companion.COLUMN_INCOME_CATEGORY_NAME
import com.vibhu.moneyplanner.IncomeCategoryData.Companion.COLUMN_INCOME_DATE
import com.vibhu.moneyplanner.IncomeCategoryData.Companion.TABLE_INCOMES
import com.vibhu.moneyplanner.IncomeCategoryData.Companion.TABLE_INCOME_CATEGORIES
import com.vibhu.moneyplanner.models.Category
import com.vibhu.moneyplanner.models.IncomeCategory
import java.util.Date
import java.util.UUID

class CategoryData(context: Context) {

    companion object { // Define constants here
        const val TABLE_CATEGORIES = "categories"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_BUDGET = "budget"

        const val TABLE_EXPENSES = "expenses"
        const val COLUMN_EXPENSE_ID = "id"
        const val COLUMN_EXPENSE_NAME = "expense_name"
        const val COLUMN_AMOUNT = "expense_amount"
        const val COLUMN_CATEGORY_ID = "category_id"
        const val COLUMN_EXPENSE_DATE = "expense_date"
    }

    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    fun addCategory(category: Category) {
        val values = ContentValues().apply {
            put(COLUMN_ID, category.categoryId.toString())
            put(COLUMN_NAME, category.categoryName)
            put(COLUMN_BUDGET, category.budget)
        }
        db.insert(TABLE_CATEGORIES, null, values)
    }

    fun getCategoryById(categoryId: UUID): Category? {
        return getAllCategories().find { it.categoryId == categoryId }
    }

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                categories.add(getCategoryFromCursor(it))
            }
        }
        return categories
    }

    private fun getCategoryFromCursor(cursor: Cursor): Category {
        val id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
        val budget = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BUDGET))
        return Category(id, name, budget)
    }

    fun updateCategory(category: Category) {
        val values = ContentValues().apply {
            put(COLUMN_NAME, category.categoryName)
            put(COLUMN_BUDGET, category.budget)
        }
        val whereClause = "$COLUMN_ID =?"
        val whereArgs = arrayOf(category.categoryId.toString())
        db.update(TABLE_CATEGORIES, values, whereClause, whereArgs)
    }

    fun deleteCategory(categoryId: UUID) {
        val whereClause = "$COLUMN_ID =?"
        val whereArgs = arrayOf(categoryId.toString())
        db.delete(TABLE_CATEGORIES, whereClause, whereArgs)
    }

    fun close() {
        dbHelper.close()
    }

    fun getAmountSpentInCategory(categoryId: UUID): Double {
        val cursor = db.query(
            "expenses", // Table name
            arrayOf("SUM(amount)"), // Columns to return
            "category_id = ?", // Where clause
            arrayOf(categoryId.toString()), // Where clause arguments
            null, // groupBy
            null, // having
            null // orderBy
        )
        var totalAmount = 0.0
        cursor.use {
            if (it.moveToFirst()) {
                totalAmount = it.getDouble(0)
            }
        }
        return totalAmount
    }



    fun getBiggestExpenseCategory(startDate: Date? = null, endDate: Date? = null): Category? {

        // Construct the SQL query with optional date range filtering with protection against SQL injections
        val sql = """
        SELECT 
            c.$COLUMN_ID, 
            c.$COLUMN_NAME,
            c.$COLUMN_BUDGET,
            SUM(e.$COLUMN_AMOUNT) AS total_amount 
        FROM $TABLE_CATEGORIES c
        LEFT JOIN $TABLE_EXPENSES e
          ON c.$COLUMN_ID = e.$COLUMN_CATEGORY_ID
            ${if (startDate != null && endDate != null)
            "AND e.$COLUMN_EXPENSE_DATE BETWEEN ? AND ?" else ""}
        GROUP BY c.$COLUMN_ID, c.$COLUMN_NAME, c.$COLUMN_BUDGET
        HAVING total_amount IS NOT NULL
        ORDER BY total_amount DESC
        LIMIT 1
    """.trimIndent()

        return db.rawQuery(sql, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val categoryId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)))
                val categoryName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val budget = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BUDGET))
                Category(categoryId, categoryName, budget)
            } else null  // Return null if no category is found
        }

    }
}