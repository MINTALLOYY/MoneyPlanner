package com.vibhu.moneyplanner // Replace with your package name

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.vibhu.moneyplanner.models.IncomeCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class IncomeCategoryData(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    companion object {  // Use a companion object
        const val TABLE_INCOME_CATEGORIES = "income_categories"
        const val COLUMN_INCOME_CATEGORY_ID = "income_category_id"
        const val COLUMN_INCOME_CATEGORY_NAME = "income_category_name"


        const val TABLE_INCOMES = "income"
        const val COLUMN_INCOME_AMOUNT = "amount"
        const val COLUMN_INCOME_DATE = "received_date"

    }

    fun addIncomeCategory(category: IncomeCategory) {
        val values = ContentValues().apply {
            put(COLUMN_INCOME_CATEGORY_ID, category.incomeCategoryId.toString())
            put(COLUMN_INCOME_CATEGORY_NAME, category.incomeCategoryName)
        }
        db.insert(TABLE_INCOME_CATEGORIES, null, values)
    }

    fun getAllIncomeCategories(): List<IncomeCategory> {
        val incomeCategories = mutableListOf<IncomeCategory>()
        val cursor = db.query(
            TABLE_INCOME_CATEGORIES,
            null,
            null,
            null,
            null,
            null,
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val categoryId = UUID.fromString(it.getString(it.getColumnIndexOrThrow(COLUMN_INCOME_CATEGORY_ID)))
                val categoryName = it.getString(it.getColumnIndexOrThrow(COLUMN_INCOME_CATEGORY_NAME))
                incomeCategories.add(IncomeCategory(categoryId, categoryName))
            }
        }
        return incomeCategories
    }

    fun getIncomeCategoryById(categoryId: UUID): IncomeCategory? {
        val selection = "$COLUMN_INCOME_CATEGORY_ID = ?"
        val selectionArgs = arrayOf(categoryId.toString())
        val cursor = db.query(
            TABLE_INCOME_CATEGORIES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val categoryName = it.getString(it.getColumnIndexOrThrow(COLUMN_INCOME_CATEGORY_NAME))
                return IncomeCategory(categoryId, categoryName)
            }
        }
        return null
    }

    fun getBiggestIncomeCategory(startDate: Date? = null, endDate: Date? = null): IncomeCategory? {
        // Construct the SQL query with optional date range filtering
        val sql = """
        SELECT 
            ic.${COLUMN_INCOME_CATEGORY_ID}, 
            ic.${COLUMN_INCOME_CATEGORY_NAME}, 
            SUM(i.${COLUMN_INCOME_AMOUNT}) AS total_amount 
        FROM ${TABLE_INCOME_CATEGORIES} ic 
        LEFT JOIN ${TABLE_INCOMES} i 
            ON ic.${COLUMN_INCOME_CATEGORY_ID} = i.${COLUMN_INCOME_CATEGORY_ID}
            ${if (startDate != null && endDate != null)
            "WHERE i.${COLUMN_INCOME_DATE} BETWEEN ? AND ?" else ""}
        GROUP BY ic.${COLUMN_INCOME_CATEGORY_ID}, ic.${COLUMN_INCOME_CATEGORY_NAME}
        HAVING total_amount IS NOT NULL
        ORDER BY total_amount DESC
        LIMIT 1
    """.trimIndent()

        // Prepare query arguments for date range
        val selectionArgs = if (startDate != null && endDate != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            arrayOf(sdf.format(startDate), sdf.format(endDate))
        } else {
            null
        }

        return db.rawQuery(sql, selectionArgs).use { cursor ->
            if (cursor.moveToFirst()) {
                val categoryId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INCOME_CATEGORY_ID)))
                val categoryName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INCOME_CATEGORY_NAME))
                IncomeCategory(categoryId, categoryName)
            } else {
                null
            }
        }
    }

    fun getIncomeCategoryByName(categoryName: String): IncomeCategory? {
        val selection = "$COLUMN_INCOME_CATEGORY_NAME = ?"
        val selectionArgs = arrayOf(categoryName)
        val cursor = db.query(
            TABLE_INCOME_CATEGORIES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                val id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INCOME_CATEGORY_ID)))
                val name = categoryName
                return IncomeCategory(id, name)
            }
        }
        return null
    }

    fun updateIncomeCategory(category: IncomeCategory) {
        val values = ContentValues().apply {
            put(COLUMN_INCOME_CATEGORY_NAME, category.incomeCategoryName)
        }
        val selection = "${COLUMN_INCOME_CATEGORY_ID} =?"
        val selectionArgs = arrayOf(category.incomeCategoryId.toString())
        db.update(TABLE_INCOME_CATEGORIES, values, selection, selectionArgs)
    }

    fun deleteIncomeCategory(categoryId: UUID) {
        val selection = "${COLUMN_INCOME_CATEGORY_ID} =?"
        val selectionArgs = arrayOf(categoryId.toString())
        db.delete(TABLE_INCOME_CATEGORIES, selection, selectionArgs)
    }

    fun close() {
        dbHelper.close()
    }
}