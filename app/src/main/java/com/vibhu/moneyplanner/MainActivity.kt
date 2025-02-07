package com.vibhu.moneyplanner

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.vibhu.moneyplanner.categoryexpense.HomeFragment
import com.vibhu.moneyplanner.databinding.ActivityMainBinding
import com.vibhu.moneyplanner.models.Income
import com.vibhu.moneyplanner.models.IncomeCategory
import com.vibhu.moneyplanner.trends.TrendFragment
import java.util.Date
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var incomeCategoryData: IncomeCategoryData
    private lateinit var incomeData: IncomeData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomeCategoryData = IncomeCategoryData(this)
        incomeData = IncomeData(this)

        if(isFirstRun()){
            showBalanceDialog()
        } else {
            setUpBottomNavigation()
            setCurrentFragment(HomeFragment())
        }
    }

    fun setUpBottomNavigation(){
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

    private fun showBalanceDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Welcome!")
        builder.setMessage("Please enter your current account balance:")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val balanceStr = input.text.toString()

            if (balanceStr.isBlank()) {
                Toast.makeText(this, "Please enter a balance", Toast.LENGTH_SHORT).show()
                showBalanceDialog() // Show the dialog again
                return@setPositiveButton
            }

            try {
                val balance = balanceStr.toDouble()

                // 1. Add "Initial Balance" Income Category (if it doesn't exist)
                val initialCategory = incomeCategoryData.getIncomeCategoryByName("Initial Balance")
                val categoryId = if (initialCategory == null) {
                    val newCategory = IncomeCategory(UUID.randomUUID(), "Initial Balance")
                    incomeCategoryData.addIncomeCategory(newCategory)
                    newCategory.incomeCategoryId
                } else {
                    initialCategory.incomeCategoryId
                }

                // 2. Log the balance as an income entry
                val currentDate = Date()
                val newIncome = Income(UUID.randomUUID(), balance, categoryId, currentDate)
                incomeData.addIncome(newIncome)

                // Set first run to false
                setFirstRun(false)

                // Resume Main Activity
                setUpBottomNavigation()
                setCurrentFragment(HomeFragment())

            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Invalid balance", Toast.LENGTH_SHORT).show()
                showBalanceDialog() // Show the dialog again
            }
        }

        builder.setCancelable(false) // Make the dialog non-cancelable (optional)
        builder.show()
    }

    private fun isFirstRun(): Boolean {
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("isFirstRun", true)
    }

    private fun setFirstRun(isFirstRun: Boolean) {
        val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("isFirstRun", isFirstRun).apply()
    }


    private fun setCurrentFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        incomeCategoryData.close()
        incomeData.close()
    }
}