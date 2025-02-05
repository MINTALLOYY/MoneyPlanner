package com.vibhu.moneyplanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class IncomeAdapter(private var incomes: List<Income>) :
    RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    class IncomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewAmount: TextView = itemView.findViewById(R.id.textViewIncomeAmount)
        val textViewCategory: TextView = itemView.findViewById(R.id.textViewIncomeCategory)
        val textViewReceivedDate: TextView = itemView.findViewById(R.id.textViewIncomeReceivedDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_income, parent, false)
        return IncomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        val income = incomes[position]
        holder.textViewAmount.text = income.amount.toString()

        val category = IncomeData(holder.itemView.context).getIncomeCategoryById(income.incomeCategoryId)
        holder.textViewCategory.text = category?.incomeCategoryName ?: "Unknown Category"

        holder.textViewReceivedDate.text = dateFormat.format(income.receivedDate)
    }

    override fun getItemCount(): Int = incomes.size

    fun updateIncomes(newIncomes: List<Income>) {
        this.incomes = newIncomes
        notifyDataSetChanged()
    }
}