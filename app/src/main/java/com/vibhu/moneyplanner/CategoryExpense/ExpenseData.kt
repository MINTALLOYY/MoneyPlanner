package com.vibhu.moneyplanner.CategoryExpense

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.vibhu.moneyplanner.DatabaseHelper
import com.vibhu.moneyplanner.Expense
import java.util.Date
import java.util.UUID

class ExpenseData(context: Context) {

    companion object {  // Define constants here
        const val TABLE_EXPENSES = "expenses"
        const val COLUMN_EXPENSE_ID = "expense_id"
        const val COLUMN_EXPENSE_NAME = "expense_name"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_CATEGORY_ID = "category_id"
        const val COLUMN_EXPENSE_DATE = "expense_date"
    }

    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase

    fun addExpense(expense: Expense) {
        val values = ContentValues().apply {
            put(COLUMN_EXPENSE_ID, expense.expenseId.toString())
            put(COLUMN_EXPENSE_NAME, expense.name)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_CATEGORY_ID, expense.categoryId.toString())
            put(COLUMN_EXPENSE_DATE, expense.expenseDate.time)
        }
        db.insert(TABLE_EXPENSES, null, values)
    }

    fun getExpensesByCategoryId(categoryId: UUID): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val selection = "$COLUMN_CATEGORY_ID =?"
        val selectionArgs = arrayOf(categoryId.toString())
        val cursor = db.query(TABLE_EXPENSES, null, selection, selectionArgs, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                expenses.add(getExpenseFromCursor(it))
            }
        }
        return expenses
    }

    private fun getExpenseFromCursor(cursor: Cursor): Expense {
        val id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_NAME))
        val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT))
        val categoryId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)))
        val date = Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DATE)))
        return Expense(id, name, amount, categoryId, date)
    }

    fun updateExpense(expense: Expense) {
        val values = ContentValues().apply {
            put(COLUMN_EXPENSE_NAME, expense.name)
            put(COLUMN_AMOUNT, expense.amount)
            put(COLUMN_CATEGORY_ID, expense.categoryId.toString())
            put(COLUMN_EXPENSE_DATE, expense.expenseDate.time)
        }
        val whereClause = "$COLUMN_EXPENSE_ID =?"
        val whereArgs = arrayOf(expense.expenseId.toString())
        db.update(TABLE_EXPENSES, values, whereClause, whereArgs)
    }

    fun deleteExpense(expenseId: UUID) {
        val whereClause = "$COLUMN_EXPENSE_ID =?"
        val whereArgs = arrayOf(expenseId.toString())
        db.delete(TABLE_EXPENSES, whereClause, whereArgs)
    }

    fun close() {
        dbHelper.close()
    }
}