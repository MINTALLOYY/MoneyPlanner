package com.vibhu.moneyplanner

import android.icu.util.Calendar
import android.os.Bundle
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

        var sharedPreferences = (requireActivity() as MainActivity).sharedPreferences
        var userId = UUID.fromString(sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null))

        getCurrentBalance()
        binding.currentBalance.text = currentBalance.toString()

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
        for(income in incomeBalanceList){
            incomeBalance += income.amount
        }
        for(expense in expenseBalanceList){
            expenseBalance += expense.amount
        }
        currentBalance = incomeBalance - expenseBalance + initialBalanceData.fetchInitialBalance(UUID.fromString((requireActivity() as MainActivity).sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null)))!!
        return currentBalance

    }

}