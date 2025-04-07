package com.vibhu.moneyplanner

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.vibhu.moneyplanner.models.Transaction
import com.vibhu.moneyplanner.databinding.FragmentWeeklyGraphBinding
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

class WeeklyFragment : Fragment() {

    private lateinit var transactionData: TransactionData
    private lateinit var initialBalanceData: InitialBalanceData
    private lateinit var binding: FragmentWeeklyGraphBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeeklyGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionData = TransactionData(requireContext())
        initialBalanceData = InitialBalanceData(requireContext())

        val sharedPreferences = (requireActivity() as MainActivity).sharedPreferences
        val userId = UUID.fromString(sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null))

        val balanceEntries = calculateBalanceEntries(userId)
        setupChart(balanceEntries)
    }

    private fun calculateBalanceEntries(userId: UUID): List<Pair<Date, Double>> {
        val weeklyBalances = mutableListOf<Pair<Date, Double>>()
        var runningBalance = initialBalanceData.fetchInitialBalance(userId) ?: 0.0

        // Get all transactions sorted by date (with normalized times)
        val allTransactions = transactionData.getAllTransaction()
            .sortedBy { it.date }
            .map {
                it.copy(date = normalizeDate(it.date)) // Normalize all dates to midnight
            }

        // Find the earliest date (normalized)
        val firstDate = allTransactions.firstOrNull()?.date
            ?: normalizeDate(initialBalanceData.fetchInitialDate(userId) ?: Date())

        // Get the previous Monday (or same day if Monday)
        val calendar = Calendar.getInstance().apply {
            time = firstDate
            // Roll back to previous Monday
            while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
            // Ensure we're at midnight
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        var currentWeekStart = calendar.time

        // Process until current week + 1 (normalized)
        val endDate = normalizeDate(Date()).let { today ->
            Calendar.getInstance().apply {
                time = today
                add(Calendar.WEEK_OF_YEAR, 1)
                set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                // Normalize to end of day
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
        }

        // Add initial balance point
        weeklyBalances.add(currentWeekStart to runningBalance)
        Log.d("Balance", "Initial ${formatDateFull(currentWeekStart)}: $runningBalance")

        while (currentWeekStart <= endDate) {
            // Calculate end of week (Sunday at 23:59:59)
            val currentWeekEnd = Calendar.getInstance().apply {
                time = currentWeekStart
                add(Calendar.DAY_OF_MONTH, 6)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time

            // Calculate net change for THIS WEEK ONLY (inclusive range)
            val weeklyChange = allTransactions
                .filter { transaction ->
                    transaction.date in currentWeekStart..currentWeekEnd
                }
                .sumOf { if(it.isIncome) it.amount else -it.amount }

            runningBalance += weeklyChange

            // Add balance at END of week
            weeklyBalances.add(currentWeekEnd to runningBalance)
            Log.d("Balance", "Week ${formatDateFull(currentWeekStart)} to ${formatDateFull(currentWeekEnd)}: " +
                    "${allTransactions.count { it.date in currentWeekStart..currentWeekEnd }} transactions, " +
                    "Change=$weeklyChange, New Balance=$runningBalance")

            // Move to next Monday at 00:00:00
            currentWeekStart = Calendar.getInstance().apply {
                time = currentWeekStart
                add(Calendar.WEEK_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time
        }

        return weeklyBalances.distinctBy { it.first }
    }

    // Helper function to normalize dates to midnight
    private fun normalizeDate(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    // Helper function for full date logging
    private fun formatDateFull(date: Date): String {
        return SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(date)
    }


    private fun setupChart(balanceEntries: List<Pair<Date, Double>>) {
        // Sort entries by date and assign proper x-values
        val sortedEntries = balanceEntries.sortedBy { it.first }
        val dateStrings = sortedEntries.map {
            SimpleDateFormat("MM/dd", Locale.getDefault()).format(it.first)
        }

        val entries = sortedEntries.mapIndexed { index, (_, balance) ->
            Entry(index.toFloat(), balance.toFloat())
        }

        val dataSet = LineDataSet(entries, "Weekly Balance").apply {
            color = Color.BLUE
            lineWidth = 2.5f
            setDrawCircles(true)
            circleRadius = 4f
            setDrawValues(false)
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

    private fun customizeChart(balanceEntries: List<Pair<Date, Double>>) {
        binding.balanceWeekly.apply {

            val shouldHighlightZero = balanceEntries.any { it.second < 0 } &&
                    balanceEntries.any { it.second > 0 }

            // Y-Axis (Left)
            axisLeft.apply {
                setDrawZeroLine(shouldHighlightZero) // Only draw if needed
                if (shouldHighlightZero) {
                    zeroLineWidth = 2.5f  // Bold line
                    zeroLineColor = Color.BLACK  // Visible color
                }

                // Rest of your Y-axis config
                axisMinimum = balanceEntries.minOfOrNull { it.second }?.toFloat()?.minus(100) ?: 0f
                axisMaximum = balanceEntries.maxOfOrNull { it.second }?.toFloat()?.plus(100) ?: 0f
                granularity = 100f
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
                        return dateFormat.format(balanceEntries[index].first)
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