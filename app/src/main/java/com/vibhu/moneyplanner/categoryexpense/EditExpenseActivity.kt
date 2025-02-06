package com.vibhu.moneyplanner.categoryexpense

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityEditExpenseBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class EditExpenseActivity: AppCompatActivity() {
    private lateinit var binding: ActivityEditExpenseBinding
    private lateinit var expenseData: ExpenseData
    private lateinit var expenseId: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseData = ExpenseData(this)

        val expenseIdString = intent.getStringExtra("expense_id")
        if (expenseIdString!= null) {
            expenseId = UUID.fromString(expenseIdString)

            // Load expense data
            val expense = expenseData.getExpenseById(expenseId)
            if (expense!= null) {
                binding.editTextExpenseName.setText(expense.name)
                binding.editTextExpenseAmount.setText(expense.amount.toString())

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                binding.editTextExpenseDate.setText(dateFormat.format(expense.expenseDate))
            } else {
                // Handle expense not found
                Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            // Handle missing expense ID
            Toast.makeText(this, "Expense ID is missing", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.buttonSaveExpense.setOnClickListener {
            val newName = binding.editTextExpenseName.text.toString()
            val newAmountStr = binding.editTextExpenseAmount.text.toString()
            val newDateStr = binding.editTextExpenseDate.text.toString()

            if (newName.isBlank() || newAmountStr.isBlank() || newDateStr.isBlank()) { // Use isBlank() for better check
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stop further execution
            }

            try {
                val newAmount = newAmountStr.toDouble()

                val newDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(newDateStr)

                if (newDate == null) {
                    Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener // Stop further execution
                }

                // *** FETCH THE EXPENSE OBJECT HERE ***
                val expense = expenseData.getExpenseById(expenseId) // Implement this function in ExpenseData

                if (expense != null) { // Check if the expense was found
                    val updatedExpense = expense.copy(name = newName, amount = newAmount, expenseDate = newDate)
                    expenseData.updateExpense(updatedExpense)
                    Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Expense not found!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
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