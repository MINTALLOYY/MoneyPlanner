package com.vibhu.moneyplanner.categoryexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.Expense
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.databinding.FragmentAddCategoryBinding
import com.vibhu.moneyplanner.databinding.FragmentAddExpenseBinding
import java.util.Calendar
import java.util.UUID
import kotlin.math.exp

class AddExpenseFragment: Fragment() {

    private lateinit var _binding: FragmentAddExpenseBinding
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData
    private lateinit var categoryId: UUID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expenseData = ExpenseData(requireContext())
        binding.buttonCancel.setOnClickListener {
            goBackToExpensesPage()
        }

        // Get the categoryId from the intent
        val categoryIdStr = arguments?.getString("categoryId")
        if (categoryIdStr != null) {
            categoryId = UUID.fromString(categoryIdStr)

            binding.buttonAddExpense.setOnClickListener {
                val name = binding.editTextExpenseName.text.toString()
                val amountStr = binding.editTextExpenseAmount.text.toString()
                val datePicker = binding.datePickerExpense // Get the DatePicker

                if (name.isNotBlank() && amountStr.isNotBlank()) {
                    try {
                        val amount = amountStr.toDouble()
                        val calendar = Calendar.getInstance()
                        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                        val date = calendar.time // Get the Date object from the DatePicker

                        val newExpense = Expense(name = name, amount = amount, categoryId = categoryId, expenseDate = date)
                        expenseData.addExpense(newExpense)

                        goBackToExpensesPage("Expense Added")

                    } catch (e: NumberFormatException) {
                        Toast.makeText(requireContext(), "Invalid budget", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error adding category: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            goBackToExpensesPage("Category Id is Missing")
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
}