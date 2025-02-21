package com.vibhu.moneyplanner // Replace with your package name

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.categoryexpense.EditExpenseFragment
import com.vibhu.moneyplanner.categoryexpense.ExpensesFragment
import com.vibhu.moneyplanner.databinding.FragmentIncomeBinding // Replace with your binding class
import com.vibhu.moneyplanner.models.Income
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

            val message = arguments?.getString("message")
            if(message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
            }
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
                goToEditIncomeFragment(income)
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
            goToAddIncomeFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        incomeAdapter.updateItems(incomeData.getIncomesByCategoryId(incomeCategoryId))
    }

    fun goToEditIncomeFragment(income: Income){
        val bundle = Bundle()
        bundle.putString("income_id", income.incomeId.toString())
        bundle.putString("income_category_id", incomeCategoryId.toString())


        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val editIncomeFragment = EditIncomeFragment()
        editIncomeFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragment_container, editIncomeFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    fun goToAddIncomeFragment(){
        val bundle = Bundle()
        bundle.putString("income_category_id", incomeCategoryId.toString())

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val addIncomeFragment = AddIncomeFragment()
        addIncomeFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragment_container, addIncomeFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        incomeData.close()
    }
}