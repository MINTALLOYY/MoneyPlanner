package com.vibhu.moneyplanner

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.tooling.data.position
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
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
import com.vibhu.moneyplanner.databinding.FragmentMonthlyGraphBinding
import java.text.SimpleDateFormat
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
import kotlin.math.round

class MonthlyFragment : Fragment() {

    private lateinit var transactionData: TransactionData
    private lateinit var initialBalanceData: InitialBalanceData
    private lateinit var binding: FragmentMonthlyGraphBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMonthlyGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionData = TransactionData(requireContext())
        initialBalanceData = InitialBalanceData(requireContext())

        val sharedPreferences = (requireActivity() as MainActivity).sharedPreferences
        val userId = UUID.fromString(sharedPreferences.getString(SharedPreferencesConstants.USER_ID_PREF, null))

        val balanceEntries = calculateMonthlyBalances(userId)
        setupChartWithMinimap(balanceEntries)
    }



    private fun calculateMonthlyBalances(userId: UUID): List<Pair<Date, Double>> {
        val monthlyBalances = mutableListOf<Pair<Date, Double>>()
        val initialBalance = initialBalanceData.fetchInitialBalance(userId) ?: 0.0

        // Get all transactions for this user
        val allTransactions = transactionData.getAllTransaction()

        // If no transactions, just return initial balance
        if (allTransactions.isEmpty()) {
            val today = Calendar.getInstance().time
            return listOf(today to initialBalance)
        }

        // Find earliest and latest dates to cover
        val firstDate = (allTransactions.minByOrNull { it.date }?.date
            ?: initialBalanceData.fetchInitialDate(userId)
            ?: Date())

        // Make sure we include current month
        val lastDate = Calendar.getInstance().apply {
            // Add one month to ensure we include current month
            add(Calendar.MONTH, 1)
        }.time

        // Get date representing first day of month for earliest transaction
        val startCalendar = Calendar.getInstance().apply {
            time = firstDate
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Create monthly points between first and last dates
        var runningBalance = initialBalance
        var currentDate = startCalendar.time

        while (currentDate.before(lastDate)) {
            // Get the last day of current month
            val monthEndCalendar = Calendar.getInstance().apply {
                time = currentDate
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            val monthEnd = monthEndCalendar.time

            // Calculate month's transactions
            val monthTransactions = allTransactions.filter {
                !it.date.before(currentDate) && !it.date.after(monthEnd)
            }

            val monthlyChange = monthTransactions.sumOf {
                if (it.isIncome) it.amount else -it.amount
            }

            // Update running balance with this month's transactions
            runningBalance += monthlyChange

            // Create a data point using the middle of the month (better for display)
            val displayDate = Calendar.getInstance().apply {
                time = currentDate
                set(Calendar.DAY_OF_MONTH, 15)  // Middle of month for display
            }.time

            // Add this month's final balance
            monthlyBalances.add(displayDate to runningBalance)

            // Move to first day of next month
            startCalendar.add(Calendar.MONTH, 1)
            currentDate = startCalendar.time
        }

        return monthlyBalances
    }
    private fun setupChartWithMinimap(balanceEntries: List<Pair<Date, Double>>) {
        // Set up normal chart
        setupChart(balanceEntries)

        // Retrieve the minimap chart from the HomeFragment
        val homeFragment = parentFragmentManager.findFragmentById(R.id.fragment_container) as? HomeFragment
        val minimapChart = homeFragment?.binding?.minimapChart ?: return

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
                setDrawAxisLine(true)
                textColor = Color.TRANSPARENT
            }

            axisLeft.apply {
                setDrawLabels(false)
                setDrawGridLines(false)
                setDrawAxisLine(true)
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


    private fun setupChart(balanceEntries: List<Pair<Date, Double>>) {
        // Sort entries by date and assign proper x-values
        val sortedEntries = balanceEntries.sortedBy { it.first }
        val dateStrings = sortedEntries.map {
            SimpleDateFormat("MM/dd", Locale.getDefault()).format(it.first)
        }

        val entries = sortedEntries.mapIndexed { index, (_, balance) ->
            Entry(index.toFloat(), balance.toFloat())
        }

        val dataSet = LineDataSet(entries, "Monthly Balance").apply {
            color = ContextCompat.getColor(requireContext(), R.color.metallic_gold)
            lineWidth = 3f
            setDrawCircles(true)
            circleRadius = 6f
            setDrawValues(false)
            setDrawCircleHole(false)
            circleColors = mutableListOf(ContextCompat.getColor(requireContext(), R.color.metallic_gold))
        }

        binding.balanceMonthly.apply {

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
        binding.balanceMonthly.apply {

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
                xAxis.valueFormatter = object : ValueFormatter() {
                    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        return balanceEntries.getOrNull(value.toInt())?.let {
                            monthFormat.format(it.first)
                        } ?: ""
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
                setDrawInside(false) // Ensure legend is drawn outside the chart
                yOffset = 10f // Add some space between x-axis labels and legend                xOffset = 0f
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