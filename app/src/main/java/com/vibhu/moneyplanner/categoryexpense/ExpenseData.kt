package com.vibhu.moneyplanner.categoryexpense

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.vibhu.moneyplanner.DatabaseHelper
import com.vibhu.moneyplanner.Expense
import com.vibhu.moneyplanner.models.Category
import java.util.Date
import java.util.UUID

class ExpenseData(context: Context) {

    companion object {  // Define constants here
        const val TABLE_EXPENSES = "expenses"
        const val COLUMN_EXPENSE_ID = "id"
        const val COLUMN_EXPENSE_NAME = "expense_name"
        const val COLUMN_AMOUNT = "expense_amount"
        const val COLUMN_CATEGORY_ID = "category_id"
        const val COLUMN_EXPENSE_DATE = "expense_date"
    }

    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase
    private val categoryData: com.vibhu.moneyplanner.categoryexpense.CategoryData = com.vibhu.moneyplanner.categoryexpense.CategoryData(context)

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

    fun getExpenseById(expenseId: UUID): Expense? {
        val selection = "${COLUMN_EXPENSE_ID} = ?"
        val selectionArgs = arrayOf(expenseId.toString())
        val cursor = db.query(
            TABLE_EXPENSES,
            null, // null selects all columns
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) { // Check if a row was returned
                return getExpenseFromCursor(it) // Use your existing function
            }
        }
        return null // Return null if no matching expense is found
    }

    fun getExpensesByCategoryId(categoryId: UUID): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val selection = "$COLUMN_CATEGORY_ID =?"
        val selectionArgs = arrayOf(categoryId.toString())
        val cursor = db.query(TABLE_EXPENSES, null, selection, selectionArgs, null, null, COLUMN_EXPENSE_DATE + " DESC")
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

    fun getAllExpenses(): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val cursor: Cursor = db.query(TABLE_EXPENSES, null, null, null, null, null, COLUMN_EXPENSE_DATE + " DESC")
        cursor.use {
            while (it.moveToNext()) {
                expenses.add(getExpenseFromCursor(it))
            }
        }
        return expenses
    }

    fun getTotalExpenseAmount(dateFromToday: Date? = null): Double {
        val expenses = getAllExpenses()
        if (dateFromToday == null) return expenses.sumOf { it.amount }
        return expenses.filter{ it.expenseDate >= dateFromToday }.sumOf{ it.amount}
    }

    fun getTotalSpentInCategory(categoryId: UUID, dateFromToday: Date? = null): Double {
        val expenses = getExpensesByCategoryId(categoryId)
        if (dateFromToday == null) return expenses.sumOf { it.amount }
        return expenses.filter { it.expenseDate >= dateFromToday }.sumOf { it.amount }
    }

    fun getBiggestCategoryOutOfCategories(categories: List<Category>, dateFromToday: Date? = null): Category {
        var listOfExpensesPerCategory = mutableListOf<Double>()
        for (category in categories) {
            if (dateFromToday == null) listOfExpensesPerCategory.add(getTotalSpentInCategory(category.categoryId))
            else listOfExpensesPerCategory.add(getTotalSpentInCategory(category.categoryId, dateFromToday))
        }
        return categories[listOfExpensesPerCategory.indexOf(listOfExpensesPerCategory.max())]
    }

    fun getExpensesInDateRange(startDate: Date, endDate: Date): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val selection = "${COLUMN_EXPENSE_DATE} BETWEEN ? AND ?"
        val selectionArgs = arrayOf(startDate.time.toString(), endDate.time.toString())
        val cursor = db.query(
            TABLE_EXPENSES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                expenses.add(getExpenseFromCursor(it))
            }
        }
        return expenses
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