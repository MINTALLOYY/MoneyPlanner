package com.vibhu.moneyplanner.CategoryExpense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.databinding.FragmentHomeBinding

class HomeFragment: Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryData: CategoryData
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryData = CategoryData(requireContext())
        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(requireContext())

        categoryAdapter = CategoryAdapter(
            categoryData.getAllCategories(),
            requireContext(),
            {
                categoryAdapter.updateItems(categoryData.getAllCategories())
            },
            { category ->
                val bundle = Bundle()
                bundle.putString("categoryId", category.categoryId.toString())

                val fragmentManager = requireActivity().supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()

                val expensesFragment = ExpensesFragment()
                expensesFragment.arguments = bundle

                fragmentTransaction.replace(R.id.fragment_container, expensesFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }
        )
        binding.recyclerViewCategories.adapter = categoryAdapter

        binding.buttonAddCategory.setOnClickListener {
            val intent = Intent(requireContext(), AddCategoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        categoryData.close()
    }
}