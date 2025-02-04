package com.vibhu.moneyplanner


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ExpenseData(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // ISO 8601 format

    fun addExpense(expense: Expense) {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_ID, expense.expenseId.toString())
            put("expense_name", expense.expenseName)
            put("expense_amount", expense.expenseAmount)
            put("category_id", expense.categoryId.toString())
            put("expense_date", dateFormat.format(expense.expenseDate))
        }
        db.insert("expenses", null, values)
    }

    fun getExpensesByCategoryId(categoryId: UUID): MutableList<Expense> {
        val expenses = mutableListOf<Expense>()
        val cursor: Cursor = db.query(
            "expenses",
            null,
            "category_id = ?",
            arrayOf(categoryId.toString()),
            null, null, null
        )

        cursor.use {
            while (cursor.moveToNext()) {
                val expenseId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("id")))
                val expenseName = cursor.getString(cursor.getColumnIndexOrThrow("expense_name"))
                val expenseAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("expense_amount"))
                val expenseDateStr = cursor.getString(cursor.getColumnIndexOrThrow("expense_date"))
                val expenseDate = dateFormat.parse(expenseDateStr) ?: Date() // Handle parsing errors
                val expense = Expense(expenseId, expenseName, expenseAmount, categoryId, expenseDate)
                expenses.add(expense)
            }
        }
        return expenses
    }

    fun getExpensesInDateRange(startDate: Date, endDate: Date): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val cursor: Cursor = db.query(
            "expenses",
            null,
            "expense_date BETWEEN ? AND ?",
            arrayOf(dateFormat.format(startDate), dateFormat.format(endDate)),
            null, null, null
        )

        cursor.use {
            while (cursor.moveToNext()) {
                val expenseId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("expense_id")))
                val expenseName = cursor.getString(cursor.getColumnIndexOrThrow("expense_name"))
                val expenseAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("expense_amount"))
                val categoryId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("category_id")))
                val expenseDateStr = cursor.getString(cursor.getColumnIndexOrThrow("expense_date"))
                val expenseDate = dateFormat.parse(expenseDateStr) ?: Date()
                val expense = Expense(expenseId, expenseName, expenseAmount, categoryId, expenseDate)
                expenses.add(expense)
            }
        }
        return expenses
    }

    fun close() {
        db.close()
        dbHelper.close()
    }
}