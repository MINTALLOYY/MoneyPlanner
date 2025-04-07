package com.vibhu.moneyplanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.vibhu.moneyplanner.categoryexpense.CategoryData
import com.vibhu.moneyplanner.categoryexpense.EditExpenseFragment
import com.vibhu.moneyplanner.categoryexpense.ExpenseData
import com.vibhu.moneyplanner.databinding.FragmentTransactionsDetailsBinding
import com.vibhu.moneyplanner.models.IncomeCategory
import com.vibhu.moneyplanner.models.Transaction
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class TransactionDetailsFragment: Fragment() {

    private var _binding: FragmentTransactionsDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionData: TransactionData
    private lateinit var incomeData: IncomeData
    private lateinit var expenseData: ExpenseData
    private lateinit var incomeCategoryData: IncomeCategoryData
    private lateinit var expenseCategoryData: CategoryData

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
        incomeCategoryData = IncomeCategoryData(requireContext())
        expenseCategoryData = CategoryData(requireContext())
        expenseData = ExpenseData(requireContext())

        val transactionIdStr = arguments?.getString("transaction_id")
        val message = arguments?.getString("message")
        if(transactionIdStr != null){
            if(message != null){
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
            val transactionId = UUID.fromString(transactionIdStr)
            val transaction = transactionData.getTransactionByID(transactionId)
            if(transaction != null){

                val typeTextView = binding.transactionType.text
                val amountTextView = binding.transactionAmount.text

                if(transaction.isIncome){
                    binding.transactionType.text = "$typeTextView Income"

                    binding.editTransactionButton.text = "Edit Income"
                    binding.deleteTransactionButton.text = "Delete Income"

                    binding.transactionAmount.text = "$amountTextView + $"

                    val incomeCategory = incomeCategoryData.getIncomeCategoryById(transaction.categoryId)
                    if(incomeCategory != null){
                        binding.transactionCategoryName.text = "Transaction Category Name: ${incomeCategory.incomeCategoryName}"
                    }
                }
                else{
                    binding.transactionType.text = "$typeTextView Expense"

                    binding.editTransactionButton.text = "Edit Expense"
                    binding.deleteTransactionButton.text = "Delete Expense"

                    binding.transactionAmount.text = "$amountTextView - $"

                    val expenseCategory = expenseCategoryData.getCategoryById(transaction.categoryId)
                    if(expenseCategory != null){
                        binding.transactionCategoryName.text = "Transaction Category Name: ${expenseCategory.categoryName}"
                    }
                }

                binding.transactionName.text = transaction.transactionName
                binding.transactionAmount.text = transaction.amount.toString()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(transaction.date)
                binding.transactionDate.text = formattedDate

                binding.editTransactionButton.setOnClickListener{
                    editTransaction(transaction)
                }
                binding.deleteTransactionButton.setOnClickListener{
                    deleteTransaction(transaction)
                }

            }
        }
        else{
            goBackToHomePage("Transaction not found")
        }
    }

    fun editTransaction(transaction: Transaction){

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val bundle = Bundle()

        if(transaction.isIncome){
            bundle.putString("income_id", transaction.id.toString())
            bundle.putString("income_category_id", transaction.categoryId.toString())

            val editIncomeFragment = EditIncomeFragment()
            editIncomeFragment.arguments = bundle

            fragmentTransaction.replace(R.id.fragment_container, editIncomeFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
        else{
            bundle.putString("expense_id", transaction.id.toString())
            bundle.putString("category_id", transaction.categoryId.toString())

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