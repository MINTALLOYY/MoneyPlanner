package com.vibhu.moneyplanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.databinding.FragmentIncomeCategoryBinding

class IncomeCategoryFragment : Fragment() {

    private var _binding: FragmentIncomeCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeData: IncomeData
    private lateinit var incomeCategoryAdapter: IncomeCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIncomeCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        incomeData = IncomeData(requireContext())

        binding.recyclerViewIncomeCategories.layoutManager = LinearLayoutManager(requireContext())
        incomeCategoryAdapter = IncomeCategoryAdapter(
            incomeData.getAllIncomeCategories()
        ) { incomeCategory -> // Lambda for item click
            val fragment = IncomeFragment()
            val bundle = Bundle()
            bundle.putString(IncomeFragment.ARG_INCOME_CATEGORY_ID, incomeCategory.incomeCategoryId.toString())
            fragment.arguments = bundle

            val transaction = requireActivity().supportFragmentManager.beginTransaction() // Use requireActivity()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        binding.recyclerViewIncomeCategories.adapter = incomeCategoryAdapter

        binding.fabAddIncomeCategory.setOnClickListener {
            val intent = Intent(requireContext(), AddIncomeCategoryActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        incomeCategoryAdapter.updateIncomeCategories(incomeData.getAllIncomeCategories())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        incomeData.close()
    }
}