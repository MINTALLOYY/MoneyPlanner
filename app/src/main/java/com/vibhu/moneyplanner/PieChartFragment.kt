package com.vibhu.moneyplanner

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.databinding.ActivityCategoryExpensesPieChartBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.Calendar
import java.util.Date
import java.util.UUID

class PieChartFragment : Fragment() {

    private var _binding: ActivityCategoryExpensesPieChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData
    private lateinit var categoryData: CategoryData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityCategoryExpensesPieChartBinding.inflate(inflater, container, false)
        val view = binding.root

        expenseData = ExpenseData(requireContext())
        categoryData = CategoryData(requireContext())

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
                colors.add(generateRandomColor())
            }
        }
        categoryData.close()

        val dataSet = PieDataSet(entries, "Expenses by Category (Last 30 Days)") // More descriptive label
        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE // Set value text color to white for visibility
        dataSet.valueTextSize = 14f // Set value text size for better readability

        val pieData = PieData(dataSet)
        binding.pieChartExpenses.data = pieData

        // Customize the chart (optional)
        binding.pieChartExpenses.description.isEnabled = false
        binding.pieChartExpenses.setUsePercentValues(true)
        binding.pieChartExpenses.setDrawHoleEnabled(true)
        binding.pieChartExpenses.setHoleRadius(60f) // Adjust hole radius
        binding.pieChartExpenses.setTransparentCircleRadius(70f) // Adjust transparent circle radius
        binding.pieChartExpenses.legend.textColor = Color.BLACK // Set legend text color to white
        binding.pieChartExpenses.setEntryLabelColor(Color.WHITE) // Set entry label text color

        binding.pieChartExpenses.invalidate() // Refresh the chart
        return view
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        expenseData.close()
    }
}