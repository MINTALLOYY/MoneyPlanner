package com.vibhu.moneyplanner.trends

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vibhu.moneyplanner.Expense
import com.vibhu.moneyplanner.categoryexpense.ExpenseData
import com.vibhu.moneyplanner.databinding.FragmentExpensesTrendsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TreeMap

class ExpensesTrendsFragment: Fragment() {

    private var _binding: FragmentExpensesTrendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expenseData = ExpenseData(requireContext())
        setUpBarChart()
    }

    private fun setUpBarChart() {
        val barChart: BarChart = binding.barChartExpenses // Assuming you have a BarChart in your layout
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        val endDate = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -12)
        val startDate = calendar.time

        val expenses = expenseData.getExpensesInDateRange(startDate, endDate)
        val monthlyExpenses = aggregateExpensesByMonth(expenses)

        val format = SimpleDateFormat("MMM", Locale.getDefault())

        var x = 0f // X-axis counter
        monthlyExpenses.forEach { (month, totalExpense) ->
            val monthLabel = format.format(month)
            labels.add(monthLabel)
            entries.add(BarEntry(x, totalExpense.toFloat()))
            x++
        }

        val dataSet = BarDataSet(entries, "Monthly Expenses")
        dataSet.color = Color.BLUE
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barChart.data = barData

        // Chart customizations
        configureChart(barChart, labels)
        barChart.invalidate() // Refresh the chart
    }

    private fun configureChart(chart: BarChart, labels: List<String>) {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        chart.setDoubleTapToZoomEnabled(false)

        // X-axis
        val xAxis = chart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelCount = labels.size
        xAxis.textColor = Color.BLACK
        xAxis.granularity = 1f
        xAxis.textSize = 12f

        // Y-axis
        val yAxisLeft = chart.axisLeft
        yAxisLeft.textColor = Color.BLACK
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.valueFormatter = object: ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.0f", value)
            }
        }

        val yAxisRight = chart.axisRight
        yAxisRight.setDrawGridLines(false) // Only left Y-axis has grid lines
        yAxisRight.isEnabled = false // Right Y-axis is not needed

        chart.legend.textColor = Color.BLACK
    }

    private fun aggregateExpensesByMonth(expenses: List<Expense>): TreeMap<Date, Double> {
        val monthlyExpenses = TreeMap<Date, Double>()
        val calendar = Calendar.getInstance()

        for (expense in expenses) {
            calendar.time = expense.expenseDate
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val month = calendar.time
            val amount = expense.amount
            monthlyExpenses[month] = (monthlyExpenses[month]?: 0.0) + amount
        }
        return monthlyExpenses
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        expenseData.close()
    }
}