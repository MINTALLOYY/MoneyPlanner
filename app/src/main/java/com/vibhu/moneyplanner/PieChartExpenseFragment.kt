package com.vibhu.moneyplanner

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.vibhu.moneyplanner.categoryexpense.CategoryData
import com.vibhu.moneyplanner.categoryexpense.ExpenseData
import com.vibhu.moneyplanner.databinding.FragmentPieChartBinding
import java.util.Calendar
import java.util.Date
import java.util.UUID

class PieChartExpenseFragment : Fragment() {

    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData
    private lateinit var categoryData: CategoryData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
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
                entries.add(PieEntry(totalExpense.toFloat(), category.categoryName))
                colors.add(generateRandomColor())
            }
        }
        categoryData.close()

        val dataSet = PieDataSet(entries, "Expenses by Category (Last 30 Days)") // More descriptive label
        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE // Set value text color to white for visibility
        dataSet.valueTextSize = 14f // Set value text size for better readability

        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData

        // Customize the chart (optional)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.setDrawHoleEnabled(true)
        binding.pieChart.setHoleRadius(60f) // Adjust hole radius
        binding.pieChart.setTransparentCircleColor(Color.BLACK) // Adjust transparent circle radius
        binding.pieChart.legend.textColor = Color.BLACK // Set legend text color to white
        binding.pieChart.setEntryLabelColor(Color.WHITE) // Set entry label text color

        binding.pieChart.invalidate() // Refresh the chart
        return view
    }



    private fun aggregateExpensesByCategory(expenses: List<Expense>): Map<UUID, Double> {
        val categoryExpenses = mutableMapOf<UUID, Double>()
        for (expense in expenses) {
            val categoryId = expense.categoryId
            val amount = expense.amount
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