package com.vibhu.moneyplanner

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vibhu.moneyplanner.databinding.FragmentTrendBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class TrendFragment : Fragment() {

    private var _binding: FragmentTrendBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrendBinding.inflate(inflater, container, false)
        val view = binding.root

        expenseData = ExpenseData(requireContext())

        val endDate = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -12)
        val startDate = calendar.time

        val expenses = expenseData.getExpensesInDateRange(startDate, endDate)
        val monthlyExpenses = aggregateExpensesByMonth(expenses)

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

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
        binding.barChartExpenses.data = barData

        // Chart customizations
        binding.barChartExpenses.description.isEnabled = false
        binding.barChartExpenses.setDrawGridBackground(false)
        binding.barChartExpenses.setDrawBarShadow(false)
        binding.barChartExpenses.setDrawValueAboveBar(true)
        binding.barChartExpenses.setPinchZoom(false)
        binding.barChartExpenses.setScaleEnabled(false)
        binding.barChartExpenses.setDoubleTapToZoomEnabled(false)

        // X-axis
        val xAxis = binding.barChartExpenses.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelCount = labels.size
        xAxis.textColor = Color.BLACK
        xAxis.granularity = 1f
        xAxis.textSize = 24f // Set x-axis label text size (adjust as needed)


        // Y-axis
        val yAxisLeft = binding.barChartExpenses.axisLeft
        yAxisLeft.textColor = Color.BLACK
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.0f", value)
            }
        }

        val yAxisRight = binding.barChartExpenses.axisRight
        yAxisRight.textColor = Color.BLACK
        yAxisRight.setDrawGridLines(true)
        yAxisRight.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.0f", value)
            }
        }

        binding.barChartExpenses.legend.textColor = Color.BLACK

        binding.barChartExpenses.invalidate()

        return view
    }

    private fun aggregateExpensesByMonth(expenses: List<Expense>): SortedMap<Date, Double> {
        val monthlyExpenses = sortedMapOf<Date, Double>()
        val calendar = Calendar.getInstance()

        for (expense in expenses) {
            calendar.time = expense.expenseDate
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val month = calendar.time
            val amount = expense.expenseAmount
            monthlyExpenses[month] = (monthlyExpenses[month] ?: 0.0) + amount
        }
        return monthlyExpenses
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        expenseData.close()
    }
}