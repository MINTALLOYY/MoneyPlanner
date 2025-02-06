package com.vibhu.moneyplanner.CategoryExpense

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityAddCategoryBinding
import com.vibhu.moneyplanner.models.Category

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var categoryData: CategoryData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryData = CategoryData(this)

        binding.buttonCancel.setOnClickListener {
            finish() // Close the activity
        }

        binding.buttonSaveCategory.setOnClickListener {
            val categoryName = binding.editTextCategoryName.text.toString()
            val budgetStr = binding.editTextCategoryBudget.text.toString()

            if (categoryName.isNotBlank() && budgetStr.isNotBlank()) {
                try {
                    val budget = budgetStr.toDouble()
                    val newCategory = Category(categoryName = categoryName, budget = budget)
                    categoryData.addCategory(newCategory)

                    Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid budget", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error adding category: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}