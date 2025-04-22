package com.vibhu.moneyplanner // Replace with your package name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.databinding.ActivityEditIncomeCategoryBinding // Replace with your binding class
import com.vibhu.moneyplanner.models.IncomeCategory
import java.util.UUID

class EditIncomeCategoryFragment : Fragment() {

    private lateinit var binding: ActivityEditIncomeCategoryBinding
    private lateinit var incomeCategoryData: IncomeCategoryData
    private lateinit var incomeCategoryId: UUID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = ActivityEditIncomeCategoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeCategoryData = IncomeCategoryData(requireContext())

        val incomeCategoryIdString = arguments?.getString("income_category_id")
        if (incomeCategoryIdString != null) {
            incomeCategoryId = UUID.fromString(incomeCategoryIdString)

            val incomeCategory = incomeCategoryData.getIncomeCategoryById(incomeCategoryId)
            if (incomeCategory != null) {
                binding.editTextIncomeCategoryName.setText(incomeCategory.incomeCategoryName)
            } else {
                goBackToIncomePage("Income Category not found")
                return
            }
        } else {
            goBackToIncomePage("Income Category ID is missing")
            return
        }

        binding.buttonSaveIncomeCategory.setOnClickListener {
            val newName = binding.editTextIncomeCategoryName.text.toString()

            if (newName.isBlank()) {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedCategory = IncomeCategory(incomeCategoryId, newName)
            incomeCategoryData.updateIncomeCategory(updatedCategory)
            goBackToIncomePage("Income Category updated")
        }

        binding.buttonCancelIncomeCategory.setOnClickListener {
            goBackToIncomePage()
        }

    }

    fun goBackToIncomePage(message: String? = null){
        val bundle = Bundle()
        bundle.putString("message", message) //Pass message

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val incomeCategoryFragment = IncomeCategoryFragment()
        incomeCategoryFragment.arguments = bundle // Set the bundle with categoryId

        fragmentTransaction.replace(R.id.fragment_container, incomeCategoryFragment)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        incomeCategoryData.close()
    }
}