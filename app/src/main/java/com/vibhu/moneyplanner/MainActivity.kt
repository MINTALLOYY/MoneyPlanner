package com.vibhu.moneyplanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Date
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var incomeCategoryData: IncomeCategoryData
    private lateinit var incomeData: IncomeData
    private lateinit var textractManager: TextractManager


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
                // Set first run to false
                setFirstRun(false)

                // Resume Main Activity
                setUpBottomNavigation()
                setCurrentFragment(HomeFragment())
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

    private fun testTextractWithLocalFile() {

        val inputStream: InputStream = assets.open("receipt.jpg")
        val tempFile = File.createTempFile("receipt", ".jpg", cacheDir)
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        textractManager.analyzeDocument(tempFile) { extractedText, error ->
            if (extractedText != null) {
                Log.d("Textract Result", extractedText)
                Log.d("TESTING", "TESTING TESTING TESTING")
                // Display text in your UI
            } else if (error != null) {
                Log.e("Textract Error", error.message.toString())
                // Handle error
            }
            Log.d("Analyzing Over", "ANALYZING IS OVER")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        incomeCategoryData.close()
        incomeData.close()
    }
}