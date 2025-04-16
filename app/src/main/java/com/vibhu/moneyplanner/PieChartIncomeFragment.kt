package com.vibhu.moneyplanner

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.util.Log
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vibhu.moneyplanner.databinding.FragmentPieChartBinding
import com.vibhu.moneyplanner.models.Income
import java.util.Calendar
import java.util.Date
import java.util.UUID

class PieChartIncomeFragment : Fragment() {

    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var incomeData: IncomeData
    private lateinit var incomeCategoryData: IncomeCategoryData
    private var entireIncome: Double = 0.0
    private var numberOfDays: Int = 30

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeData = IncomeData(requireContext())
        incomeCategoryData = IncomeCategoryData(requireContext())

        val numberOfDaysStr = arguments?.getString("numberOfDays") ?: "30"
        numberOfDays = numberOfDaysStr.toInt()

        val endDate = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -numberOfDays)
        val startDate = calendar.time

        val incomes = incomeData.getIncomesInDateRange(startDate, endDate)
        val sourceIncomes = aggregateIncomesByCategory(incomes)

        val entries = mutableListOf<PieEntry>()
        val colorsAvailable = mutableListOf<Int>(
            ContextCompat.getColor(requireContext(), R.color.dark_grey),  // Convert color resource to color integer
            ContextCompat.getColor(requireContext(), R.color.green_text),
            ContextCompat.getColor(requireContext(), R.color.off_white),
            ContextCompat.getColor(requireContext(), R.color.metallic_gold),
            ContextCompat.getColor(requireContext(), R.color.red),
        )
        val colors = mutableListOf<Int>()

        var count = 0
        var loopCount = 0
        sourceIncomes.forEach { (incomeSourceId, totalIncome) ->
            val incomeSource = incomeCategoryData.getIncomeCategoryById(incomeSourceId)
            if (incomeSource != null && totalIncome > 0.0) {

                val percentage = (totalIncome / entireIncome).toFloat() * 100
                Log.d("Percentage", "Percentage: $percentage")
                entries.add(PieEntry(percentage, incomeSource.incomeCategoryName))

                colors.add(colorsAvailable[count])
                count += 1

                if ((count == (colorsAvailable.size - 1))) {
                    loopCount += 1
                    if (loopCount == (colorsAvailable.size - 1)) {
                        loopCount = 0
                    }
                    count = loopCount
                }

            }
        }

        setUpPieChart(colors, entries)
    }

    private fun setUpPieChart (colors: List<Int>, entries: List<PieEntry>){
        val dataSet = PieDataSet(entries, "Incomes by Category (Last 30 Days)") // More descriptive label
        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE // Set value text color to white for visibility
        dataSet.valueTextSize = 14f // Set value text size for better readability
        dataSet.sliceSpace = 2f // Add space between slices for better separation
        dataSet.selectionShift = 5f // Add shift to selected slice for emphasis
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getPieLabel(value: Float, pieEntry: PieEntry): String {
                if (value < 10f) {
                    if(value < 5f){
                        return ""
                    }
                    return "${String.format("%.1f", value)}%" // Format percentage with one decimal place
                }
                return "${String.format("%.2f", value)}%" // Include label and formatted percentage
            }
        }
        dataSet.valueLinePart1Length = 0.4f // Length of first part of line
        dataSet.valueLinePart2Length = 0.4f // Length of second part
        dataSet.valueLineColor = Color.DKGRAY // Color of connecting lines
        dataSet.valueLineWidth = 1f
        dataSet.xValuePosition =  PieDataSet.ValuePosition.OUTSIDE_SLICE

        val pieData = PieData(dataSet)
        binding.pieChart.data = pieData

        binding.pieChart.apply {
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.WHITE)
            clipToOutline = true

            description.isEnabled = true
            description.textColor = Color.WHITE
            if (numberOfDays != Int.MAX_VALUE) description.text = "Last ${numberOfDays} Days"
            else description.text = "All Time"

            // 1. Set sufficient offsets for labels
            setExtraOffsets(32f, 32f, 32f, 32f) // Left, Top, Right, Bottom
            setHoleColor(ContextCompat.getColor(requireContext(), R.color.black))
            setTransparentCircleColor(ContextCompat.getColor(requireContext(), R.color.black))

            legend.isEnabled = false

            animateY(1000)
            invalidate()
        }
    }


    private fun aggregateIncomesByCategory(incomes: List<Income>): Map<UUID, Double> {
        val incomeCategoryExpenses = mutableMapOf<UUID, Double>()
        for (income in incomes) {
            val incomeCategoryId = income.incomeCategoryId
            val amount = income.amount
            incomeCategoryExpenses[incomeCategoryId] = (incomeCategoryExpenses[incomeCategoryId] ?: 0.0) + amount
            entireIncome += amount
        }

        Log.d("Total Income", "Total Income: $entireIncome")
        return incomeCategoryExpenses
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        incomeData.close()
        incomeCategoryData.close()
    }
}