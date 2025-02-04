package com.vibhu.moneyplanner

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vibhu.moneyplanner.databinding.ActivityCategoryExpensesPieChartBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.Calendar
import java.util.Date
import java.util.UUID

class CategoryExpensesPieChartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryExpensesPieChartBinding
    private lateinit var expenseData: ExpenseData
    private lateinit var categoryData: CategoryData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCategoryExpensesPieChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        expenseData = ExpenseData(this)
        categoryData = CategoryData(this)

        val endDate = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = calendar.time

        val expenses = expenseData.getExpensesInDateRange(startDate, endDate)
        val categoryExpenses = aggregateExpensesByCategory(expenses)

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        categoryExpenses.forEach { (categoryId, totalExpense) ->
            val category = categoryData.getCategoryById(categoryId)
            if (category != null) {
                entries.add(PieEntry(totalExpense.toFloat(), category.name))
                colors.add(generateRandomColor()) // Generate random colors for each category
            }
        }
        categoryData.close()

        val dataSet = PieDataSet(entries, "Expenses by Category")
        dataSet.colors = colors

        val pieData = PieData(dataSet)
        binding.pieChartExpenses.data = pieData

        // Customize the chart (optional)
        binding.pieChartExpenses.description.isEnabled = false
        binding.pieChartExpenses.setUsePercentValues(true)
        binding.pieChartExpenses.setDrawHoleEnabled(true)
        binding.pieChartExpenses.setHoleRadius(70f)
        binding.pieChartExpenses.setTransparentCircleRadius(80f)

        binding.pieChartExpenses.invalidate() // Refresh the chart
    }

    private fun aggregateExpensesByCategory(expenses: List<Expense>): Map<UUID, Double> {
        val categoryExpenses = mutableMapOf<UUID, Double>()
        for (expense in expenses) {
            val categoryId = expense.categoryId
            val amount = expense.expenseAmount
            categoryExpenses[categoryId] = (categoryExpenses[categoryId] ?: 0.0) + amount
        }
        return categoryExpenses
    }

    private fun generateRandomColor(): Int {
        val rnd = java.util.Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    override fun onDestroy() {
        super.onDestroy()
        expenseData.close()
    }
}