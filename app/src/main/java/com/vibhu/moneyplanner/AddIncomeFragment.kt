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

        receivedDateCalendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            receivedDateCalendar.set(Calendar.YEAR, year)
            receivedDateCalendar.set(Calendar.MONTH, monthOfYear)
            receivedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateReceivedDateLabel()
        }

        binding.editTextDateReceived.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                receivedDateCalendar.get(Calendar.YEAR),
                receivedDateCalendar.get(Calendar.MONTH),
                receivedDateCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.buttonAddIncome.setOnClickListener {
            val amountStr = binding.editTextAmount.text.toString()
            val receivedDateStr = binding.editTextDateReceived.text.toString()

            if (amountStr.isBlank() || receivedDateStr.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                val receivedDate = receivedDateCalendar.time // Get Date from Calendar

                val income = Income(
                    amount = amount,
                    incomeCategoryId = incomeCategoryId,
                    receivedDate = receivedDate
                )
                incomeData.addIncome(income)

                goBackToIncomePage("Income added successfully")

            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error adding income: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateReceivedDateLabel() {
        val myFormat = "MM/dd/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        binding.editTextDateReceived.setText(dateFormat.format(receivedDateCalendar.time))
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