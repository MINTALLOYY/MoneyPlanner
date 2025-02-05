package com.vibhu.moneyplanner

import IncomeCategory
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.text.SimpleDateFormat
import java.util.*

class IncomeData(context: Context) {

    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Income Category functions:
    fun addIncomeCategory(incomeCategory: IncomeCategory) {
        val values = ContentValues().apply {
            put("income_category_id", incomeCategory.incomeCategoryId.toString())
            put("income_category_name", incomeCategory.incomeCategoryName)
        }
        db.insert("income_categories", null, values)
    }

    fun getAllIncomeCategories(): List<IncomeCategory> {
        val incomeCategories = mutableListOf<IncomeCategory>()
        val cursor: Cursor = db.query("income_categories", null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                incomeCategories.add(getIncomeCategoryFromCursor(it))
            }
        }
        return incomeCategories
    }

    private fun getIncomeCategoryFromCursor(cursor: Cursor): IncomeCategory {
        val id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("income_category_id")))
        val name = cursor.getString(cursor.getColumnIndexOrThrow("income_category_name"))
        return IncomeCategory(id, name)
    }

    fun getIncomeCategoryById(incomeCategoryId: UUID): IncomeCategory? {
        val cursor = db.query(
            "income_categories",
            null,
            "income_category_id = ?",
            arrayOf(incomeCategoryId.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return getIncomeCategoryFromCursor(it)
            }
        }
        return null
    }

    // Income functions:
    fun addIncome(income: Income) {
        val values = ContentValues().apply {
            put("income_id", income.incomeId.toString())
            put("source", income.source)
            put("amount", income.amount)
            put("income_category_id", income.incomeCategoryId.toString())
            put("received_date", dateFormat.format(income.receivedDate))
        }
        db.insert("income", null, values)
    }

    fun getAllIncomes(): List<Income> {
        val incomes = mutableListOf<Income>()
        val cursor: Cursor = db.query("income", null, null, null, null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                incomes.add(getIncomeFromCursor(it))
            }
        }
        return incomes
    }

    private fun getIncomeFromCursor(cursor: Cursor): Income {
        val id = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("income_id")))
        val source = cursor.getString(cursor.getColumnIndexOrThrow("source"))
        val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
        val categoryId = UUID.fromString(cursor.getString(cursor.getColumnIndexOrThrow("income_category_id")))
        val receivedDate = dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow("received_date"))) ?: Date()
        return Income(id, source, amount, categoryId, receivedDate)
    }

    fun getIncomesByCategoryId(incomeCategoryId: UUID): List<Income> {
        val incomes = mutableListOf<Income>()
        val cursor = db.query(
            "income",
            null,
            "income_category_id = ?",
            arrayOf(incomeCategoryId.toString()),
            null,
            null,
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                incomes.add(getIncomeFromCursor(it))
            }
        }
        return incomes
    }


    fun close() {
        dbHelper.close()
    }
}