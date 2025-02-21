package com.vibhu.moneyplanner // Replace with your package name

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.databinding.ActivityEditIncomeBinding // Replace with your binding class
import com.vibhu.moneyplanner.models.Income
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class EditIncomeFragment : Fragment() {

    private lateinit var binding: ActivityEditIncomeBinding
    private lateinit var incomeData: IncomeData
    private lateinit var incomeId: UUID
    private lateinit var incomeCategoryId: UUID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        super.onCreate(savedInstanceState)

        binding = ActivityEditIncomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeData = IncomeData(requireContext())

        val incomeIdString = arguments?.getString("income_id")
        val incomeCategoryIdString = arguments?.getString("income_category_id")
        if (incomeIdString != null && incomeCategoryIdString != null) {
            incomeId = UUID.fromString(incomeIdString)
            incomeCategoryId = UUID.fromString(incomeCategoryIdString)

            val income = incomeData.getIncomeById(incomeId)
            if (income != null) {
                binding.editTextIncomeAmount.setText(income.amount.toString())

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.editTextIncomeDate.setText(dateFormat.format(income.receivedDate))
                incomeCategoryId = income.incomeCategoryId // Save for potential use later
            } else {
                goBackToIncomePage("Income not Found")
                return
            }
        } else {
            goBackToIncomePage("Income ID is Missing")
            return
        }


        binding.buttonSaveIncome.setOnClickListener {
            val newAmountStr = binding.editTextIncomeAmount.text.toString()
            val newDateStr = binding.editTextIncomeDate.text.toString()

            if (newAmountStr.isBlank() || newDateStr.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val newAmount = newAmountStr.toDouble()
                val newDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(newDateStr)

                if (newDate != null) {
                    val updatedIncome = Income(
                        incomeId, // Keep the original incomeId
                        newAmount,
                        incomeCategoryId,
                        newDate,
                    )

                    incomeData.updateIncome(updatedIncome)
                    goBackToIncomePage("Income Updated")
                } else {
                    Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show()
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, selectedYear)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                }.time

                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)
                binding.editTextIncomeDate.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    fun goBackToIncomePage(message: String? = null){
        val bundle = Bundle()
        bundle.putString("incomeCategoryId", incomeCategoryId.toString()) // Pass categoryId
        bundle.putString("message", message) //Pass message

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val incomeFragment = IncomeFragment()
        incomeFragment.arguments = bundle // Set the bundle with categoryId

        fragmentTransaction.replace(R.id.fragment_container, incomeFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }


    override fun onDestroy() {
        super.onDestroy()
        incomeData.close()
    }
}