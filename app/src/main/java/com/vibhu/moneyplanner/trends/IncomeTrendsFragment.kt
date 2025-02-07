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
import com.vibhu.moneyplanner.IncomeData
import com.vibhu.moneyplanner.databinding.FragmentIncomeTrendsBinding
import com.vibhu.moneyplanner.models.Income
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TreeMap

class IncomeTrendsFragment : Fragment() {

    private var _binding: FragmentIncomeTrendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeData: IncomeData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeData = IncomeData(requireContext())
        setUpBarChart()
    }

    private fun setUpBarChart() {
        val barChart: BarChart = binding.barChartIncome
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        val endDate = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -12)
        val startDate = calendar.time

        val incomes = incomeData.getAllIncomes().filter { it.receivedDate in startDate..endDate }
        val monthlyIncomes = aggregateIncomesByMonth(incomes)

        val format = SimpleDateFormat("MMM", Locale.getDefault())

        var x = 0f
        monthlyIncomes.forEach { (month, totalIncome) ->
            val monthLabel = format.format(month)
            labels.add(monthLabel)
            entries.add(BarEntry(x, totalIncome.toFloat()))
            x++
        }

        val dataSet = BarDataSet(entries, "Monthly Income")
        dataSet.color = Color.GREEN
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barChart.data = barData

        configureChart(barChart, labels)
        barChart.invalidate()
    }

    private fun configureChart(chart: BarChart, labels: List<String>) {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        chart.setDoubleTapToZoomEnabled(false)

        val xAxis = chart.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelCount = labels.size
        xAxis.textColor = Color.WHITE
        xAxis.granularity = 1f
        xAxis.textSize = 12f

        val yAxisLeft = chart.axisLeft
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("%.0f", value)
            }
        }

        val yAxisRight = chart.axisRight
        yAxisRight.setDrawGridLines(false)
        yAxisRight.isEnabled = false

        chart.legend.textColor = Color.WHITE
    }


    private fun aggregateIncomesByMonth(incomes: List<Income>): TreeMap<Date, Double> {
        val monthlyIncomes = TreeMap<Date, Double>()
        val calendar = Calendar.getInstance()

        for (income in incomes) {
            calendar.time = income.receivedDate
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val month = calendar.time
            val amount = income.amount
            monthlyIncomes[month] = (monthlyIncomes[month] ?: 0.0) + amount
        }
        return monthlyIncomes
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        incomeData.close()
    }
}