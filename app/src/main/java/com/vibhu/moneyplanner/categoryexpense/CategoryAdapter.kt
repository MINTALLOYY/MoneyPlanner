package com.vibhu.moneyplanner.categoryexpense

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.models.Category
import java.text.NumberFormat
import java.util.Locale
import com.vibhu.moneyplanner.roundingTwoDecimals

class CategoryAdapter(
    private var categories: List<Category>,
    private val context: Context,
    private val onItemClick: (Category) -> Unit,
    private val onItemEditClick: (Category) -> Unit, // Callback for edit
    private val onItemDeleteClick: (Category) -> Unit // Callback for delete
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.textViewCategoryName)
        val spentTextView: TextView = itemView.findViewById(R.id.textViewSpent)
        val editButton: ImageButton = itemView.findViewById(R.id.buttonEditCategory) // Correct ID
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDeleteCategory) // Correct ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false) 
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {  // Implement this method
        val category = categories[position]

        holder.nameTextView.text = category.categoryName

        val expenseData = ExpenseData(context)
        val formattedSpent = NumberFormat.getCurrencyInstance(Locale.US).format(roundingTwoDecimals(expenseData.getTotalSpentInCategory(category.categoryId, null)))
        holder.spentTextView.text = formattedSpent

        holder.itemView.setOnClickListener {  // Click listener for the entire item
            onItemClick(category)
        }

        holder.editButton.setOnClickListener {
            onItemEditClick(category) // Call the edit callback
        }

        holder.deleteButton.setOnClickListener {
            onItemDeleteClick(category) // Call the delete callback
        }

    }

    override fun getItemCount(): Int = categories.size

    fun updateItems(newCategories: List<Category>) {
        this.categories = newCategories
        notifyDataSetChanged()
    }
}