package com.vibhu.moneyplanner

import android.content.Context
import com.vibhu.moneyplanner.categoryexpense.ExpenseData
import com.vibhu.moneyplanner.models.Transaction
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID


class TransactionData(context: Context) {

    private val incomeData = IncomeData(context)
    private val expenseData = ExpenseData(context)

    fun getAllTransaction(): List<Transaction> {
        val transactions: MutableList<Transaction> = mutableListOf()
        transactions.addAll(incomeData.getAllIncomes().map { Transaction(it.amount, it.receivedDate, true, it.incomeLogName, it.incomeId, it.incomeCategoryId) })
        transactions.addAll(expenseData.getAllExpenses().map { Transaction(it.amount, it.expenseDate, false, it.name, it.expenseId, it.categoryId) })
        transactions.sortByDescending { it.date }
        return transactions
    }

    fun getTransactionByID(id: UUID): Transaction? {
        val transactions: MutableList<Transaction> = mutableListOf()
        transactions.addAll(incomeData.getAllIncomes().map { Transaction(it.amount, it.receivedDate, true, it.incomeLogName, it.incomeId, it.incomeCategoryId) })
        transactions.addAll(expenseData.getAllExpenses().map { Transaction(it.amount, it.expenseDate, false, it.name, it.expenseId, it.categoryId) })

        for(transaction in transactions){
            if(transaction.id == id){
                return transaction
            }
        }

        return null

    }
}