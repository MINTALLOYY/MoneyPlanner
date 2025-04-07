package com.vibhu.moneyplanner

import android.content.Context
import android.icu.text.NumberFormat
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vibhu.moneyplanner.models.Income
import java.util.Locale

class IncomeAdapter(
    private var incomes: List<Income>,
    private val context: Context,
    private val onItemEditClick: (Income) -> Unit,
    private val onItemDeleteClick: (Income) -> Unit
) : RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountTextView: TextView = itemView.findViewById(R.id.textViewIncomeAmount)
        val dateTextView: TextView = itemView.findViewById(R.id.textViewIncomeDate)
        val nameTextView: TextView = itemView.findViewById(R.id.textViewIncomeName)
        val editButton: ImageButton = itemView.findViewById(R.id.buttonEditIncome)
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDeleteIncome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_income, parent, false)
        return IncomeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val income = incomes[position]

        val incomeName = income.incomeLogName
        holder.nameTextView.text = incomeName

        val formattedIncomeAmount = NumberFormat.getCurrencyInstance(Locale.US).format(income.amount)
        holder.amountTextView.text = formattedIncomeAmount

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(income.receivedDate)
        holder.dateTextView.text = formattedDate

        holder.editButton.setOnClickListener {
            onItemEditClick(income)
        }

        holder.deleteButton.setOnClickListener {
            onItemDeleteClick(income)
        }
    }

    override fun getItemCount(): Int {
        return incomes.size
    }

    fun updateItems(newIncomes: List<Income>) {
        this.incomes = newIncomes
        notifyDataSetChanged()
    }
}