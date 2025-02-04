package com.vibhu.moneyplanner

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.util.UUID

class CategoryData(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    fun getAllCategories(): MutableList<Category> {
        val categories = mutableListOf<Category>()
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_NAME,
            null, null, null, null, null, null
        )

        cursor.use {
            while (cursor.moveToNext()) {
                val id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME))
                val budget = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET))
                val category = Category(id, name, budget)
                categories.add(category)
            }
        }
        return categories
    }

    fun insertCategory(category: Category) {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, category.id.toString())
            put(DatabaseHelper.COLUMN_NAME, category.name)
            put(DatabaseHelper.COLUMN_BUDGET, category.budget)
        }
        db.insert(DatabaseHelper.TABLE_NAME, null, values)
    }

    fun updateCategory(category: Category) {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, category.name)
            put(DatabaseHelper.COLUMN_BUDGET, category.budget)
        }
        val whereClause = "${DatabaseHelper.COLUMN_ID} = ?"
        val whereArgs = arrayOf(category.id.toString())
        db.update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs)
    }

    fun deleteCategory(categoryId: UUID) {
        val whereClause = "${DatabaseHelper.COLUMN_ID} = ?"
        val whereArgs = arrayOf(categoryId.toString())
        db.delete(DatabaseHelper.TABLE_NAME, whereClause, whereArgs)
    }


    fun getCategoryById(categoryId: UUID): Category? {
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_NAME,
            null,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(categoryId.toString()),
            null, null, null
        )

        cursor.use {
            if (cursor.moveToFirst()) {
                val id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME))
                val budget = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BUDGET))
                return Category(id, name, budget)
            }
        }
        return null
    }

    // Close the database when done.  It's crucial to manage database connections
    // to prevent leaks.  In a real app, you'd typically close the database in
    // the Activity's onDestroy() method or when it's no longer needed.
    fun close() {
        db.close()
        dbHelper.close()
    }
}