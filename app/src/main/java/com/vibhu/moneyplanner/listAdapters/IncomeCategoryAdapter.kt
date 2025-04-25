package com.vibhu.moneyplanner.listAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vibhu.moneyplanner.database.IncomeData
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.models.IncomeCategory
import com.vibhu.moneyplanner.constants.roundingTwoDecimals
import java.text.NumberFormat
import java.util.Locale

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
        val earnedTextView: TextView = itemView.findViewById(R.id.textViewEarned)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeCategoryViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_income_category, parent, false)
        return IncomeCategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IncomeCategoryViewHolder, position: Int) {
        val incomeCategory = incomeCategories[position]

        holder.nameTextView.text = incomeCategory.incomeCategoryName

        val incomeData = IncomeData(context)
        holder.earnedTextView.text = NumberFormat.getCurrencyInstance(Locale.US).format(
            roundingTwoDecimals(
                incomeData.getTotalEarnedInSource(
                    incomeCategory.incomeCategoryId,
                    null
                )
            )
        )


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

    fun updateItems(
        newIncomeCategories: List<IncomeCategory>) {
        this.incomeCategories = newIncomeCategories
        notifyDataSetChanged()
    }
}