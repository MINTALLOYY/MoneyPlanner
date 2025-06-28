package com.vibhu.moneyplanner.trends

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.vibhu.moneyplanner.MainActivity
import com.vibhu.moneyplanner.R
import com.vibhu.moneyplanner.constants.SharedPreferencesConstants
import com.vibhu.moneyplanner.database.InitialBalanceData
import com.vibhu.moneyplanner.database.TransactionData
import com.vibhu.moneyplanner.databinding.FragmentWeeklyGraphBinding
import com.vibhu.moneyplanner.uiFragments.HomeFragment
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
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
        setupChartWithMinimap(balanceEntries)
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


    private fun setupChartWithMinimap(balanceEntries: List<Pair<Date, Double>>) {

        Log.e("WeeklyFragment", "Set Up Main Chart")

        // Set up normal chart
        setupChart(balanceEntries)

        Log.e("WeeklyFragment", "Retrieving Minimap")
        // Retrieve the minimap chart from the HomeFragment
        val homeFragment = parentFragmentManager.findFragmentById(R.id.fragment_container) as? HomeFragment
        val minimapChart = homeFragment?.binding?.minimapChart

        // Test if minimap exists (REMOVE BEFORE PRESENTATION)
        if (minimapChart == null) {
            Log.e("WeeklyFragment", "Minimap chart not found in HomeFragment")
            return
        }

        // Entries for minimap (same data, different formatting)
        val sortedEntries = balanceEntries.sortedBy { it.first }
        val entries = sortedEntries.mapIndexed { index, (_, balance) ->
            Entry(index.toFloat(), balance.toFloat())
        }

        // Dataset creation
        val minimapDataSet = LineDataSet(entries, "").apply {
            color = ContextCompat.getColor(requireContext(), R.color.metallic_gold)
            lineWidth = 1.5f
            setDrawCircles(false)
            setDrawValues(false)
        }

        // Minimap configuration
        configureMinimap(minimapChart, minimapDataSet, entries)

        // Refresh minimap
        minimapChart.invalidate()


    }

    private fun configureMinimap(chart: LineChart, dataSet: LineDataSet, entries: List<Entry>) {
        // Minimap specific configuration
        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false

            // Hide all the details on the minimap
            xAxis.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textColor = Color.TRANSPARENT
            }

            axisLeft.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textColor = Color.TRANSPARENT
            }

            axisRight.isEnabled = false

            dataSet.apply {
                setDrawCircles(true)
                setDrawCircleHole(false)
                circleRadius = 4f
                circleColors = List(entries.size - 1) { Color.TRANSPARENT } + listOf(
                    ContextCompat.getColor(requireContext(), R.color.metallic_gold)
                )
                highLightColor = Color.TRANSPARENT
            }

            // Make it more compact
            setViewPortOffsets(0f, 0f, 16f, 0f)

            // No animations for the minimap
            animateXY(500,500)
        }
    }

    // Normalize dates to midnight
    private fun normalizeDate(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    // Full date logging
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
            color = ContextCompat.getColor(requireContext(), R.color.metallic_gold)
            lineWidth = 3f
            setDrawCircles(true)
            circleRadius = 6f
            setDrawValues(false)
            setDrawCircleHole(false)
            circleColors = mutableListOf(ContextCompat.getColor(requireContext(), R.color.metallic_gold))
        }

        binding.balanceWeekly.apply {

            // 2. Set data and viewport
            data = LineData(dataSet)
            setViewPortOffsets(50f, 20f, 50f, 150f)
            background = ContextCompat.getDrawable(requireContext(), R.drawable.white_container)

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

                // Formatting for large numbers
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when {
                            value >= 1000000 -> "${(value/1000000).toString().take(3)}M"
                            value >= 1000 -> "${(value / 1000).toString().take(3)}k"
                            value <= -1000000 -> "-${(-value/1000000).toString().take(3)}M"
                            value <= -1000 -> "-${(-value / 1000).toString().take(3)}k"
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
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                xOffset = 0f
                setDrawInside(false) // Ensure legend is drawn outside the chart
                yOffset = 10f // Add some space between x-axis labels and legend
            }

            // General Chart Settings
            setExtraLeftOffset(20f)
            setViewPortOffsets(70f, 60f, 50f, 70f)  // left, top, right, bottom
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