package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.DeepAnalyzeExpenseCategoryAdapter
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentDeepAnalyzeExpenseBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseGraphDataViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.executeAfterDelay
import com.rohitthebest.manageyourrenters.utils.format
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.loadAnyValueFromSharedPreference
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private const val TAG = "DeepAnalyzeExpenseFragm"

@AndroidEntryPoint
class DeepAnalyzeExpenseFragment : Fragment(R.layout.fragment_deep_analyze_expense),
    DeepAnalyzeExpenseCategoryAdapter.OnClickListener {

    private var _binding: FragmentDeepAnalyzeExpenseBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val expenseGraphDataViewModel by viewModels<ExpenseGraphDataViewModel>()

    private lateinit var pie: Pie

    private lateinit var deepAnalyzeExpenseCategoryAdapter: DeepAnalyzeExpenseCategoryAdapter

    private var isExpenseCategoryRVVisible = true

    private var isDateRangeSelected = false

    private var startDate = System.currentTimeMillis() - (Constants.ONE_DAY_MILLISECONDS * 30)
    private var endDate = System.currentTimeMillis()

    private var selectedCustomDateRangeMenu: CustomDateRange? = CustomDateRange.ALL_TIME

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDeepAnalyzeExpenseBinding.bind(view)

        binding.toolbar.subtitle = getString(R.string.all_time)

        deepAnalyzeExpenseCategoryAdapter = DeepAnalyzeExpenseCategoryAdapter()

        setUpPieChart()

        setUpRecyclerView()

        loadCustomDateRangeValueFromSharedPreference()

        handleDateRangeMenu(selectedCustomDateRangeMenu ?: CustomDateRange.ALL_TIME)

        getAllExpenseCategories()

        initListeners()

        observerExpenseGraphData()
    }

    private fun observerExpenseGraphData() {

        expenseGraphDataViewModel.expenseGraphData.observe(viewLifecycleOwner) {

            if (it != null && it.first.isNotEmpty()) {

                val expenseCategoryNameAndTheirTotalList = it.first
                val total = it.second

                pie.title("Total expense amount: ${total.format(2)}")

                val data = ArrayList<DataEntry>()

                expenseCategoryNameAndTheirTotalList.forEach { expenseCategoryAndTheirTotalExpenseAmounts ->

                    data.add(
                        ValueDataEntry(
                            expenseCategoryAndTheirTotalExpenseAmounts.categoryName,
                            expenseCategoryAndTheirTotalExpenseAmounts.totalAmount
                        )
                    )
                }

                lifecycleScope.launch {

                    delay(500)

                    if (data.isNotEmpty()) {

                        binding.chart.show()
                        pie.data(data)
                    } else {

                        binding.chart.hide()
                        showToast(requireContext(), getString(R.string.no_data_available))
                    }

                }
            }
        }
    }

    private fun loadCustomDateRangeValueFromSharedPreference() {

        selectedCustomDateRangeMenu =
            requireActivity().loadAnyValueFromSharedPreference(
                CustomDateRange::class.java,
                Constants.CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_NAME,
                Constants.CUSTOM_DATE_RANGE_FOR_GRAPH_FRAGMENT_SHARED_PREF_KEY
            )

        if (selectedCustomDateRangeMenu == null) {

            selectedCustomDateRangeMenu = CustomDateRange.ALL_TIME
        }

    }


    private fun initListeners() {


        binding.clearSelectionFAB.setOnClickListener {

            initialState()
            binding.selectAllFAB.enable()
        }

        binding.selectAllFAB.setOnClickListener {

            makeAllTheCategoriesSelected()

        }

        binding.showHideRVBtn.setOnClickListener {

            if (isExpenseCategoryRVVisible) {

                hideExpenseCategoryRVBySlidingUp()

            } else {

                showExpenseCategoryRVBySlidingDown()
            }
        }

        binding.deepAnalyzeExpenseMenuBtn
            .setOnClickListener {

                showMenuForSelectingCustomTime(it)
            }

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showMenuForSelectingCustomTime(view: View) {

        Functions.showCustomDateRangeOptionMenu(
            requireActivity(),
            view
        ) { selectedMenu ->

            selectedCustomDateRangeMenu = selectedMenu
            handleDateRangeMenu(selectedMenu)

        }
    }


    private fun handleDateRangeMenu(selectedOption: CustomDateRange) {

        when (selectedOption) {

            CustomDateRange.ALL_TIME -> {

                isDateRangeSelected = false
                binding.toolbar.subtitle = getString(R.string.all_time)
                initChartData()
            }

            CustomDateRange.LAST_30_DAYS -> {

                isDateRangeSelected = true
                startDate = System.currentTimeMillis() - (30 * Constants.ONE_DAY_MILLISECONDS)
                endDate = System.currentTimeMillis()

                binding.toolbar.subtitle = getString(R.string.last_30_days)
                initChartData()
            }

            CustomDateRange.LAST_7_DAYS -> {

                isDateRangeSelected = true
                startDate = System.currentTimeMillis() - (7 * Constants.ONE_DAY_MILLISECONDS)
                endDate = System.currentTimeMillis()

                binding.toolbar.subtitle = getString(R.string.last_7_days)

                initChartData()
            }

            CustomDateRange.LAST_365_DAYS -> {

                isDateRangeSelected = true
                startDate = System.currentTimeMillis() - (365 * Constants.ONE_DAY_MILLISECONDS)
                endDate = System.currentTimeMillis()

                binding.toolbar.subtitle = getString(R.string.last_365_days)

                initChartData()
            }

            CustomDateRange.CUSTOM_DATE_RANGE -> {

                Functions.showDateRangePickerDialog(
                    startDate,
                    endDate,
                    {
                        requireActivity().supportFragmentManager
                    },
                    { date ->

                        startDate = date.first
                        endDate = date.second

                        isDateRangeSelected = true

                        binding.toolbar.subtitle =
                            "${
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    startDate
                                )
                            } - " +
                                    "${
                                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                            endDate
                                        )
                                    }"


                        initChartData()
                    }
                )

            }

            else -> {

                isDateRangeSelected = true

                val pair = Functions.getMillisecondsOfStartAndEndUsingConstants(
                    selectedOption
                )

                startDate = pair.first
                endDate = pair.second

                when (selectedOption) {

                    CustomDateRange.THIS_MONTH -> binding.toolbar.subtitle =
                        getString(R.string.this_month)

                    CustomDateRange.THIS_WEEK -> binding.toolbar.subtitle =
                        getString(R.string.this_week)

                    CustomDateRange.PREVIOUS_MONTH -> binding.toolbar.subtitle =
                        getString(R.string.previous_month)

                    CustomDateRange.PREVIOUS_WEEK -> binding.toolbar.subtitle =
                        getString(R.string.previous_week)

                    else -> {}
                }
                initChartData()
            }
        }

    }

    private fun hideExpenseCategoryRVBySlidingUp() {

        isExpenseCategoryRVVisible = false

        val animate = TranslateAnimation(
            0f,  // fromXDelta
            0f,  // toXDelta
            0f,  // fromYDelta
            -binding.expenseCategoriesRv.height.toFloat()// toYDelta
        )

        animate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {

                Log.d(TAG, "onAnimationEnd: ")
                binding.expenseCategoriesRv.hide()
                binding.expenseCategoriesRv.isEnabled = false
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }

        })

        animate.duration = 500
        animate.fillAfter = true
        binding.expenseCategoriesRv.startAnimation(animate)
        binding.showHideRVBtn.animate().rotation(0f).setDuration(500).start()

    }

    private fun showExpenseCategoryRVBySlidingDown() {

        isExpenseCategoryRVVisible = true

        binding.expenseCategoriesRv.isEnabled = true
        binding.expenseCategoriesRv.show()

        val animate = TranslateAnimation(
            0f,  // fromXDelta
            0f,  // toXDelta
            -binding.expenseCategoriesRv.height.toFloat(),  // fromYDelta
            0f // toYDelta
        )

        animate.duration = 500
        animate.fillAfter = true
        binding.expenseCategoriesRv.startAnimation(animate)
        binding.showHideRVBtn.animate().rotation(180f).setDuration(500).start()
    }

    private fun makeAllTheCategoriesSelected() {

        deepAnalyzeExpenseCategoryAdapter.currentList.forEach { expenseCategory ->

            expenseCategory.isSelected = true
        }

        deepAnalyzeExpenseCategoryAdapter.notifyItemRangeChanged(
            0,
            deepAnalyzeExpenseCategoryAdapter.currentList.size
        )

        binding.selectAllFAB.disable()
        binding.clearSelectionFAB.enable()

        initChartData()
    }

    private fun setUpRecyclerView() {

        binding.expenseCategoriesRv.apply {

            setHasFixedSize(true)
            adapter = deepAnalyzeExpenseCategoryAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
        }

        deepAnalyzeExpenseCategoryAdapter.setOnClickListener(this)
    }

    private fun getAllExpenseCategories() {

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner) { expenseCategories ->

                deepAnalyzeExpenseCategoryAdapter.submitList(expenseCategories)

                initialState()
            }
    }

    private fun initialState() {

        deepAnalyzeExpenseCategoryAdapter.currentList.forEach { expenseCategory ->

            expenseCategory.isSelected = false
        }

        deepAnalyzeExpenseCategoryAdapter.notifyItemRangeChanged(
            2,
            deepAnalyzeExpenseCategoryAdapter.currentList.size
        )

        deepAnalyzeExpenseCategoryAdapter.currentList[0].isSelected = true
        deepAnalyzeExpenseCategoryAdapter.currentList[1].isSelected = true
        deepAnalyzeExpenseCategoryAdapter.notifyItemRangeChanged(0, 2)

        binding.clearSelectionFAB.disable()

        isDateRangeSelected = selectedCustomDateRangeMenu != CustomDateRange.ALL_TIME

        initChartData()
    }

    private fun initChartData() {

        val selectedCategories =
            deepAnalyzeExpenseCategoryAdapter.currentList.filter { expenseCategory ->
                expenseCategory.isSelected
            }.map { it.key }

        if (!isDateRangeSelected) {

            expenseGraphDataViewModel.getTotalExpenseAmountWithTheirExpenseCategoryNamesForSelectedExpenseCategories(
                selectedCategories
            )
        } else {

            expenseGraphDataViewModel.getTotalExpenseAmountWithTheirExpenseCategoryNamesForSelectedExpenseCategoriesByDateRange(
                selectedCategories,
                startDate,
                endDate + Constants.ONE_DAY_MILLISECONDS
            )
        }
    }

    private var changeChartDataJob: Job? = null

    override fun onItemClick(expenseCategory: ExpenseCategory, position: Int) {

        if (expenseCategory.isSelected) {

            if (getSelectedExpenseCategoryCount() <= 2) {

                showToast(
                    requireContext(),
                    getString(R.string.minimum_two_categories_needs_to_be_selected)
                )
                return
            }
        }

        expenseCategory.isSelected = !expenseCategory.isSelected
        deepAnalyzeExpenseCategoryAdapter.notifyItemChanged(position)

        if (getSelectedExpenseCategoryCount() == deepAnalyzeExpenseCategoryAdapter.currentList.size) {

            binding.selectAllFAB.disable()
        } else {

            binding.selectAllFAB.enable()
        }

        if (getSelectedExpenseCategoryCount() > 2) {

            binding.clearSelectionFAB.enable()
        }

        changeChartDataJob = lifecycleScope.launch {

            changeChartDataJob.executeAfterDelay(250) {

                initChartData()
            }
        }
    }

    private fun getSelectedExpenseCategoryCount(): Int {

        return deepAnalyzeExpenseCategoryAdapter.currentList.filter { e -> e.isSelected }.size
    }

    private fun setUpPieChart() {

        Log.d(TAG, "setUpPieChart: ")

        pie = AnyChart.pie()

        binding.chart.setProgressBar(binding.progressBar)

        pie.title().enabled(true)
            .fontColor("#212121")

        //pie.title("Expenses on each category")

        pie.labels().position("outside")

        pie.legend().title().enabled(false)
//        pie.legend().title()
//            .text("Expense categories")
//            .padding(0.0, 0.0, 10.0, 0.0)

        if (getSelectedExpenseCategoryCount() >= 15) {

            pie.legend().enabled(false)
        }

        pie.legend()
            .position("bottom")
            .itemsLayout(LegendLayout.HORIZONTAL_EXPANDABLE)
            .align(Align.LEFT)

        binding.chart.setZoomEnabled(true)

        binding.chart.setChart(pie)

    }

    private fun FloatingActionButton.disable() {

        this.isEnabled = false
        this.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.divider_color))
    }

    private fun FloatingActionButton.enable() {

        this.isEnabled = true
        this.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.teal_200))

    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.d(TAG, "onDestroyView: ")

        _binding = null
    }

}
