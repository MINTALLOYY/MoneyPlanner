package com.vibhu.moneyplanner.trends

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vibhu.moneyplanner.Expense
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.database.CategoryData
import com.vibhu.moneyplanner.database.ExpenseData
import com.vibhu.moneyplanner.databinding.FragmentPieChartBinding
import java.util.Calendar
import java.util.Date
import java.util.Random
import java.util.UUID
import kotlin.collections.forEach

class PieChartExpenseFragment : Fragment() {

    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var expenseData: ExpenseData
    private lateinit var categoryData: CategoryData
    private var entireExpense = 0.0
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

        expenseData = ExpenseData(requireContext())
        categoryData = CategoryData(requireContext())

        val numberOfDaysStr = arguments?.getString("numberOfDays") ?: "30"
        numberOfDays = numberOfDaysStr.toInt()

        val endDate = Date()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -numberOfDays)
        val startDate = calendar.time

        val expenses = expenseData.getExpensesInDateRange(startDate, endDate)
        val categoryExpenses = aggregateExpensesByCategory(expenses)

        val entries = mutableListOf<PieEntry>()
        val colorsAvailable = mutableListOf<Int>(
            ContextCompat.getColor(requireContext(), R.color.dark_grey),  // Convert color resource to color integer
            ContextCompat.getColor(requireContext(), R.color.red_text),
            ContextCompat.getColor(requireContext(), R.color.slightly_dark_grey),
            ContextCompat.getColor(requireContext(), R.color.red),
            ContextCompat.getColor(requireContext(), R.color.dark_gold),
            ContextCompat.getColor(requireContext(), R.color.metallic_gold),
        )
        val colors = mutableListOf<Int>()

        var count = 0
        var loopCount = 0
        categoryExpenses.forEach { (categoryId, totalExpense) ->
            val category = categoryData.getCategoryById(categoryId)
            if (category != null && totalExpense > 0.0) {
                val percentage = (totalExpense / entireExpense).toFloat() * 100
                Log.d("Percentage", "Percentage: $percentage")

                entries.add(PieEntry(percentage, category.categoryName))
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

        categoryData.close()

    }

    private fun setUpPieChart (colors: List<Int>, entries: List<PieEntry>){
        val dataSet =
            PieDataSet(entries, "Incomes by Category (Last 30 Days)") // More descriptive label
        dataSet.colors = colors
        dataSet.valueTextColor = Color.WHITE // Set value text color to white for visibility
        dataSet.valueTextSize = 14f // Set value text size for better readability
        dataSet.sliceSpace = 2f // Add space between slices for better separation
        dataSet.selectionShift = 5f // Add shift to selected slice for emphasis
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getPieLabel(value: Float, pieEntry: PieEntry): String {
                if (value < 20f) {
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
            setEntryLabelColor(Color.BLACK)
            clipToOutline = true

            description.isEnabled = true
            description.textColor = Color.BLACK
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

    private fun aggregateExpensesByCategory(expenses: List<Expense>): Map<UUID, Double> {
        val categoryExpenses = mutableMapOf<UUID, Double>()
        for (expense in expenses) {
            val categoryId = expense.categoryId
            val amount = expense.amount
            categoryExpenses[categoryId] = (categoryExpenses[categoryId] ?: 0.0) + amount
            entireExpense += amount
        }
        Log.d("Total Expense", "-$${entireExpense}")
        return categoryExpenses
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        expenseData.close()
    }
}