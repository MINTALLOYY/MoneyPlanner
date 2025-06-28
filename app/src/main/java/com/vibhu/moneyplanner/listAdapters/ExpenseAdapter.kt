package com.vibhu.moneyplanner.listAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vibhu.moneyplanner.Expense
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.constants.roundingTwoDecimals
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val context: Context,
    private val onItemEditClick: (Expense) -> Unit,
    private val onItemDeleteClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewExpenseName)
        val amountTextView: TextView = itemView.findViewById(R.id.textViewExpenseAmount)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewExpenseDate)
        val editButton: ImageButton = itemView.findViewById(R.id.buttonEditExpense)
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDeleteExpense)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.nameTextView.text = expense.name
        val formattedAmount = NumberFormat.getCurrencyInstance(Locale.US).format(
            roundingTwoDecimals(
                expense.amount
            )
        )
        holder.amountTextView.text = formattedAmount

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(expense.expenseDate)
        holder.dateTextView.text = formattedDate

        holder.editButton.setOnClickListener {
            onItemEditClick(expense)
        }

        holder.deleteButton.setOnClickListener {
            onItemDeleteClick(expense)
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateItems(newExpenses: List<Expense>) {
        this.expenses = newExpenses
        notifyDataSetChanged()
    }
}