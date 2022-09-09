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
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.databinding.FragmentGraphBinding
import com.rohitthebest.manageyourrenters.others.Constants.CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_KEY
import com.rohitthebest.manageyourrenters.others.Constants.CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_NAME
import com.rohitthebest.manageyourrenters.others.Constants.ONE_DAY_MILLISECONDS
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.saveBitmapToCacheDirectoryAndShare
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "GraphFragment"

@AndroidEntryPoint
class GraphFragment : Fragment(R.layout.fragment_graph) {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()

    private lateinit var pie: Pie
    private var isAllTimeSelected = false

    private var selectedCustomDateRangeMenu: CustomDateRange? = CustomDateRange.ALL_TIME

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGraphBinding.bind(view)

        //setUpCartesianChart()
        changeSelectionUI()
        setUpPieChart()

        loadCustomDateRangeValueFromSharedPreference()

        handleDateRangeSelectionMenu(selectedCustomDateRangeMenu ?: CustomDateRange.ALL_TIME)
        initListeners()
    }

    private fun loadCustomDateRangeValueFromSharedPreference() {

        selectedCustomDateRangeMenu =
            requireActivity().loadAnyValueFromSharedPreference(
                CustomDateRange::class.java,
                CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_NAME,
                CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_KEY
            )

        if (selectedCustomDateRangeMenu == null) {

            selectedCustomDateRangeMenu = CustomDateRange.ALL_TIME
        }

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

        binding.chart.setChart(pie)
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
                        getString(R.string.depp_analyze_error_message),
                        Toast.LENGTH_LONG
                    )
                }
            }

            true
        }

        binding.toolbar.menu.findItem(R.id.menu_monthly_graph).setOnMenuItemClickListener {

            expenseViewModel.getAllExpenses().observe(viewLifecycleOwner) {

                if (it.isNotEmpty()) {
                    findNavController().navigate(R.id.action_graphFragment_to_monthlyGraphFragment)
                } else {

                    showToast(requireContext(), getString(R.string.no_expense_added))
                }
            }

            true
        }

        binding.toolbar.menu.findItem(R.id.menu_share_expense_graph_sc).setOnMenuItemClickListener {

            binding.toolbar.hide()

            val bitmap = binding.root.loadBitmap()

            binding.toolbar.show()

            lifecycleScope.launch {

                saveBitmapToCacheDirectoryAndShare(requireActivity(), bitmap)
            }

            true
        }

        binding.toolbar.menu.findItem(R.id.menu_save_expense_graph_sc).setOnMenuItemClickListener {

            binding.toolbar.hide()

            val bitmap = binding.root.loadBitmap()

            binding.toolbar.show()

            bitmap.saveToStorage(requireContext(), "${getUid()}_expense_graph")

            showToast(requireContext(), "Screenshot saved to phone storage")

            true
        }

        binding.dateRangeMenuBtn.setOnClickListener { view ->

            showMenuForSelectingCustomTime(view)

        }

        binding.dateRangeTv.setOnClickListener { view ->

            //todo : open expenses in bottomsheet
        }
    }

    private fun showMenuForSelectingCustomTime(view: View) {

        Functions.showCustomDateRangeOptionMenu(
            requireActivity(),
            view
        ) { selectedMenu ->

            selectedCustomDateRangeMenu = selectedMenu
            saveCustomDateRangeValueInSharedPreference()
            handleDateRangeSelectionMenu(selectedMenu)
        }
    }

    private fun handleDateRangeSelectionMenu(selectedMenu: CustomDateRange) {

        Log.d(TAG, "handleDateRangeSelectionMenu: selectedMenu -> $selectedMenu")

        when (selectedMenu) {

            CustomDateRange.ALL_TIME -> {

                if (!isAllTimeSelected) {

                    Log.d(
                        TAG,
                        "handleDateRangeSelectionMenu: all time selected : $isAllTimeSelected"
                    )

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

                Log.d(TAG, "date range: selectedMenu -> $selectedMenu")

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
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    d1
                )
            } to ${
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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
                .observe(viewLifecycleOwner) { expenseCategories ->

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
                }
        }

    }

    private fun getExpenseCategoryExpensesByAllTime() {

        if (isAllTimeSelected) {


            Log.d(TAG, "getExpenseCategoryExpensesByAllTime: ")

            expenseViewModel.getTotalExpenseAmount().observe(viewLifecycleOwner) { total ->

                pie.legend().title("Total expense : $total")
            }

            expenseCategoryViewModel.getAllExpenseCategories()
                .observe(viewLifecycleOwner) { expenseCategories ->

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
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        saveCustomDateRangeValueInSharedPreference()

        _binding = null
    }

    private fun saveCustomDateRangeValueInSharedPreference() {

        if (selectedCustomDateRangeMenu != CustomDateRange.CUSTOM_DATE_RANGE) {

            requireActivity().saveAnyObjectToSharedPreference(
                CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_NAME,
                CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_KEY,
                selectedCustomDateRangeMenu
            )
        }
    }
}
