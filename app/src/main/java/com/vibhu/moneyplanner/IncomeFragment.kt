package com.vibhu.moneyplanner // Replace with your package name

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.categoryexpense.ExpensesFragment
import com.vibhu.moneyplanner.databinding.FragmentIncomeBinding // Replace with your binding class
import java.util.UUID

class IncomeFragment : Fragment() {

    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeData: IncomeData
    private lateinit var incomeAdapter: IncomeAdapter
    private lateinit var incomeCategoryId: UUID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val incomeCategoryIdStr = arguments?.getString("incomeCategoryId")
        if (incomeCategoryIdStr!= null) {
            incomeCategoryId = UUID.fromString(incomeCategoryIdStr)
        } else {
            // add action if it doesn't exist like toast
        }

        incomeData = IncomeData(requireContext())

        val incomeCategoryIdString = arguments?.getString("incomeCategoryId")
        if (incomeCategoryIdString != null) {
            incomeCategoryId = UUID.fromString(incomeCategoryIdString)
        } else {
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            fragmentTransaction.replace(R.id.fragment_container, IncomeCategoryFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
            return
        }

        binding.recyclerViewIncomes.layoutManager = LinearLayoutManager(requireContext())

        incomeAdapter = IncomeAdapter(
            incomeData.getIncomesByCategoryId(incomeCategoryId), // Filter by category!
            requireContext(),
            { income -> // onItemEditClick
                val intent = Intent(requireContext(), EditIncomeActivity::class.java)
                intent.putExtra("income_id", income.incomeId.toString())
                startActivity(intent)
            },
            { income -> // onItemDeleteClick
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Income")
                    .setMessage("Are you sure you want to delete this income?")
                    .setPositiveButton("Delete") { _, _ ->
                        incomeData.deleteIncome(income.incomeId)
                        incomeAdapter.updateItems(incomeData.getIncomesByCategoryId(incomeCategoryId))
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.recyclerViewIncomes.adapter = incomeAdapter

        binding.fabAddIncome.setOnClickListener {
            val intent = Intent(requireContext(), AddIncomeActivity::class.java)
            intent.putExtra("income_category_id", incomeCategoryId.toString())
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        incomeAdapter.updateItems(incomeData.getIncomesByCategoryId(incomeCategoryId))
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        incomeData.close()
    }
}