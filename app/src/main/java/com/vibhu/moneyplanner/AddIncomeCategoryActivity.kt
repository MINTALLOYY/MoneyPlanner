package com.vibhu.moneyplanner

import IncomeCategory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityAddIncomeCategoryBinding
import com.vibhu.moneyplanner.IncomeData

class AddIncomeCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddIncomeCategoryBinding
    private lateinit var incomeData: IncomeData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddIncomeCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomeData = IncomeData(this)

        binding.buttonAddIncomeCategory.setOnClickListener {
            val name = binding.editTextIncomeCategoryName.text.toString()

            if (name.isBlank()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val incomeCategory = IncomeCategory(incomeCategoryName = name)
            incomeData.addIncomeCategory(incomeCategory)

            Toast.makeText(this, "Income category added successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        incomeData.close()
    }
}