package com.vibhu.moneyplanner.addingData

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.uiFragments.CategoriesFragment
import com.vibhu.moneyplanner.database.CategoryData
import com.vibhu.moneyplanner.databinding.FragmentAddCategoryBinding
import com.vibhu.moneyplanner.models.Category

class AddCategoryFragment : Fragment() {

    private lateinit var _binding: FragmentAddCategoryBinding
    private val binding get() = _binding!!
    private lateinit var categoryData: CategoryData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)

        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryData = CategoryData(requireContext())
        binding.buttonCancel.setOnClickListener {
            goBackToHomePage()
        }

        binding.buttonSaveCategory.setOnClickListener {
            val categoryName = binding.editTextCategoryName.text.toString()
            if (categoryName.isNotBlank()) {
                try {

                    if (categoryName.length > 16) { // Double-check in code
                        Toast.makeText(requireContext(), "Max 16 characters allowed", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val newCategory = Category(categoryName = categoryName)
                    categoryData.addCategory(newCategory)

                    goBackToHomePage("Category Added")

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error adding category: ${e.message}", Toast.LENGTH_SHORT).show()
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
}