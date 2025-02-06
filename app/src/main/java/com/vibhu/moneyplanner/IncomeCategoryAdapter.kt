package com.vibhu.moneyplanner

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.vibhu.moneyplanner.models.IncomeCategory

class IncomeCategoryAdapter(
    private var incomeCategories: List<IncomeCategory>,
    private val context: Context,
    private val onItemEditClick: (IncomeCategory) -> Unit,
    private val onItemDeleteClick: (IncomeCategory) -> Unit,
    private val onItemClick: (IncomeCategory) -> Unit // For item clicks
) : RecyclerView.Adapter<IncomeCategoryAdapter.IncomeCategoryViewHolder>() {

    class IncomeCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewIncomeCategoryName)
        val editButton: ImageButton = itemView.findViewById(R.id.buttonEditIncomeCategory)
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDeleteIncomeCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeCategoryViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_income_category, parent, false)
        return IncomeCategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IncomeCategoryViewHolder, position: Int) {
        val incomeCategory = incomeCategories[position]

        holder.nameTextView.text = incomeCategory.incomeCategoryName

        holder.itemView.setOnClickListener {
            onItemClick(incomeCategory) // Item click
        }

        holder.editButton.setOnClickListener {
            onItemEditClick(incomeCategory)
        }

        holder.deleteButton.setOnClickListener {
            onItemDeleteClick(incomeCategory)
        }
    }

    override fun getItemCount(): Int {
        return incomeCategories.size
    }

    fun updateItems(newIncomeCategories: List<IncomeCategory>) {
        this.incomeCategories = newIncomeCategories
        notifyDataSetChanged()
    }
}