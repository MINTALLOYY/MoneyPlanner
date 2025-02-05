package com.vibhu.moneyplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.databinding.ActivityCategoryBinding // Correct binding class

class HomeFragment : Fragment() {

    private var _binding: ActivityCategoryBinding? = null // Correct binding class
    private val binding get() = _binding!!
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryData: CategoryData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityCategoryBinding.inflate(inflater, container, false) // Correct layout inflation
        val view = binding.root

        categoryData = CategoryData(requireContext())

        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(requireContext())

        categoryAdapter = CategoryAdapter(categoryData.getAllCategories()) { categoryId ->
            val fragment = CategoryExpensesFragment()
            val bundle = Bundle()
            bundle.putString(CategoryExpensesFragment.EXTRA_CATEGORY_ID, categoryId.toString()) // Pass categoryId
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace fragment_container
                .addToBackStack(null) // Optional: Add to back stack for navigation
                .commit()
        }
        binding.recyclerViewCategories.adapter = categoryAdapter

        binding.buttonAddCategory.setOnClickListener {
            val intent = android.content.Intent(requireContext(), AddCategoryActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        categoryAdapter.updateCategories(categoryData.getAllCategories())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        categoryData.close()
    }
}