package com.vibhu.moneyplanner.trends

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vibhu.moneyplanner.IncomeData
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.databinding.FragmentBarChartTrendsBinding
import com.vibhu.moneyplanner.models.Income
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TreeMap
import kotlin.math.ceil

class IncomeTrendsFragment : Fragment() {

    private var _binding: FragmentBarChartTrendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeData: IncomeData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarChartTrendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeData = IncomeData(requireContext())
        setUpBarChart()
    }

    private fun setUpBarChart() {
        val barChart: BarChart = binding.barChart
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
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.green)
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

        // Y-axis (Left) - Auto-scaling with 10% margin
        val yAxisLeft = chart.axisLeft.apply {
            textColor = Color.WHITE
            setDrawGridLines(true)

            axisMinimum = 0f

            // Calculate rounded maximum with 10% margin
            val rawMax = chart.yMax * 1.1f
            axisMaximum = calculateSmartMax(rawMax)

            // Get the optimal step size
            val stepSize = calculateStepSize(axisMaximum)
            setLabelCount((axisMaximum / stepSize).toInt() + 1, true)

            // Format labels without decimals
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value % stepSize == 0f) String.format("%.0f", value) else ""
                }
            }
        }

        // Disable right Y-axis
        chart.axisRight.isEnabled = false

        // Refresh chart
        chart.invalidate()
        yAxisLeft.axisMinimum = 0f // Force Y-axis to start at zero

        // Y-axis (Right) - Disabled
        val yAxisRight = chart.axisRight
        yAxisRight.setDrawGridLines(false)
        yAxisRight.isEnabled = false

        // Additional chart settings
        chart.legend.textColor = Color.WHITE
        chart.setExtraOffsets(0f, 0f, 0f, 10f) // Add padding if needed
        chart.animateY(1000) // Smooth animation
    }

    private fun calculateSmartMax(rawMax: Float): Float {
        return when {
            rawMax <= 100 -> ceil(rawMax / 20f) * 20f  // Small values: steps of 20
            rawMax <= 500 -> ceil(rawMax / 50f) * 50f  // Medium values: steps of 50
            rawMax <= 2000 -> ceil(rawMax / 100f) * 100f  // Large values: steps of 100
            else -> ceil(rawMax / 200f) * 200f  // Very large values: steps of 200
        }
    }

    // Determines optimal step size based on max value
    private fun calculateStepSize(max: Float): Float {
        return when {
            max <= 100 -> 20f
            max <= 500 -> 50f
            max <= 2000 -> 100f
            else -> 200f
        }
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