package com.vibhu.moneyplanner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.databinding.FragmentIncomeBinding
import java.util.UUID

class IncomeFragment: Fragment() {

    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeData: IncomeData
    private lateinit var incomeAdapter: IncomeAdapter

    companion object {
        const val ARG_INCOME_CATEGORY_ID = "income_category_id" // Define the argument key
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIncomeBinding.inflate(inflater, container, false)
        val view = binding.root

        incomeData = IncomeData(requireContext())

        // Retrieve incomeCategoryId from arguments
        val incomeCategoryIdString = arguments?.getString(ARG_INCOME_CATEGORY_ID)
        val incomeCategoryId = incomeCategoryIdString?.let { UUID.fromString(it) }

        // Initialize the adapter with filtered incomes (if incomeCategoryId is available)
        incomeAdapter = IncomeAdapter(
            if (incomeCategoryId!= null) {
                incomeData.getIncomesByCategoryId(incomeCategoryId)
            } else {
                incomeData.getAllIncomes()
            }
        )
        binding.recyclerViewIncomes.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewIncomes.adapter = incomeAdapter

        binding.fabAddIncome.setOnClickListener {
            val incomeCategoryIdString = arguments?.getString(IncomeFragment.ARG_INCOME_CATEGORY_ID)

            if (incomeCategoryIdString != null) {  // Simplified null check
                val intent = Intent(requireContext(), AddIncomeActivity::class.java)
                intent.putExtra(AddIncomeActivity.EXTRA_INCOME_CATEGORY_ID, incomeCategoryIdString)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Please select an income category first", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Update the adapter with filtered incomes (if incomeCategoryId is available)
        val incomeCategoryIdString = arguments?.getString(ARG_INCOME_CATEGORY_ID)
        val incomeCategoryId = incomeCategoryIdString?.let { UUID.fromString(it) }
        incomeAdapter.updateIncomes(
            if (incomeCategoryId!= null) {
                incomeData.getIncomesByCategoryId(incomeCategoryId)
            } else {
                incomeData.getAllIncomes()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        incomeData.close()
    }
}