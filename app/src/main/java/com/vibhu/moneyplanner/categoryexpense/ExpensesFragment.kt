package com.vibhu.moneyplanner.categoryexpense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.databinding.FragmentExpensesBinding
import java.util.UUID

class ExpensesFragment: Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryId: UUID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expenseData = ExpenseData(requireContext())

        // Get the category ID from the arguments
        val categoryIdString = arguments?.getString("categoryId")
        if (categoryIdString!= null) {
            categoryId = UUID.fromString(categoryIdString)
        } else {
            // Handle the case where categoryId is missing
            // For example, you might show an error message and go back to the previous fragment
            // or choose a default category
        }

        binding.recyclerViewExpenses.layoutManager = LinearLayoutManager(requireContext())

        expenseAdapter = ExpenseAdapter(
            expenseData.getExpensesByCategoryId(categoryId),
            requireContext(),
            { expense -> // onItemEditClick
                val intent = Intent(requireContext(), EditExpenseActivity::class.java)
                intent.putExtra("expense_id", expense.expenseId.toString())
                startActivity(intent)
            },
            { expense -> // onItemDeleteClick
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete") { _, _ ->
                        expenseData.deleteExpense(expense.expenseId)
                        // Update the list after deleting
                        expenseAdapter.updateItems(expenseData.getExpensesByCategoryId(categoryId))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.recyclerViewExpenses.adapter = expenseAdapter

        binding.fabAddExpense.setOnClickListener {
            val intent = Intent(requireContext(), AddExpenseActivity::class.java)
            intent.putExtra("category_id", categoryId.toString())
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        expenseAdapter.updateItems(expenseData.getExpensesByCategoryId(categoryId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        expenseData.close()
    }
}