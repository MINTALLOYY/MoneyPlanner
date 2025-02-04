package com.vibhu.moneyplanner

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityAddExpenseBinding
import java.lang.NumberFormatException
import java.util.Calendar
import java.util.Date
import java.util.UUID

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var expenseData: ExpenseData
    private lateinit var categoryId: UUID

    companion object {
        const val EXTRA_CATEGORY_ID = "category_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseData = ExpenseData(this)

        categoryId = UUID.fromString(intent.getStringExtra(EXTRA_CATEGORY_ID)!!)

        binding.buttonSaveExpense.setOnClickListener {
            val name = binding.editTextExpenseName.text.toString()
            val amountStr = binding.editTextExpenseAmount.text.toString()

            if (name.isNotEmpty() && amountStr.isNotEmpty()) {
                try {
                    val amount = amountStr.toDouble()
                    val selectedDate = with(binding.datePickerExpense) {
                        Calendar.getInstance().apply {
                            set(year, month, dayOfMonth)
                        }.time
                    }

                    val newExpense = Expense(
                        expenseName = name,
                        expenseAmount = amount,
                        categoryId = categoryId,
                        expenseDate = selectedDate
                    )
                    expenseData.addExpense(newExpense)
                    finish()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid expense amount", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonCancelExpense.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        expenseData.close()
    }
}