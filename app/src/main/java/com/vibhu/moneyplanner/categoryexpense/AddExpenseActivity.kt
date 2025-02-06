package com.vibhu.moneyplanner.categoryexpense

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.Expense
import com.vibhu.moneyplanner.databinding.ActivityAddExpenseBinding
import java.util.Calendar
import java.util.UUID

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var expenseData: ExpenseData
    private lateinit var categoryId: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseData = ExpenseData(this)

        // Get the categoryId from the intent
        val categoryIdStr = intent.getStringExtra("category_id")
        if (categoryIdStr != null) {
            try {
                categoryId = UUID.fromString(categoryIdStr)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, "Invalid category ID", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } else {
            Toast.makeText(this, "Category ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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

                    Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                    finish()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error adding expense: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}