package com.vibhu.moneyplanner // Replace with your package name

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityEditIncomeCategoryBinding // Replace with your binding class
import com.vibhu.moneyplanner.models.IncomeCategory
import java.util.UUID

class EditIncomeCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditIncomeCategoryBinding
    private lateinit var incomeCategoryData: IncomeCategoryData
    private lateinit var incomeCategoryId: UUID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditIncomeCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomeCategoryData = IncomeCategoryData(this)

        val incomeCategoryIdString = intent.getStringExtra("income_category_id")
        if (incomeCategoryIdString != null) {
            incomeCategoryId = UUID.fromString(incomeCategoryIdString)

            val incomeCategory = incomeCategoryData.getIncomeCategoryById(incomeCategoryId)
            if (incomeCategory != null) {
                binding.editTextIncomeCategoryName.setText(incomeCategory.incomeCategoryName)
            } else {
                Toast.makeText(this, "Income Category not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } else {
            Toast.makeText(this, "Income Category ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.buttonSaveIncomeCategory.setOnClickListener {
            val newName = binding.editTextIncomeCategoryName.text.toString()

            if (newName.isBlank()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedCategory = IncomeCategory(incomeCategoryId, newName)
            incomeCategoryData.updateIncomeCategory(updatedCategory)
            Toast.makeText(this, "Income Category updated", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        incomeCategoryData.close()
    }
}