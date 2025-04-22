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
import android.util.Log

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

                binding.editTextIncomeName.setText(income.incomeLogName)

                val calendar = Calendar.getInstance()
                calendar.time = income.receivedDate

                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                binding.editTextIncomeDate.updateDate(year, month, day)

                incomeCategoryId = income.incomeCategoryId
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
            val newDateInfo = binding.editTextIncomeDate
            val incomeName = binding.editTextIncomeName.text.toString()

            if (newAmountStr.isBlank() || incomeName.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                try {
                    val newAmount = newAmountStr.toDouble()

                    // Extracting date from DatePicker
                    val calendar = Calendar.getInstance()
                    calendar.set(newDateInfo.year, newDateInfo.month, newDateInfo.dayOfMonth)
                    val newDate = calendar.time

                    Log.d("Income Name", incomeName)

                    if (newDate != null) {
                        val updatedIncome = Income(
                            incomeId, // Keep the original incomeId
                            newAmount,
                            incomeCategoryId,
                            newDate,
                            incomeName
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

        binding.buttonCancelIncome.setOnClickListener {
            goBackToIncomePage()
        }
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