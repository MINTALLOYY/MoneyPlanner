package com.vibhu.moneyplanner // Replace with your package name

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityEditIncomeBinding // Replace with your binding class
import com.vibhu.moneyplanner.models.Income
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class EditIncomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditIncomeBinding
    private lateinit var incomeData: IncomeData
    private lateinit var incomeId: UUID
    private lateinit var incomeCategoryId: UUID // To pass back if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomeData = IncomeData(this)

        val incomeIdString = intent.getStringExtra("income_id")
        if (incomeIdString != null) {
            incomeId = UUID.fromString(incomeIdString)

            val income = incomeData.getIncomeById(incomeId)
            if (income != null) {
                binding.editTextIncomeAmount.setText(income.amount.toString())

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.editTextIncomeDate.setText(dateFormat.format(income.receivedDate))
                incomeCategoryId = income.incomeCategoryId // Save for potential use later
            } else {
                Toast.makeText(this, "Income not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } else {
            Toast.makeText(this, "Income ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        binding.buttonSaveIncome.setOnClickListener {
            val newAmountStr = binding.editTextIncomeAmount.text.toString()
            val newDateStr = binding.editTextIncomeDate.text.toString()

            if (newAmountStr.isBlank() || newDateStr.isBlank()) {
                Toast.makeText(this, "Please fill in required fields", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Income updated", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
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

    override fun onDestroy() {
        super.onDestroy()
        incomeData.close()
    }
}