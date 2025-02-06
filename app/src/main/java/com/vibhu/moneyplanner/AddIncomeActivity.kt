package com.vibhu.moneyplanner

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityAddIncomeBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID

class AddIncomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddIncomeBinding
    private lateinit var incomeData: IncomeData
    private lateinit var incomeCategoryId: UUID
    private lateinit var receivedDateCalendar: Calendar

    companion object {
        const val EXTRA_INCOME_CATEGORY_ID = "income_category_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomeData = IncomeData(this)

        incomeCategoryId = UUID.fromString(intent.getStringExtra(EXTRA_INCOME_CATEGORY_ID)!!)

        receivedDateCalendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            receivedDateCalendar.set(Calendar.YEAR, year)
            receivedDateCalendar.set(Calendar.MONTH, monthOfYear)
            receivedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateReceivedDateLabel()
        }

        binding.editTextDateReceived.setOnClickListener {
            DatePickerDialog(
                this,
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
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
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

                Toast.makeText(this, "Income added successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error adding income: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateReceivedDateLabel() {
        val myFormat = "MM/dd/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        binding.editTextDateReceived.setText(dateFormat.format(receivedDateCalendar.time))
    }

    override fun onDestroy() {
        super.onDestroy()
        incomeData.close()
    }
}