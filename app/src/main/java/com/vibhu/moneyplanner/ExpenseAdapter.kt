package com.vibhu.moneyplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseAdapter(private var expenses: List<Expense>):
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    class ExpenseViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textViewExpenseName: TextView = itemView.findViewById(R.id.textViewExpenseName)
        val textViewExpenseAmount: TextView = itemView.findViewById(R.id.textViewExpenseAmount)
        val textViewExpenseDate: TextView = itemView.findViewById(R.id.textViewExpenseDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.textViewExpenseName.text = expense.expenseName
        holder.textViewExpenseAmount.text = expense.expenseAmount.toString()
        holder.textViewExpenseDate.text = dateFormat.format(expense.expenseDate)
    }

    override fun getItemCount(): Int = expenses.size

    fun updateExpenses(newExpenses: List<Expense>) {
        this.expenses = newExpenses
        notifyDataSetChanged()
    }
}