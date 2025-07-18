package com.vibhu.moneyplanner.uiFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.addingData.AddCategoryFragment
import com.vibhu.moneyplanner.listAdapters.CategoryAdapter
import com.vibhu.moneyplanner.database.CategoryData
import com.vibhu.moneyplanner.database.ExpenseData
import com.vibhu.moneyplanner.uiFragments.ExpensesFragment
import com.vibhu.moneyplanner.databinding.FragmentCategoriesBinding
import com.vibhu.moneyplanner.editingData.EditCategoryFragment

class CategoriesFragment: Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryData: CategoryData
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var expenseData: ExpenseData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryData = CategoryData(requireContext())
        expenseData = ExpenseData(requireContext())
        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(requireContext())

        categoryAdapter = CategoryAdapter(
            categoryData.getAllCategories(),
            requireContext(),
            { category -> // onItemClick (for the whole item)
                val bundle = Bundle()
                bundle.putString("categoryId", category.categoryId.toString()) // Pass categoryId

                val fragmentManager = requireActivity().supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()

                val expensesFragment = ExpensesFragment()
                expensesFragment.arguments = bundle // Set the bundle with categoryId

                fragmentTransaction.replace(R.id.fragment_container, expensesFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            },
            { category -> // onItemEditClick
                val bundle = Bundle()
                bundle.putString("categoryId", category.categoryId.toString()) // Pass categoryId

                val fragmentManager = requireActivity().supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()

                val editCategoryFragment = EditCategoryFragment()
                editCategoryFragment.arguments = bundle // Set the bundle with categoryId

                fragmentTransaction.replace(R.id.fragment_container, editCategoryFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            },
            { category -> // onItemDeleteClick
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete this category?")
                    .setPositiveButton("Delete") { _, _ ->
                        categoryData.deleteCategory(category.categoryId)
                        expenseData.deleteAllExpensesInCategoryId(category.categoryId)
                        categoryAdapter.updateItems(categoryData.getAllCategories())
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.recyclerViewCategories.adapter = categoryAdapter

        binding.buttonAddCategory.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            val addCategoryFragment = AddCategoryFragment()

            fragmentTransaction.replace(R.id.fragment_container, addCategoryFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    override fun onResume() {
        super.onResume()
        categoryAdapter.updateItems(categoryData.getAllCategories())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        categoryData.close()
    }
}