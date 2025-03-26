package com.vibhu.moneyplanner

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.tooling.data.position
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.vibhu.moneyplanner.categoryexpense.ExpenseData
import com.vibhu.moneyplanner.databinding.FragmentWeeklyGraphBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class WeeklyFragment : Fragment() {

    private lateinit var incomeData: IncomeData
    private lateinit var expenseData: ExpenseData
    private lateinit var initialBalanceData: InitialBalanceData
    private lateinit var binding: FragmentWeeklyGraphBinding
    private var currentBalance: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeeklyGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        incomeData = IncomeData(requireContext())
        expenseData = ExpenseData(requireContext())
        initialBalanceData = InitialBalanceData(requireContext())

        val sharedPreferences = (requireActivity() as MainActivity).sharedPreferences
        val userId = UUID.fromString(sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null))
        currentBalance = initialBalanceData.fetchInitialBalance(userId) ?: 0.0
        currentBalance += incomeData.getTotalIncomeAmount() - expenseData.getTotalExpenseAmount()

        val balanceEntries = calculateBalanceEntries(userId)
        setupChart(balanceEntries)
    }

    private fun calculateBalanceEntries(userId: UUID): List<Pair<LocalDate, Double>> {
        val balanceEntries = mutableListOf<Pair<LocalDate, Double>>()
        val today = LocalDate.now()
        val twoWeeksLater = today.plusWeeks(2)

        // 1. Get ALL transactions with their correct date fields
        val allIncomes = incomeData.getAllIncomes().map {
            Transaction(it.amount, it.receivedDate.toLocalDate(), isIncome = true)
        }
        val allExpenses = expenseData.getAllExpenses().map {
            Transaction(it.amount, it.expenseDate.toLocalDate(), isIncome = false)
        }

        // 2. Combine and sort by date
        val allTransactions = (allIncomes + allExpenses)
            .sortedBy { it.date }

        // 3. Get initial balance
        var runningBalance = initialBalanceData.fetchInitialBalance(userId) ?: 0.0

        // 4. Find the earliest date (initial balance date or first transaction)
        val initialDate = initialBalanceData.fetchInitialDate(userId)?.toLocalDate()
            ?: allTransactions.firstOrNull()?.date
            ?: today

        // 5. Set up weekly buckets
        val calendar = Calendar.getInstance().apply {
            time = initialDate.toDate()
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        var currentWeekStart = calendar.time.toLocalDate()

        // 6. Process week by week
        var transactionIndex = 0
        while (currentWeekStart.isBefore(twoWeeksLater)) {
            val currentWeekEnd = currentWeekStart.plusDays(6)

            // Process transactions for this week
            while (transactionIndex < allTransactions.size) {
                val transaction = allTransactions[transactionIndex]
                if (transaction.date.isAfter(currentWeekEnd)) break

                runningBalance += if (transaction.isIncome) transaction.amount else -transaction.amount
                transactionIndex++
            }

            // Record weekly balance
            balanceEntries.add(currentWeekStart to runningBalance)

            // Move to next week
            currentWeekStart = currentWeekStart.plusWeeks(1)
        }

        return balanceEntries
    }

    // Helper data class
    private data class Transaction(
        val amount: Double,
        val date: LocalDate,
        val isIncome: Boolean
    )

    private fun setupChart(balanceEntries: List<Pair<LocalDate, Double>>) {
        val entries = balanceEntries.sortedBy { it.first }.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second.toFloat())
        }

        val dataSet = LineDataSet(entries, "Balance Over Time").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false) // Hide values on points to reduce clutter
        }

        binding.balanceWeekly.apply {

            // 2. Set data and viewport
            data = LineData(dataSet)
            setViewPortOffsets(50f, 20f, 50f, 50f)
            setBackgroundColor(Color.WHITE)

            // 3. Force proper initial layout
            post {
                notifyDataSetChanged()
                invalidate()
            }

            // 4. Control touch behavior
            setTouchEnabled(true)
            setPinchZoom(false)
            setScaleEnabled(false)
        }


        customizeChart(balanceEntries)
    }

    private fun customizeChart(balanceEntries: List<Pair<LocalDate, Double>>) {
        binding.balanceWeekly.apply {
            // Y-Axis (Left)
            axisLeft.apply {
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
                textSize = 9f  // Smaller text
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when {
                            value >= 1000000 -> "${(value/1000000).toInt()}M"
                            value >= 1000 -> "${(value/1000).toInt()}K"
                            else -> value.toInt().toString()
                        }
                    }
                }
            }

            // X-Axis (Bottom)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                labelCount = min(5, balanceEntries.size)
                valueFormatter = object : ValueFormatter() {
                    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        val index = value.toInt().coerceIn(0, balanceEntries.size - 1)
                        return dateFormat.format(balanceEntries[index].first.toDate())
                    }
                }
                setAvoidFirstLastClipping(true)
                yOffset = 10f
                setAvoidFirstLastClipping(true)
                setDrawGridLines(false)
            }

            // Right Axis
            axisRight.isEnabled = false

            // Legend
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                yOffset = 25f
                xOffset = 0f
            }

            // General Chart Settings
            setExtraLeftOffset(20f)
            setViewPortOffsets(70f, 30f, 50f, 70f)  // left, top, right, bottom
            description.isEnabled = false
            setDrawGridBackground(false)
            invalidate()
        }
    }

    fun Date.toLocalDate(): LocalDate {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun LocalDate.toDate(): Date {
        return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
    }
}