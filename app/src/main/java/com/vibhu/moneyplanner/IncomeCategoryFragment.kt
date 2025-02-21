package com.vibhu.moneyplanner // Replace with your package name

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.categoryexpense.AddCategoryFragment
import com.vibhu.moneyplanner.categoryexpense.EditCategoryFragment
import com.vibhu.moneyplanner.databinding.FragmentIncomeCategoryBinding
import java.util.UUID

class IncomeCategoryFragment : Fragment() {

    private var _binding: FragmentIncomeCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeCategoryData: IncomeCategoryData
    private lateinit var incomeCategoryAdapter: IncomeCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeCategoryData = IncomeCategoryData(requireContext())


        val message = arguments?.getString("message")
        if(message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        }

        binding.recyclerViewIncomeCategories.layoutManager = LinearLayoutManager(requireContext())

        incomeCategoryAdapter = IncomeCategoryAdapter(
            incomeCategoryData.getAllIncomeCategories(),
            requireContext(),
            { category -> // onItemEditClick
                val bundle = Bundle()
                bundle.putString("income_category_id", category.incomeCategoryId.toString()) // Pass categoryId

                val fragmentManager = requireActivity().supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()

                val editIncomeCategoryFragment = EditIncomeCategoryFragment()
                editIncomeCategoryFragment.arguments = bundle // Set the bundle with categoryId

                fragmentTransaction.replace(R.id.fragment_container, editIncomeCategoryFragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            },
            { category -> // onItemDeleteClick
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Income Category")
                    .setMessage("Are you sure you want to delete this income category?")
                    .setPositiveButton("Delete") { _, _ ->
                        incomeCategoryData.deleteIncomeCategory(category.incomeCategoryId)
                        incomeCategoryAdapter.updateItems(incomeCategoryData.getAllIncomeCategories())
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            { category -> // onItemClick (to view incomes)
                val incomesFragment = IncomeFragment()
                val bundle = Bundle()
                bundle.putString("incomeCategoryId", category.incomeCategoryId.toString())
                incomesFragment.arguments = bundle

                val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragment_container, incomesFragment) // Replace the fragment
                fragmentTransaction.addToBackStack(null) // Add to back stack (optional)
                fragmentTransaction.commit()
            }
        )

        binding.recyclerViewIncomeCategories.adapter = incomeCategoryAdapter

        binding.fabAddIncomeCategory.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()

            val addIncomeCategoryFragment = AddIncomeCategoryFragment()

            fragmentTransaction.replace(R.id.fragment_container, addIncomeCategoryFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    override fun onResume() {
        super.onResume()
        incomeCategoryAdapter.updateItems(incomeCategoryData.getAllIncomeCategories())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        incomeCategoryData.close()
    }
}