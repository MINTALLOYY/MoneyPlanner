package com.vibhu.moneyplanner.categoryexpense

import android.os.Bundle
import android.util.Log
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
import com.vibhu.moneyplanner.roundingTwoDecimals

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
        val totalAutofill = arguments?.getString("total")
        val nameAutofill = arguments?.getString("name")
        val dateAutofill = arguments?.getString("date")
        if (categoryIdStr != null) {
            categoryId = UUID.fromString(categoryIdStr)

            if(totalAutofill != null){
                binding.editTextExpenseAmount.setText(totalAutofill)
                Log.d("Total", totalAutofill)
                if(nameAutofill == null) Toast.makeText(requireContext(), arguments?.getString("nameError"), Toast.LENGTH_SHORT).show()
                else binding.editTextExpenseName.setText(nameAutofill)
                if(dateAutofill == null) Toast.makeText(requireContext(), arguments?.getString("dateError"), Toast.LENGTH_SHORT).show()
                else {
                    autofillDatePicker(dateAutofill)
                }
            }

            binding.buttonAddExpense.setOnClickListener {
                val name = binding.editTextExpenseName.text.toString()
                val amountStr = binding.editTextExpenseAmount.text.toString()
                val datePicker = binding.datePickerExpense // Get the DatePicker

                if (name.isNotBlank() && amountStr.isNotBlank()) {
                    try {

                        if (name.length > 16) { // Double-check in code
                            Toast.makeText(requireContext(), "Max 16 characters allowed", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val amount = roundingTwoDecimals(amountStr.toDouble())
                        val calendar = Calendar.getInstance()
                        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                        val date = calendar.time // Get the Date object from the DatePicker

                        val newExpense = Expense(
                            name = name,
                            amount = amount,
                            categoryId = categoryId,
                            expenseDate = date)
                        expenseData.addExpense(newExpense)

                        goBackToExpensesPage("Expense Added")

                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error adding expense: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            goBackToExpensesPage("Category Id is Missing")
        }

    }

    private fun autofillDatePicker(string: String) {
        val dateParts = string.split("/")

        var year = 0
        var month = 0
        var day = 0
        if(dateParts[0].length == 4) {
            year = dateParts[0].toInt()
            month = dateParts[1].toInt() - 1 // Month is zero-based
            day = dateParts[2].toInt()
        }
        else if(dateParts[0].length == 2) {
            month = dateParts[0].toInt() - 1 // Month is zero-based
            day = dateParts[1].toInt()
            year = dateParts[2].toInt()
        }
        else {
            return
        }
        binding.datePickerExpense.updateDate(year, month, day)
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