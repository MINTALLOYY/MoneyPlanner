package com.vibhu.moneyplanner.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {

    companion object {
        const val DATABASE_VERSION = 16 // Increment for database changes
        const val DATABASE_NAME = "moneyplanner_db"
    }

    override fun onCreate(db: SQLiteDatabase) {

        // Create the initial balance table
        val CREATE_INITIAL_BALANCE_TABLE = "CREATE TABLE initial_balance " +
                "(user_id TEXT PRIMARY KEY, initial_amount REAL, initial_date TEXT) "
        db.execSQL(CREATE_INITIAL_BALANCE_TABLE)

        // Create the categories table
        val CREATE_CATEGORIES_TABLE = "CREATE TABLE categories " +
                "(id TEXT PRIMARY KEY, name TEXT)"
        db.execSQL(CREATE_CATEGORIES_TABLE)

        // Create the expenses table
        val CREATE_EXPENSES_TABLE = "CREATE TABLE expenses (" +
                "id TEXT PRIMARY KEY, expense_name TEXT, expense_amount REAL, " +
                "category_id TEXT, expense_date TEXT, " +
                "FOREIGN KEY (category_id) REFERENCES categories(id))"
        db.execSQL(CREATE_EXPENSES_TABLE)

        // Create the income category table
        val CREATE_INCOME_CATEGORY_TABLE = "CREATE TABLE income_categories (" +
                "income_category_id TEXT PRIMARY KEY, " +
                "income_category_name TEXT)"
        db.execSQL(CREATE_INCOME_CATEGORY_TABLE)

        // Create the income table
        val CREATE_INCOME_TABLE = "CREATE TABLE income (" +
                "income_id TEXT PRIMARY KEY, " +
                "amount REAL, " +
                "income_category_id TEXT, " + // Foreign key
                "received_date TEXT, " +
                "income_name TEXT, " +
                "FOREIGN KEY (income_category_id) REFERENCES income_categories(income_category_id))"
        db.execSQL(CREATE_INCOME_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades here (for production app)
        // For simplicity during development, you can drop the tables and recreate them:
        if (oldVersion < 16) { // Only upgrade if the database is older than version 2.
            db.execSQL("DROP TABLE IF EXISTS expenses")
            db.execSQL("DROP TABLE IF EXISTS categories")
            db.execSQL("DROP TABLE IF EXISTS income_categories")
            db.execSQL("DROP TABLE IF EXISTS income")
            db.execSQL("DROP TABLE IF EXISTS initial_balance")
            onCreate(db) // Recreate the tables
        }
    }
}