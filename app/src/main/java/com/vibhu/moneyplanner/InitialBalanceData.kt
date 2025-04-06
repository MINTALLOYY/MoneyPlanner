package com.vibhu.moneyplanner

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.vibhu.moneyplanner.models.InitialBalance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InitialBalanceData(context: Context) {
    companion object{
        const val INITIAL_BALANCE_TABLE = "initial_balance"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_AMOUNT = "initial_amount"
        const val COLUMN_DATE = "initial_date"
    }

    private val dbHelper = DatabaseHelper(context)
    private val db: SQLiteDatabase = dbHelper.writableDatabase
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun addInitialBalanceData(initialBalance: InitialBalance){
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, initialBalance.userId.toString())
            put(COLUMN_AMOUNT, initialBalance.initialAmount)
            put(COLUMN_DATE, dateFormat.format(initialBalance.initialDate))
        }
        db.insert(INITIAL_BALANCE_TABLE, null, values)
    }

    fun fetchInitialBalance(userId: UUID): Double?{
        val selection = "$COLUMN_USER_ID = ?"
        val selectionArgs = arrayOf(userId.toString())
        val cursor = db.query(
            INITIAL_BALANCE_TABLE,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return it.getDouble(it.getColumnIndexOrThrow(COLUMN_AMOUNT))
            }
        }
        return null
    }

    fun fetchInitialDate(userId: UUID): Date?{
        val selection = "$COLUMN_USER_ID = ?"
        val selecionArgs = arrayOf(userId.toString())
        val cursor = db.query(
            INITIAL_BALANCE_TABLE,
            null,
            selection,
            selecionArgs,
            null,
            null,
            null
        )
        cursor.use{
            if(it.moveToFirst()){
                return dateFormat.parse(it.getString(it.getColumnIndexOrThrow(COLUMN_DATE)))
            }
        }
        return null
    }

    fun clearAllInitialBalanceData(){
        db.delete(INITIAL_BALANCE_TABLE, null, null)
    }

    fun close() {
        db.close()

    }


}