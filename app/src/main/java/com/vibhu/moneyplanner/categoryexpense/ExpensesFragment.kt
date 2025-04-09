package com.vibhu.moneyplanner.categoryexpense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.CameraReceiptActivity
import com.vibhu.moneyplanner.CameraReceiptFragment
import com.vibhu.moneyplanner.Expense
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.ReceiptScannerFragment
import com.vibhu.moneyplanner.databinding.FragmentExpensesBinding
import java.util.UUID

class ExpensesFragment: Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryId: UUID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expenseData = ExpenseData(requireContext())

        // Get the category ID from the arguments
        val categoryIdString = arguments?.getString("categoryId")
        if (categoryIdString!= null) {
            categoryId = UUID.fromString(categoryIdString)

            val message = arguments?.getString("message")
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }

            binding.recyclerViewExpenses.layoutManager = LinearLayoutManager(requireContext())

            expenseAdapter = ExpenseAdapter(
                expenseData.getExpensesByCategoryId(categoryId),
                requireContext(),
                { expense -> // onItemEditClick
                    goToEditExpenseFragment(expense)
                },
                { expense -> // onItemDeleteClick
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Expense")
                        .setMessage("Are you sure you want to delete this expense?")
                        .setPositiveButton("Delete") { _, _ ->
                            expenseData.deleteExpense(expense.expenseId)
                            // Update the list after deleting
                            expenseAdapter.updateItems(expenseData.getExpensesByCategoryId(categoryId))
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            )

            binding.recyclerViewExpenses.adapter = expenseAdapter

            binding.fabAddExpense.setOnClickListener {
                goToAddExpenseFragment()
            }
            binding.fabScanReceipt.setOnClickListener {
                goToReceiptScanner()
                /*val intent = Intent(requireContext(), CameraReceiptActivity::class.java)
                startActivity(intent)*/
            }

        } else {
            // add action if it doesn't exist like toast
        }


    }

    fun goToReceiptScanner(){
        val bundle = Bundle()
        bundle.putString("category_id", categoryId.toString())

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val cameraReceiptFragment = CameraReceiptFragment()
        cameraReceiptFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragment_container, cameraReceiptFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    fun goToEditExpenseFragment(expense: Expense){
        val bundle = Bundle()
        bundle.putString("expense_id", expense.expenseId.toString()) // Pass expenseId
        bundle.putString("category_id", categoryId.toString()) // Pass categoryId

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val editExpenseFragment = EditExpenseFragment()
        editExpenseFragment.arguments = bundle // Set the bundle with expenseId

        fragmentTransaction.replace(R.id.fragment_container, editExpenseFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    fun goToAddExpenseFragment(){
        val bundle = Bundle()
        bundle.putString("categoryId", categoryId.toString()) // Pass categoryId

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val addExpenseFragment = AddExpenseFragment()
        addExpenseFragment.arguments = bundle // Set the bundle with categoryId

        fragmentTransaction.replace(R.id.fragment_container, addExpenseFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onResume() {
        super.onResume()
        expenseAdapter.updateItems(expenseData.getExpensesByCategoryId(categoryId))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        expenseData.close()
    }
}