package com.vibhu.moneyplanner.categoryexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.databinding.ActivityEditCategoryBinding
import com.vibhu.moneyplanner.models.Category
import java.util.UUID
import com.vibhu.moneyplanner.roundingTwoDecimals

class EditCategoryFragment : Fragment(){

    private lateinit var _binding: ActivityEditCategoryBinding
    private val binding get() = _binding!!
    private lateinit var categoryData: CategoryData
    private lateinit var categoryId: UUID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = ActivityEditCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryData = CategoryData(requireContext())

        binding.buttonCancel.setOnClickListener {
            goBackToHomePage()
        }

        // Get the category ID from the intent
        val categoryIdString = arguments?.getString("categoryId")
        if (categoryIdString != null) {
            categoryId = UUID.fromString(categoryIdString)

            // Load the category data for editing
            val category = categoryData.getAllCategories().find { it.categoryId == categoryId }
            if (category != null) {
                binding.editTextCategoryName.setText(category.categoryName)
                binding.editTextBudget.setText(category.budget.toString())
            } else {
                goBackToHomePage("Category Not Found") // Close the activity if the category isn't found
            }
        } else {
            goBackToHomePage("Category Id Not Found")
        }

        binding.buttonSaveCategory.setOnClickListener {
            val newName = binding.editTextCategoryName.text.toString()
            val newBudgetStr = binding.editTextBudget.text.toString()

            if (newName.isNotBlank() && newBudgetStr.isNotBlank()) {
                try {
                    val newBudget = roundingTwoDecimals(newBudgetStr.toDouble())
                    val updatedCategory = Category(categoryId, newName, newBudget) // Use existing ID

                    categoryData.updateCategory(updatedCategory)
                    goBackToHomePage("Category Updated")
                } catch (e: NumberFormatException) {
                    Toast.makeText(requireContext(), "Invalid budget", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun goBackToHomePage(message: String? = null){
        val bundle = Bundle()
        bundle.putString("message", message) //Pass message

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val categoriesFragment = CategoriesFragment()
        categoriesFragment.arguments = bundle // Set the bundle with categoryId

        fragmentTransaction.replace(R.id.fragment_container, categoriesFragment)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        categoryData.close()
    }
}