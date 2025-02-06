package com.vibhu.moneyplanner.categoryexpense

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityEditCategoryBinding
import com.vibhu.moneyplanner.models.Category
import java.util.UUID

class EditCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCategoryBinding
    private lateinit var categoryData: CategoryData
    private lateinit var categoryId: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryData = CategoryData(this)

        // Get the category ID from the intent
        val categoryIdString = intent.getStringExtra("category_id")
        if (categoryIdString != null) {
            categoryId = UUID.fromString(categoryIdString)

            // Load the category data for editing
            val category = categoryData.getAllCategories().find { it.categoryId == categoryId }
            if (category != null) {
                binding.editTextCategoryName.setText(category.categoryName)
                binding.editTextBudget.setText(category.budget.toString())
            } else {
                Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show()
                finish() // Close the activity if the category isn't found
            }
        } else {
            Toast.makeText(this, "Category ID missing", Toast.LENGTH_SHORT).show()
            finish() // Close if the ID is missing
        }

        binding.buttonSaveCategory.setOnClickListener {
            val newName = binding.editTextCategoryName.text.toString()
            val newBudgetStr = binding.editTextBudget.text.toString()

            if (newName.isNotBlank() && newBudgetStr.isNotBlank()) {
                try {
                    val newBudget = newBudgetStr.toDouble()
                    val updatedCategory = Category(categoryId, newName, newBudget) // Use existing ID

                    categoryData.updateCategory(updatedCategory)
                    Toast.makeText(this, "Category updated", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid budget", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        categoryData.close()
    }
}