package com.vibhu.moneyplanner

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.databinding.ActivityCategoryExpensesBinding
import java.util.UUID

class CategoryExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryExpensesBinding
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var expenseData: ExpenseData
    private lateinit var categoryId: UUID

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoryExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseData = ExpenseData(this)

        val categoryIdString = intent.getStringExtra(EXTRA_CATEGORY_ID)
        if (categoryIdString != null) {
            categoryId = UUID.fromString(categoryIdString)

            val categoryData = CategoryData(this)
            val category = categoryData.getCategoryById(categoryId)

            if (category != null) {
                binding.textViewCategoryName.text = category.name
            } else {
                Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show()
                finish() // Important: Finish the activity if the category isn't found
                return // Exit onCreate early to prevent further errors
            }
            categoryData.close()

            binding.recyclerViewExpenses.layoutManager = LinearLayoutManager(this)
            expenseAdapter = ExpenseAdapter(expenseData.getExpensesByCategoryId(categoryId))
            binding.recyclerViewExpenses.adapter = expenseAdapter

            binding.fabAddExpense.setOnClickListener {
                val intent = Intent(this, AddExpenseActivity::class.java)
                intent.putExtra(AddExpenseActivity.EXTRA_CATEGORY_ID, categoryId.toString())
                startActivity(intent)
            }
        } else {
            Toast.makeText(this, "Error: Category ID not found", Toast.LENGTH_SHORT).show()
            finish() // Finish if no category ID is passed
            return // Prevent the rest of onCreate from running
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable the back button
    }

    override fun onResume() {
        super.onResume()
        if (::categoryId.isInitialized) {
            expenseAdapter.updateExpenses(expenseData.getExpensesByCategoryId(categoryId))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        expenseData.close()
    }
}