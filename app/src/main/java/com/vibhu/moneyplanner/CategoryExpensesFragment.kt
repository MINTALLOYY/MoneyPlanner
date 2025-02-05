package com.vibhu.moneyplanner // Your package name

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.databinding.FragmentCategoryExpensesBinding // Your binding class
import java.util.UUID

class CategoryExpensesFragment : Fragment() {

    private var _binding: FragmentCategoryExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var expenseData: ExpenseData
    private lateinit var categoryId: UUID

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCategoryExpensesBinding.inflate(inflater, container, false)
        val view = binding.root

        expenseData = ExpenseData(requireContext())

        val categoryIdString = arguments?.getString(EXTRA_CATEGORY_ID) // Get from arguments
        if (categoryIdString != null) {
            categoryId = UUID.fromString(categoryIdString)

            val categoryData = CategoryData(requireContext())
            val category = categoryData.getCategoryById(categoryId)

            if (category != null) {
                binding.textViewCategoryName.text = category.name
            } else {
                Toast.makeText(requireContext(), "Category not found", Toast.LENGTH_SHORT).show()
                // Important: If the category isn't found, you might want to navigate back or handle it differently
            }
            categoryData.close()

            binding.recyclerViewExpenses.layoutManager = LinearLayoutManager(requireContext())
            expenseAdapter = ExpenseAdapter(expenseData.getExpensesByCategoryId(categoryId))
            binding.recyclerViewExpenses.adapter = expenseAdapter

            binding.fabAddExpense.setOnClickListener {
                val intent = Intent(requireContext(), AddExpenseActivity::class.java)
                intent.putExtra(AddExpenseActivity.EXTRA_CATEGORY_ID, categoryId.toString())
                startActivity(intent)
            }
        } else {
            Toast.makeText(requireContext(), "Error: Category ID not found", Toast.LENGTH_SHORT).show()
            // Handle the error appropriately, maybe by navigating back
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (::categoryId.isInitialized) {
            expenseAdapter.updateExpenses(expenseData.getExpensesByCategoryId(categoryId))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        expenseData.close()
    }
}