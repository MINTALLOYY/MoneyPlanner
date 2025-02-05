package com.vibhu.moneyplanner

import IncomeCategory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IncomeCategoryAdapter(
    private var incomeCategories: List<IncomeCategory>,
    private val onItemClick: (IncomeCategory) -> Unit // Callback function
) :
    RecyclerView.Adapter<IncomeCategoryAdapter.IncomeCategoryViewHolder>() {

    class IncomeCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewIncomeCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeCategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_income_category, parent, false)
        return IncomeCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncomeCategoryViewHolder, position: Int) {
        val incomeCategory = incomeCategories[position]
        holder.textViewName.text = incomeCategory.incomeCategoryName

        holder.textViewName.setOnClickListener {
            onItemClick(incomeCategory) // Call the callback
        }
    }

    override fun getItemCount(): Int = incomeCategories.size

    fun updateIncomeCategories(newIncomeCategories: List<IncomeCategory>) {
        this.incomeCategories = newIncomeCategories
        notifyDataSetChanged()
    }
}