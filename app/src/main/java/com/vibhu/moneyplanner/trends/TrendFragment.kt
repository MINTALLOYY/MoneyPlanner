package com.vibhu.moneyplanner.trends

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
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

        val tabLayout = binding.tabLayout // Assuming you have a TabLayout in your layout (see XML below)

        tabLayout.addTab(tabLayout.newTab().setText("Expenses"))
        tabLayout.addTab(tabLayout.newTab().setText("Income"))

        // Set a default tab (optional - you could just show the Expenses tab first in the XML)
        showExpensesFragment()


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
    }

    private fun showExpensesFragment() {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.trends_fragment_container, ExpensesTrendsFragment())
        fragmentTransaction.commit()
    }

    private fun showIncomeFragment() {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.trends_fragment_container, IncomeTrendsFragment())
        fragmentTransaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}