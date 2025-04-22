package com.vibhu.moneyplanner

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.categoryexpense.ExpensesFragment
import com.vibhu.moneyplanner.databinding.ActivityAddIncomeBinding
import com.vibhu.moneyplanner.models.Income
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

class AddIncomeFragment : Fragment() {

    private lateinit var binding: ActivityAddIncomeBinding
    private lateinit var incomeData: IncomeData
    private lateinit var incomeCategoryId: UUID
    private lateinit var receivedDateCalendar: Calendar

    companion object {
        const val EXTRA_INCOME_CATEGORY_ID = "income_category_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeData = IncomeData(requireContext())

        incomeCategoryId = UUID.fromString(arguments?.getString(EXTRA_INCOME_CATEGORY_ID)!!)

        binding.editTextIncomeName.setHint("Income Log " + (incomeData.getSizeOfIncomesInCategory(incomeCategoryId) + 1))

        binding.buttonAddIncome.setOnClickListener {
            val amountStr = binding.editTextAmount.text.toString()
            val datePicker = binding.editTextDateReceived
            val incomeName = binding.editTextIncomeName.text.toString()

            if (amountStr.isBlank() || incomeName.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                try {

                    if (incomeName.length > 20) { // Double-check in code
                        Toast.makeText(requireContext(), "Max 20 characters allowed", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val amount = amountStr.toDouble()

                    receivedDateCalendar = Calendar.getInstance()
                    receivedDateCalendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                    val receivedDate = receivedDateCalendar.time // Get Date from the DatePicker

                    val income = Income(
                        amount = amount,
                        incomeCategoryId = incomeCategoryId,
                        receivedDate = receivedDate,
                        incomeLogName = incomeName
                    )
                    incomeData.addIncome(income)

                    goBackToIncomePage("Income Added")

                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error adding income: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonCancel.setOnClickListener {
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
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        incomeData.close()
    }
}