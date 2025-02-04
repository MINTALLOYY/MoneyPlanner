package com.vibhu.moneyplanner


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityAddCategoryBinding
import java.lang.NumberFormatException

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var categoryData: CategoryData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryData = CategoryData(this) // Initialize CategoryData

        binding.buttonSaveCategory.setOnClickListener {
            val name = binding.editTextCategoryName.text.toString()
            val budgetStr = binding.editTextCategoryBudget.text.toString()

            if (name.isNotEmpty() && budgetStr.isNotEmpty()) {
                try {
                    val budget = budgetStr.toDouble()
                    val newCategory = Category(name = name, budget = budget)
                    categoryData.insertCategory(newCategory)
                    finish() // Go back to MainActivity
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid budget input", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonCancel.setOnClickListener {
            finish() // Call finish() to go back
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        categoryData.close() // Close the database
    }
}