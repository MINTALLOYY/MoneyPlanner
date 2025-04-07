package com.vibhu.moneyplanner

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import java.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.compose.foundation.layout.add
import androidx.compose.ui.tooling.data.position
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.add
import androidx.recyclerview.widget.LinearLayoutManager
import com.vibhu.moneyplanner.categoryexpense.ExpenseData
import com.vibhu.moneyplanner.databinding.FragmentHomeBinding
import com.vibhu.moneyplanner.models.InitialBalance
import com.vibhu.moneyplanner.models.Transaction
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.text.format
import kotlin.text.toFloat
import kotlin.text.withIndex

class HomeFragment: Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeData: IncomeData
    private lateinit var expenseData: ExpenseData
    private lateinit var initialBalanceData: InitialBalanceData
    private var currentBalance: Double = 0.0
    private lateinit var transactionHistoryList: List<Transaction>
    private var initialBalance: Double? = 0.0
    private lateinit var transactionAdapter: TransactionHistoryAdapter
    private lateinit var transactionData: TransactionData
    private lateinit var searchListAdapter: SearchListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeData = IncomeData(requireContext())
        expenseData = ExpenseData(requireContext())
        initialBalanceData = InitialBalanceData(requireContext())
        transactionData = TransactionData(requireContext())

        binding.transactionHistory.layoutManager = LinearLayoutManager(requireContext())

        val searchView = binding.searchTransaction
        searchView.clearFocus()

        val userUUIDStr = (requireActivity() as MainActivity).sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null)
        Log.d("userUUIDStr", "" + userUUIDStr)
        val balance = initialBalanceData.fetchInitialBalance(UUID.fromString(userUUIDStr))!!
        Log.d("balance", "" + balance)

        initialBalance = 0.0 + balance

        var sharedPreferences = (requireActivity() as MainActivity).sharedPreferences
        var userId = UUID.fromString(sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null))

        getCurrentBalance()
        binding.currentBalance.text = currentBalance.toString()
        Log.d("currentBalance", "" + currentBalance)

        getTransactionHistory()

        transactionAdapter = TransactionHistoryAdapter(
            transactionHistoryList,
            requireContext(),
            { transaction ->
                navigateToTransactionDetails(transaction)
            }
        )

        binding.transactionHistory.adapter = transactionAdapter
        transactionAdapter.updateItems(transactionData.getAllTransaction())

        searchListAdapter = SearchListAdapter(
            emptyList(),
            requireContext()
        ) { transaction ->
            navigateToTransactionDetails(transaction)
            hideSearchResults()
            binding.searchTransaction.clearFocus()
            hideKeyboard()

        }
        setupSearch()

        setFragment(WeeklyFragment())
        binding.weeklyMonthlyChanger.setOnClickListener{
            changeFragment()
        }
    }

    private fun setupSearch() {
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchListAdapter
        }

        // Handle search queries
        binding.searchTransaction.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    hideSearchResults()
                } else {
                    filterTransactions(newText)
                }
                return true
            }
        })

        // Handle click outside
        binding.root.setOnClickListener {
            if (binding.searchResultsRecyclerView.visibility == View.VISIBLE) {
                hideSearchResults()
            }
        }
    }

    private fun filterTransactions(query: String) {
        val normalizedQuery = query.lowercase(Locale.getDefault()).trim()

        val filtered = transactionHistoryList
            .filter { transaction ->
                transaction.transactionName.lowercase(Locale.getDefault()).contains(normalizedQuery)
            }
            .sortedWith(
                compareByDescending<Transaction> {
                    // Exact matches first
                    it.transactionName.lowercase(Locale.getDefault()) == normalizedQuery
                }.thenByDescending {
                    // Then by date (newest first)
                    it.date
                }
            )

        searchListAdapter.updateItems(filtered)

        if (filtered.isNotEmpty()) {
            showSearchResults()
        } else {
            hideSearchResults()
        }
    }

    private fun showSearchResults() {
        binding.searchResultsRecyclerView.visibility = View.VISIBLE
        // Add fade animation
        binding.searchResultsRecyclerView.alpha = 0f
        binding.searchResultsRecyclerView.animate()
            .alpha(1f)
            .setDuration(200)
            .start()
    }

    private fun hideKeyboard() {
        val activity = requireActivity()
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocus = activity.currentFocus ?: binding.root
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        binding.searchTransaction.clearFocus()
    }

    private fun hideSearchResults() {
        binding.searchResultsRecyclerView.visibility = View.GONE
        binding.searchTransaction.clearFocus()
        hideKeyboard()
        Log.e("hideSearchResults", "hideSearchResults called")

    }

    private fun setFragment(graphFragment: Fragment){
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(binding.balanceChart.id, graphFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun changeFragment(){
        if(binding.weeklyMonthlyChanger.text == "Change to Monthly"){
            setFragment(MonthlyFragment())
            binding.weeklyMonthlyChanger.text = "Change to Weekly"
        }
        else{
            setFragment(WeeklyFragment())
            binding.weeklyMonthlyChanger.text = "Change to Monthly"
        }
    }

    private fun getCurrentBalance(): Double{
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_WEEK, -7)
        val lastWeek = cal.time

        // val initial = initialBalanceData.fetchInitialDate(UUID.fromString((requireActivity() as MainActivity).sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null)))!!

        val incomeBalanceList = incomeData.getIncomesInDateRange(lastWeek, Date())
        val expenseBalanceList = expenseData.getExpensesInDateRange(lastWeek, Date())

        var incomeBalance = 0.0
        var expenseBalance = 0.0
        if(incomeBalanceList.isNotEmpty() && expenseBalanceList.isNotEmpty()){
            for(income in incomeBalanceList){
                incomeBalance += income.amount
            }
            for(expense in expenseBalanceList){
                expenseBalance += expense.amount
            }
            Log.d("expenseBalance", expenseBalance.toString())
            Log.d("incomeBalance", incomeBalance.toString())
            currentBalance = incomeBalance - expenseBalance + initialBalance!!
        }
        else{
            currentBalance = 0.0 + initialBalance!!
        }
        return currentBalance

    }

    private fun getTransactionHistory(): List<Transaction>{

        val allTransactions = transactionData.getAllTransaction()

        transactionHistoryList = allTransactions

        return allTransactions
    }

    private fun navigateToTransactionDetails(transaction: Transaction){
        val bundle = Bundle()
        bundle.putString("transaction_id", transaction.id.toString())

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val transactionDetailsFragment = TransactionDetailsFragment()
        transactionDetailsFragment.arguments = bundle

        fragmentTransaction.replace(R.id.fragment_container, transactionDetailsFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        expenseData.close()
        incomeData.close()
        initialBalanceData.close()
    }

}