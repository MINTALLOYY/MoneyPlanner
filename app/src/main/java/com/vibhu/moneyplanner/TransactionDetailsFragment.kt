package com.vibhu.moneyplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.vibhu.moneyplanner.categoryexpense.EditExpenseFragment
import com.vibhu.moneyplanner.categoryexpense.ExpenseData
import com.vibhu.moneyplanner.databinding.FragmentTransactionsDetailsBinding
import com.vibhu.moneyplanner.models.Transaction
import java.util.UUID

class TransactionDetailsFragment: Fragment() {

    private var _binding: FragmentTransactionsDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionData: TransactionData
    private lateinit var incomeData: IncomeData
    private lateinit var expenseData: ExpenseData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransactionsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionData = TransactionData(requireContext())
        incomeData = IncomeData(requireContext())
        expenseData = ExpenseData(requireContext())

        val transactionIdStr = arguments?.getString("transactionId")
        val message = arguments?.getString("message")
        if(transactionIdStr != null){
            if(message != null){
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
            val transactionId = UUID.fromString(transactionIdStr)
            val transaction = transactionData.getTransactionByID(transactionId)
            if(transaction != null){
                if(transaction.isIncome){
                    binding.transactionType.text = "Income"
                }
                else{
                    binding.transactionType.text = "Expense"
                }

                binding.transactionName.text = transaction.transactionName
                binding.transactionAmount.text = transaction.amount.toString()
                binding.transactionDate.text = transaction.date.toString()

                binding.editTransactionButton.setOnClickListener{
                    editTransaction(transaction)
                }
                binding.deleteTransactionButton.setOnClickListener{
                    deleteTransaction(transaction)
                }

            }
        }
    }

    fun editTransaction(transaction: Transaction){

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val bundle = Bundle()

        if(transaction.isIncome){
            bundle.putString("income_id", transaction.id.toString())

            val editIncomeFragment = EditIncomeFragment()
            editIncomeFragment.arguments = bundle

            fragmentTransaction.replace(R.id.fragment_container, editIncomeFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
        else{
            bundle.putString("expense_id", transaction.id.toString())

            val editExpenseFragment = EditExpenseFragment()
            editExpenseFragment.arguments = bundle

            fragmentTransaction.replace(R.id.fragment_container, editExpenseFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    fun deleteTransaction(transaction: Transaction){
        if(transaction.isIncome){
            incomeData.deleteIncome(transaction.id)
            goBackToHomePage("Income Deleted")
        }
        else{
            expenseData.deleteExpense(transaction.id)
            goBackToHomePage("Expense Deleted")
        }
    }

    fun goBackToHomePage(message: String){
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val bundle = Bundle()
        bundle.putString("message", message)

        val homeFragment = HomeFragment()
        homeFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragment_container, homeFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}