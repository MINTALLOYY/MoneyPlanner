package com.vibhu.moneyplanner

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        const val DATABASE_VERSION = 4 // Increment for database changes
        const val DATABASE_NAME = "moneyplanner_db"
        const val TABLE_NAME = "categories"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_BUDGET = "budget"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create the categories table
        val CREATE_CATEGORIES_TABLE = "CREATE TABLE categories " +
                "(id TEXT PRIMARY KEY, name TEXT, budget REAL)"
        db.execSQL(CREATE_CATEGORIES_TABLE)

        // Create the expenses table
        val CREATE_EXPENSES_TABLE = "CREATE TABLE expenses (" +
                "id TEXT PRIMARY KEY, expense_name TEXT, expense_amount REAL, " +
                "category_id TEXT, expense_date TEXT, " +
                "FOREIGN KEY (category_id) REFERENCES categories(id))"
        db.execSQL(CREATE_EXPENSES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades here (for production app)
        // For simplicity during development, you can drop the tables and recreate them:
        if (oldVersion < 4) { // Only upgrade if the database is older than version 2.
            db.execSQL("DROP TABLE IF EXISTS expenses")
            db.execSQL("DROP TABLE IF EXISTS categories")
            onCreate(db) // Recreate the tables
        }
    }
}