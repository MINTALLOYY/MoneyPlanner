package com.vibhu.moneyplanner.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.vibhu.moneyplanner.database.DatabaseHelper
import com.vibhu.moneyplanner.models.Category
import java.util.UUID

class CategoryData(context: Context) {

    companion object { // Define constants here
        const val TABLE_CATEGORIES = "categories"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
    }

    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    fun addCategory(category: Category) {
        val values = ContentValues().apply {
            put(COLUMN_ID, category.categoryId.toString())
            put(COLUMN_NAME, category.categoryName)
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
        return Category(id, name)
    }

    fun updateCategory(category: Category) {
        val values = ContentValues().apply {
            put(COLUMN_NAME, category.categoryName)
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

}