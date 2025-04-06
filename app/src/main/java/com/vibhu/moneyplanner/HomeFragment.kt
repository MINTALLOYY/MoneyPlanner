package com.vibhu.moneyplanner

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.add
import androidx.compose.ui.tooling.data.position
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.add
import com.vibhu.moneyplanner.categoryexpense.ExpenseData
import com.vibhu.moneyplanner.databinding.FragmentHomeBinding
import com.vibhu.moneyplanner.models.InitialBalance
import com.vibhu.moneyplanner.models.Transaction
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
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
        // Todo: Add transaction history to recycler view
        // Todo: Create recycler view adapter for transaction history
        // Todo: Create layouts for transaction details with date, amount, type, edit, and delete

        setFragment(WeeklyFragment())
        binding.weeklyMonthlyChanger.setOnClickListener{
            changeFragment()
        }
    }

    fun setFragment(graphFragment: Fragment){
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(binding.balanceChart.id, graphFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun changeFragment(){
        if(binding.weeklyMonthlyChanger.text == "Change to Monthly"){
            setFragment(MonthlyFragment())
            binding.weeklyMonthlyChanger.text = "Change to Weekly"
        }
        else{
            setFragment(WeeklyFragment())
            binding.weeklyMonthlyChanger.text = "Change to Monthly"
        }
    }

    fun getCurrentBalance(): Double{
        val incomeBalanceList = incomeData.getIncomesInDateRange(initialBalanceData.fetchInitialDate(UUID.fromString((requireActivity() as MainActivity).sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null)))!!, Date())
        val expenseBalanceList = expenseData.getExpensesInDateRange(initialBalanceData.fetchInitialDate(UUID.fromString((requireActivity() as MainActivity).sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null)))!!, Date())
        var incomeBalance = 0.0
        var expenseBalance = 0.0
        if(incomeBalanceList.isNotEmpty() && expenseBalanceList.isNotEmpty()){
            for(income in incomeBalanceList){
                incomeBalance += income.amount
            }
            for(expense in expenseBalanceList){
                expenseBalance += expense.amount
            }
            currentBalance = incomeBalance - expenseBalance + initialBalance!!
        }
        else{
            currentBalance = 0.0 + initialBalance!!
        }
        return currentBalance

    }

    fun getTransactionHistory(): List<Transaction>{

        val allTransactions = (incomeData.getAllIncomes().map {
            Transaction(it.amount, it.receivedDate, true, it.incomeLogName, it.incomeId)
        } + expenseData.getAllExpenses().map {
            Transaction(it.amount, it.expenseDate, false, it.name, it.expenseId)
        }).sortedBy { it.date }

        transactionHistoryList = allTransactions

        return allTransactions
    }

    fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun LocalDate.toDate(): Date {
        return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        expenseData.close()
        incomeData.close()
        initialBalanceData.close()
    }

}