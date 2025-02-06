package com.vibhu.moneyplanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.vibhu.moneyplanner.categoryexpense.HomeFragment
import com.vibhu.moneyplanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val homeFragment = HomeFragment()
        val pieChartFragment = PieChartFragment()
        val trendFragment = TrendFragment()

        setCurrentFragment(homeFragment) // Set initial fragment

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    setCurrentFragment(homeFragment)
                    true
                }
                R.id.navigation_pie_chart -> {
                    setCurrentFragment(pieChartFragment)
                    true
                }
                R.id.navigation_trend -> {
                    setCurrentFragment(trendFragment)
                    true
                }
                R.id.navigation_income_categories -> {
                    setCurrentFragment(IncomeCategoryFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun setCurrentFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}