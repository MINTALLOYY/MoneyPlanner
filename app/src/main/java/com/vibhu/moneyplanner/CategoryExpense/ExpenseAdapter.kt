package com.vibhu.moneyplanner.CategoryExpense

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vibhu.moneyplanner.Expense
import com.vibhu.moneyplanner.R
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val context: Context,
    private val onExpenseUpdated: () -> Unit // Lambda for updates
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewExpenseName)
        val amountTextView: TextView = itemView.findViewById(R.id.textViewExpenseAmount)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewExpenseDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.nameTextView.text = expense.name
        holder.amountTextView.text = expense.amount.toString()

        // Format the date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(expense.expenseDate)
        holder.dateTextView.text = formattedDate

        // Add item click listener if needed
        holder.itemView.setOnClickListener {
            // Handle item click here if required
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateItems(newExpenses: List<Expense>) {
        this.expenses = newExpenses
        notifyDataSetChanged()
    }
}