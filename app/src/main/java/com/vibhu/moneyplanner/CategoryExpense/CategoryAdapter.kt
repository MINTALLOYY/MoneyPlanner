package com.vibhu.moneyplanner.CategoryExpense

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.vibhu.moneyplanner.EditableAdapter
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.models.Category

class CategoryAdapter(
    categories: List<Category>,
    context: Context,
    onCategoryUpdated: () -> Unit,
    private val onItemClick: (Category) -> Unit // Callback for item click
) : EditableAdapter<Category>(categories, context, onCategoryUpdated) {

    override fun getLayoutId(): Int = R.layout.item_category

    override fun bindData(holder: EditableViewHolder, item: Category) {
        holder.itemView.findViewById<TextView>(R.id.textViewCategoryName).text = item.categoryName
        holder.itemView.findViewById<TextView>(R.id.textViewBudget).text = item.budget.toString()

        holder.itemView.setOnClickListener {
            onItemClick(item) // Call the callback
        }
    }

    override fun showEditDialog(item: Category, context: Context) {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_edit_category, null)
        builder.setView(dialogView)

        val editTextName = dialogView.findViewById<EditText>(R.id.editTextEditCategoryName)
        val editTextBudget = dialogView.findViewById<EditText>(R.id.editTextEditBudget)

        editTextName.setText(item.categoryName)
        editTextBudget.setText(item.budget.toString())

        builder.setPositiveButton("Update") { dialog, _ ->
            val newName = editTextName.text.toString()
            val newBudgetStr = editTextBudget.text.toString()

            if (newName.isNotBlank() && newBudgetStr.isNotBlank()) {
                try {
                    val newBudget = newBudgetStr.toDouble()
                    val updatedCategory = Category(item.categoryId, newName, newBudget)
                    CategoryData(context).updateCategory(updatedCategory) // Use updateCategory
                    onItemUpdated()
                } catch (e: NumberFormatException) {
                    // Handle invalid budget input
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun showDeleteConfirmationDialog(item: Category, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Category")
        builder.setMessage("Are you sure you want to delete this category?")

        builder.setPositiveButton("Delete") { dialog, _ ->
            CategoryData(context).deleteCategory(item.categoryId)
            onItemUpdated()
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}