package com.vibhu.moneyplanner // Replace with your package name

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.vibhu.moneyplanner.models.IncomeCategory
import java.util.UUID

class IncomeCategoryData(context: Context) {
    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    companion object {  // Use a companion object
        const val TABLE_INCOME_CATEGORIES = "income_categories"
        const val COLUMN_INCOME_CATEGORY_ID = "income_category_id"
        const val COLUMN_INCOME_CATEGORY_NAME = "income_category_name"
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
        val selection = "${COLUMN_INCOME_CATEGORY_ID} =?"
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