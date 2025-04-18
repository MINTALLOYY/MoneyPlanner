package com.vibhu.moneyplanner.categoryexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.databinding.ActivityEditExpenseBinding
import com.vibhu.moneyplanner.roundingTwoDecimals
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.exp

class EditExpenseFragment: Fragment() {
    private lateinit var _binding: ActivityEditExpenseBinding
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData
    private lateinit var expenseId: UUID
    private lateinit var categoryId: UUID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = ActivityEditExpenseBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expenseData = ExpenseData(requireContext())

        val expenseIdString = arguments?.getString("expense_id")
        val categoryIdString = arguments?.getString("category_id")
        if (expenseIdString != null && categoryIdString != null) {
            expenseId = UUID.fromString(expenseIdString)
            categoryId = UUID.fromString(categoryIdString)

            // Load expense data
            val expense = expenseData.getExpenseById(expenseId)
            if (expense!= null) {
                binding.editTextExpenseName.setText(expense.name)
                binding.editTextExpenseAmount.setText(roundingTwoDecimals(expense.amount).toString())

                // Set date picker to the date of when the expense was recorded
                val calendar = Calendar.getInstance()
                calendar.time = expense.expenseDate

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                binding.editTextExpenseDate.updateDate(year,month,day)

            } else {
                // Handle expense not found
                goBackToExpensesPage("Expense not Found")
            }
        } else {
            // Handle missing expense ID
            goBackToExpensesPage("Expense ID is Missing")
        }

        binding.buttonSaveExpense.setOnClickListener {
            val newName = binding.editTextExpenseName.text.toString()
            val newAmountStr = binding.editTextExpenseAmount.text.toString()
            val newDateInfo = binding.editTextExpenseDate

            if (newName.isBlank() || newAmountStr.isBlank() || newDateInfo.isEmpty()) { // Use isBlank() for better check
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }
            if (newName.length > 16){
                Toast.makeText(requireContext(), "No more than 16 characters in the name fields", Toast.LENGTH_SHORT)
            }

            try {
                val newAmount = roundingTwoDecimals(newAmountStr.toDouble())

                val calendar = Calendar.getInstance()
                calendar.set(newDateInfo.year, newDateInfo.month, newDateInfo.dayOfMonth)
                val newDate = calendar.time

                // *** FETCH THE EXPENSE OBJECT HERE ***
                val expense = expenseData.getExpenseById(expenseId) // Implement this function in ExpenseData

                if (expense != null) { // Check if the expense was found
                    val updatedExpense = expense.copy(name = newName, amount = newAmount, expenseDate = newDate)
                    expenseData.updateExpense(updatedExpense)
                    goBackToExpensesPage("Expense Updated")
                } else {
                    Toast.makeText(requireContext(), "Expense not found!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonCancel.setOnClickListener {
            goBackToExpensesPage()
        }
    }

    fun goBackToExpensesPage(message: String? = null){
        val bundle = Bundle()
        bundle.putString("categoryId", categoryId.toString()) // Pass categoryId
        bundle.putString("message", message) //Pass message

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val expensesFragment = ExpensesFragment()
        expensesFragment.arguments = bundle // Set the bundle with categoryId

        expenseData.close()

        fragmentTransaction.replace(R.id.fragment_container, expensesFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        expenseData.close()
    }
}