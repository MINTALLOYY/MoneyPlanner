package com.vibhu.moneyplanner

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.textract.AmazonTextract
import com.amazonaws.services.textract.AmazonTextractClient
import com.vibhu.moneyplanner.constants.SharedPreferencesConstants
import com.vibhu.moneyplanner.database.IncomeCategoryData
import com.vibhu.moneyplanner.database.IncomeData
import com.vibhu.moneyplanner.database.InitialBalanceData
import com.vibhu.moneyplanner.uiFragments.CategoriesFragment
import com.vibhu.moneyplanner.databinding.ActivityMainBinding
import com.vibhu.moneyplanner.models.InitialBalance
import com.vibhu.moneyplanner.trends.PieChartExpenseFragment
import com.vibhu.moneyplanner.uiFragments.TrendFragment
import com.vibhu.moneyplanner.uiFragments.ChatBotQAFragment
import com.vibhu.moneyplanner.uiFragments.HomeFragment
import com.vibhu.moneyplanner.uiFragments.IncomeCategoryFragment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Calendar
import java.util.UUID
import kotlin.text.isNotEmpty
import kotlin.text.toDouble

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var incomeCategoryData: IncomeCategoryData
    private lateinit var incomeData: IncomeData
    private lateinit var initialBalanceData: InitialBalanceData
    private lateinit var textractManager: TextractManager
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        incomeCategoryData = IncomeCategoryData(this)
        incomeData = IncomeData(this)
        initialBalanceData = InitialBalanceData(this)

        sharedPreferences = getSharedPreferences(SharedPreferencesConstants.NAME, Context.MODE_PRIVATE)


        if(isFirstRun()){
            showBalanceDialog()
        }
        else {
            try{
                val initialBalance = initialBalanceData.fetchInitialBalanceObject(UUID.fromString(sharedPreferences.getString(
                    SharedPreferencesConstants.USER_ID_PREF, null)))!!
                Log.d("Initial Date", initialBalance.initialDate.toString())
                setUpBottomNavigation()
                setCurrentFragment(HomeFragment())
            } catch(e: NullPointerException){
                showBalanceDialog()
            }

        }

    }

    fun setUpBottomNavigation(){
        val categoriesFragment = CategoriesFragment()
        val pieChartExpenseFragment = PieChartExpenseFragment()
        val trendFragment = TrendFragment()

        setCurrentFragment(categoriesFragment) // Set initial fragment

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    setCurrentFragment(HomeFragment())
                    true
                }
                R.id.navigation_categories -> {
                    setCurrentFragment(categoriesFragment)
                    true
                }
//                R.id.navigation_pie_chart -> {
//                    setCurrentFragment(pieChartFragment)
//                    true
//                }
                R.id.navigation_trend -> {
                    setCurrentFragment(trendFragment)
                    true
                }
                R.id.navigation_income_categories -> {
                    setCurrentFragment(IncomeCategoryFragment())
                    true
                }
                R.id.navigation_chatbot -> {
                    setCurrentFragment(ChatBotQAFragment())
                    true
                }
//                R.id.navigation_camera_test -> {
//                    setCurrentFragment(CameraReceiptFragment())
//                    true
//                }
                else -> false
            }
        }
    }

    private fun showBalanceDialog() {
        // Set first run to false
        setFirstRun(false)
        Log.d("First Run", isFirstRun().toString())

        // Get the initial balance of the user
        val input = EditText(this)
        input.hint = "100.00"

        initialBalanceData.clearAllInitialBalanceData()

        AlertDialog.Builder(this)
            .setTitle("Enter Your Initial Balance")
            .setMessage("Enter your initial balance to track how much money you have while using the app")
            .setView(input)
            .setPositiveButton("Save") { dialog, _ ->
                val balanceText = input.text.toString()
                if(balanceText.isNotEmpty()) {
                    try {
                        val balance = balanceText.toDouble()

                        val calendar = Calendar.getInstance()
                        calendar.set(2024, Calendar.MARCH, 31)
                        val date = calendar.time
                        Log.d("date", date.toString())

                        val initialBalance = InitialBalance(balance, date)

                        addUserId(initialBalance.userId)

                        initialBalanceData.addInitialBalanceData(initialBalance)
                        Log.d("Initial Balance", "Initial balance saved: $balance")
                        Log.d("User ID Saved", "User ID saved: ${initialBalance.userId}")
                    } catch (e: NumberFormatException) {
                        Log.e("showBalanceDialog", "Invalid number format", e)
                    }
                }
                dialog.dismiss()
            }
            .show()

        // Resume Main Activity
        setUpBottomNavigation()
        setCurrentFragment(CategoriesFragment())
    }

    private fun isFirstRun(): Boolean {
        val sharedPrefs = getSharedPreferences(SharedPreferencesConstants.NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(SharedPreferencesConstants.FIRST_RUN, true)
    }

    private fun setFirstRun(isFirstRun: Boolean) {
        val sharedPrefs = getSharedPreferences(SharedPreferencesConstants.NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(SharedPreferencesConstants.FIRST_RUN, isFirstRun).apply()
    }

    private fun addUserId(userId: UUID) {
        sharedPreferences.edit().remove(SharedPreferencesConstants.USER_ID_PREF).apply()
        sharedPreferences.edit().putString(SharedPreferencesConstants.USER_ID_PREF, userId.toString()).apply()
    }

    private fun setCurrentFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun testTextractWithLocalFile() {

        val textractClient: AmazonTextract by lazy {
            val credentialsProvider = CognitoCachingCredentialsProvider(
                this,
                "us-east-1:250834a1-31f1-4bff-a8ac-adfd1484a595", // Identity Pool ID
                Regions.US_EAST_1
            )
            AmazonTextractClient(credentialsProvider)
        }
        textractManager = TextractManager(textractClient)

        val inputStream: InputStream = assets.open("costcoReceipt.jpg")
        val tempFile = File.createTempFile("costcoReceipt", ".jpg", cacheDir)
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        textractManager.analyzeDocument(tempFile) { extractedName, extractedDate, extractedTotal, error ->
            if (extractedTotal != null) {
                Log.d("Textract Result", "Extracted Total: $extractedTotal" + " Extracted Name: $extractedName" + " Extracted Date: $extractedDate")
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
        initialBalanceData.close()
    }
}