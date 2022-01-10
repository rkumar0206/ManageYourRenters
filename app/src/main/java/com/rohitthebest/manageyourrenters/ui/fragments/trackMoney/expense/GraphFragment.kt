package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.chart.common.listener.Event
import com.anychart.chart.common.listener.ListenersInterface
import com.anychart.charts.Cartesian
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.databinding.FragmentGraphBinding
import com.rohitthebest.manageyourrenters.others.Constants.ONE_DAY_MILLISECONDS
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "GraphFragment"

@AndroidEntryPoint
class GraphFragment : Fragment(R.layout.fragment_graph) {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()

    private lateinit var pie: Pie
    private lateinit var cartesian: Cartesian

    private val SELECTED_DATE_RANGE_KEY = "custom_date_range_key"
    private var isAllTimeSelected = false
    private var selectedCustomDateRange: CustomDateRange = CustomDateRange.ALL_TIME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {

            Log.d(TAG, "onCreate: ${savedInstanceState.getString(SELECTED_DATE_RANGE_KEY)}")

            val dateRange = savedInstanceState.getString(SELECTED_DATE_RANGE_KEY)
            val type = object : TypeToken<CustomDateRange>() {}.type
            selectedCustomDateRange = Gson().fromJson(dateRange, type)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphBinding.bind(view)

        //setUpCartesianChart()
        changeSelectionUI()
        setUpPieChart()

        Log.d(TAG, "onViewCreated: selected custom : $selectedCustomDateRange")

        //getExpenseCategoryExpensesByAllTime()
        handleDateRangeSelectionMenu(selectedCustomDateRange)
        initListeners()
    }


    private fun setUpPieChart() {

        Log.d(TAG, "setUpPieChart: ")

        pie = AnyChart.pie()

        binding.chart.setProgressBar(binding.progressBar)

        pie.title().enabled(true)
        pie.title("All time")
        //pie.title("Expenses on each category")

        pie.labels().position("outside")

        pie.legend().title().enabled(true)
        pie.legend().title()
            .fontColor("#212121")
            .padding(0.0, 0.0, 30.0, 0.0)

        pie.legend()
            .position("bottom")
            .itemsLayout(LegendLayout.HORIZONTAL_EXPANDABLE)
            .align(Align.CENTER)

/*
        pie.setOnClickListener(object :
            ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event) {

                            Toast.makeText(
                                requireContext(),
                                event.data["x"].toString() + ":" + event.data["value"],
                                Toast.LENGTH_SHORT
                            ).show()

            }
        })
*/

        binding.chart.setChart(pie)

    }

    private fun setUpCartesianChart() {

        cartesian = AnyChart.column()
        binding.chart.setProgressBar(binding.progressBar)

        cartesian.title("Expenses on each category")

        cartesian.labels().position("outside")

        cartesian.legend().title().enabled(true)
        cartesian.legend().title()
            .text("Expense categories")
            .padding(0.0, 0.0, 10.0, 0.0)

        cartesian.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)

        cartesian.setOnClickListener(object :
            ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event) {
/*
                            Toast.makeText(
                                requireContext(),
                                event.data["x"].toString() + ":" + event.data["value"],
                                Toast.LENGTH_SHORT
                            ).show()
*/
            }
        })
    }

    private var d1 = System.currentTimeMillis() - (ONE_DAY_MILLISECONDS * 30)
    private var d2 = System.currentTimeMillis()

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbar.menu.findItem(R.id.menu_deep_analyze_expense).setOnMenuItemClickListener {

            expenseCategoryViewModel.getAllExpenseCategories().observe(viewLifecycleOwner) {

                if (it.size >= 2) {

                    findNavController().navigate(R.id.action_graphFragment_to_deepAnalyzeExpenseFragment)
                } else {

                    showToast(
                        requireContext(),
                        "You must have at least 2 categories for deep analyze mode",
                        Toast.LENGTH_LONG
                    )
                }
            }

            true
        }

        binding.dateRangeMenuBtn.setOnClickListener { view ->

            showMenuForSelectingCustomTime(view)

        }

        binding.dateRangeTv.setOnClickListener { view ->

            showMenuForSelectingCustomTime(view)
        }
    }

    private fun showMenuForSelectingCustomTime(view: View) {

        Functions.showCustomDateRangeOptionMenu(
            requireActivity(),
            view
        ) { selectedMenu ->

            selectedCustomDateRange = selectedMenu

            handleDateRangeSelectionMenu(selectedCustomDateRange)

        }
    }

    private fun handleDateRangeSelectionMenu(selectedMenu: CustomDateRange) {

        when (selectedMenu) {

            CustomDateRange.ALL_TIME -> {

                if (!isAllTimeSelected) {

                    isAllTimeSelected = true

                    getExpenseCategoryExpensesByAllTime()

                    changeSelectionUI()

                    pie.title(getString(R.string.all_time))
                }
            }

            CustomDateRange.LAST_30_DAYS -> {

                isAllTimeSelected = false

                d1 = System.currentTimeMillis() - (30 * ONE_DAY_MILLISECONDS)
                d2 = System.currentTimeMillis()

                getExpenseByDateRange()

                pie.title(getString(R.string.last_30_days))
            }

            CustomDateRange.LAST_7_DAYS -> {

                isAllTimeSelected = false
                d1 = System.currentTimeMillis() - (7 * ONE_DAY_MILLISECONDS)
                d2 = System.currentTimeMillis()

                getExpenseByDateRange()

                pie.title(getString(R.string.last_7_days))
            }

            CustomDateRange.LAST_365_DAYS -> {

                isAllTimeSelected = false

                d1 = System.currentTimeMillis() - (365 * ONE_DAY_MILLISECONDS)
                d2 = System.currentTimeMillis()

                getExpenseByDateRange()

                pie.title(getString(R.string.last_365_days))
            }

            CustomDateRange.CUSTOM_DATE_RANGE -> {

                Functions.showDateRangePickerDialog(
                    d1,
                    d2, {
                        requireActivity().supportFragmentManager
                    },
                    { dates: Pair<Long, Long> ->

                        d1 = dates.first
                        d2 = dates.second

                        isAllTimeSelected = false

                        getExpenseByDateRange()
                    }
                )

                pie.title(getString(R.string.custom_range))
            }

            else -> {

                isAllTimeSelected = false

                val pair = Functions.getMillisecondsOfStartAndEndUsingConstants(
                    selectedMenu
                )

                d1 = pair.first
                d2 = pair.second

                getExpenseByDateRange()

                when (selectedMenu) {

                    CustomDateRange.THIS_MONTH -> pie.title(getString(R.string.this_month))
                    CustomDateRange.THIS_WEEK -> pie.title(getString(R.string.this_week))
                    CustomDateRange.PREVIOUS_MONTH -> pie.title(getString(R.string.previous_month))
                    CustomDateRange.PREVIOUS_WEEK -> pie.title(getString(R.string.previous_week))
                    else -> {}
                }

            }
        }

    }

    private fun getExpenseByDateRange() {

        if (!isAllTimeSelected) {

            changeSelectionUI()
            getExpenseCategoryExpensesByDateRange(
                d1,
                d2
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeSelectionUI() {

        if (isAllTimeSelected) {

            binding.dateRangeTv.text = "All time"
        } else {

            binding.dateRangeTv.text = "${
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    d1
                )
            } to ${
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    d2
                )
            }"

        }
    }

    private fun getExpenseCategoryExpensesByDateRange(date1: Long?, date2: Long?) {

        if (!isAllTimeSelected) {

            Log.d(TAG, "getExpenseCategoryExpensesByDateRange: ")

            expenseViewModel.getTotalExpenseAmountByDateRange(
                date1!!,
                date2!! + ONE_DAY_MILLISECONDS
            )
                .observe(viewLifecycleOwner) { total ->

                    pie.legend().title("Total expense : %.3f".format(total))
                }


            expenseCategoryViewModel.getAllExpenseCategories()
                .observe(viewLifecycleOwner, { expenseCategories ->

                    if (expenseCategories.isNotEmpty()) {

                        val data = ArrayList<DataEntry>()

                        expenseCategories.forEach { expenseCategory ->

                            lifecycleScope.launch {

                                try {
                                    expenseViewModel.getExpenseAmountSumByExpenseCategoryByDateRange(
                                        expenseCategory.key, date1, date2 + ONE_DAY_MILLISECONDS
                                    ).collect { amount ->

                                        Log.d(
                                            TAG,
                                            "getExpenseCategoryExpensesByDateRange: category : ${expenseCategory.categoryName} -> amount : $amount"
                                        )

                                        data.add(
                                            ValueDataEntry(
                                                expenseCategory.categoryName,
                                                amount
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        lifecycleScope.launch {

                            delay(500)

                            Log.d(TAG, "getAllExpenseCategory: $data")

                            if (data.isNotEmpty()) {

                                binding.chart.show()
                                pie.data(data)

                            } else {

                                binding.chart.hide()
                                showToast(requireContext(), "No data found")
                            }
                        }

                    }
                })
        }

    }

    private fun getExpenseCategoryExpensesByAllTime() {

        if (isAllTimeSelected) {


            Log.d(TAG, "getExpenseCategoryExpensesByAllTime: ")

            expenseViewModel.getTotalExpenseAmount().observe(viewLifecycleOwner) { total ->

                pie.legend().title("Total expense : $total")
            }


            expenseCategoryViewModel.getAllExpenseCategories()
                .observe(viewLifecycleOwner, { expenseCategories ->

                    if (expenseCategories.isNotEmpty()) {

                        val data = ArrayList<DataEntry>()

                        expenseCategories.forEach { expenseCategory ->

                            lifecycleScope.launch {

                                try {
                                    expenseViewModel.getExpenseAmountSumByExpenseCategoryKey(
                                        expenseCategory.key
                                    ).collect { amount ->

                                        Log.d(
                                            TAG,
                                            "getExpenseCategoryExpensesByAllTime: category : ${expenseCategory.categoryName} -> amount : $amount"
                                        )

                                        data.add(
                                            ValueDataEntry(
                                                expenseCategory.categoryName,
                                                amount
                                            )
                                        )
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        lifecycleScope.launch {

                            delay(500)

                            Log.d(TAG, "getAllExpenseCategory: $data")

                            if (data.isNotEmpty()) {

                                binding.chart.show()
                                pie.data(data)
                                //binding.chart.setChart(pie)
                            } else {

                                binding.chart.hide()
                            }

                        }

                    }
                })
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putString(SELECTED_DATE_RANGE_KEY, Gson().toJson(selectedCustomDateRange))
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
