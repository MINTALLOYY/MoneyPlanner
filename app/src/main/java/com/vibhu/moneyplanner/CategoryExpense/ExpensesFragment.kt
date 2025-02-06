package com.vibhu.moneyplanner.CategoryExpense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.databinding.FragmentExpensesBinding // Updated binding class
import java.util.UUID

class ExpensesFragment: Fragment() { // Renamed class

    private var _binding: FragmentExpensesBinding? = null // Updated binding class
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryId: UUID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false) // Updated binding
        val view = binding.root

        expenseData = ExpenseData(requireContext())
        binding.recyclerViewExpenses.layoutManager = LinearLayoutManager(requireContext())

        arguments?.getString("categoryId")?.let {
            categoryId = UUID.fromString(it)

            expenseAdapter = ExpenseAdapter(
                expenseData.getExpensesByCategoryId(categoryId),
                requireContext()
            ) {
                expenseAdapter.updateItems(expenseData.getExpensesByCategoryId(categoryId))
            }

            binding.recyclerViewExpenses.adapter = expenseAdapter
        }

        binding.fabAddExpense.setOnClickListener {
            // Start the AddExpenseActivity
            val intent = Intent(requireContext(), AddExpenseActivity::class.java)
            intent.putExtra("categoryId", categoryId.toString()) // Pass the categoryId
            startActivity(intent)
        }

        return view

        return view
    }

    override fun onResume() {
        super.onResume()
        arguments?.getString("categoryId")?.let {
            categoryId = UUID.fromString(it)
            expenseAdapter.updateItems(expenseData.getExpensesByCategoryId(categoryId))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        expenseData.close()
    }
}