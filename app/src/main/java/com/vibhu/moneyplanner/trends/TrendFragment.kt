package com.vibhu.moneyplanner.trends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.vibhu.moneyplanner.PieChartExpenseFragment
import com.vibhu.moneyplanner.PieChartIncomeFragment
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.databinding.FragmentTrendBinding // Your binding class

class TrendFragment : Fragment() {

    private var _binding: FragmentTrendBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("OnCreateView", "onCreateView called in TrendFragment")
        _binding = FragmentTrendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val expenseIncomeTabLayout = binding.tabLayout

        // Set the text fo the Tab Layout
        expenseIncomeTabLayout.addTab(expenseIncomeTabLayout.newTab().setText("Expenses"))
        expenseIncomeTabLayout.addTab(expenseIncomeTabLayout.newTab().setText("Income"))

        // Set a default tab
        showExpensesFragment()


        // Setup up the Tab for switching in between expenses and incomes
        expenseIncomeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    showExpensesFragment()
                } else {
                    showIncomeFragment()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Set the button and the list of possible values
        val pieTimeButton: Button = binding.pieTimeChanger
        val listOfDays = mutableListOf("Last 7 days", "Last 30 days", "Last 365 days", "All Time")


        // Update the Pie Charts based on the time displayed on the text
        pieTimeButton.setOnClickListener {
            val currentText = pieTimeButton.text.toString()
            val currentIndex = listOfDays.indexOf(currentText)
            val nextIndex = (currentIndex + 1) % listOfDays.size
            pieTimeButton.text = listOfDays[nextIndex]
            val numberOfDays = when (listOfDays[nextIndex]) {
                "Last 7 days" -> 7
                "Last 30 days" -> 30
                "Last 365 days" -> 365
                "All Time" -> Int.MAX_VALUE
                else -> 30
            }
            updatePieChart(numberOfDays, tabIsOnIncome())
        }

        // Set the button and the list of possible values
        val trendsTimeButton: Button = binding.trendsTimeChanger
        val listOfTimePeriods = mutableListOf("Monthly", "Yearly")

        // Default to Monthly
        trendsTimeButton.text = listOfTimePeriods[0]

        // Update the bar charts based on the time displayed on the text
        trendsTimeButton.setOnClickListener {
            val currentText = trendsTimeButton.text.toString()
            val currentIndex = listOfTimePeriods.indexOf(currentText)
            val nextIndex = (currentIndex + 1) % listOfTimePeriods.size
            trendsTimeButton.text = listOfTimePeriods[nextIndex]
            updateTrendsFragment(listOfTimePeriods[nextIndex], tabIsOnIncome())
        }

    }

    private fun updatePieChart(numberOfDays: Int, isIncome: Boolean) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        val pieChartFragment = if (isIncome) PieChartIncomeFragment() else PieChartExpenseFragment()


        val bundle = Bundle()
        bundle.putString("numberOfDays", numberOfDays.toString())
        pieChartFragment.arguments = bundle


        fragmentTransaction.replace(R.id.trends_pie_chart, pieChartFragment)
        fragmentTransaction.commit()
    }


    private fun updateTrendsFragment(timePeriod: String, isIncome: Boolean) {
        val fragmentTransaction = childFragmentManager.beginTransaction()


        val trendsFragment = if (isIncome) IncomeTrendsFragment() else ExpensesTrendsFragment()
        val bundle = Bundle()
        bundle.putString("timePeriod", timePeriod)
        trendsFragment.arguments = bundle


        fragmentTransaction.replace(R.id.trends_fragment_container, trendsFragment)
        fragmentTransaction.commit()
    }

    private fun tabIsOnIncome(): Boolean {
        val tabLayout = binding.tabLayout
        if (tabLayout.selectedTabPosition == 0) {
            return false
        } else {
            return true
        }
    }

    private fun showExpensesFragment() {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.trends_fragment_container, ExpensesTrendsFragment())
        fragmentTransaction.replace(R.id.trends_pie_chart, PieChartExpenseFragment())
        fragmentTransaction.commit()
    }

    private fun showIncomeFragment() {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.trends_fragment_container, IncomeTrendsFragment())
        fragmentTransaction.replace(R.id.trends_pie_chart, PieChartIncomeFragment())
        fragmentTransaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}