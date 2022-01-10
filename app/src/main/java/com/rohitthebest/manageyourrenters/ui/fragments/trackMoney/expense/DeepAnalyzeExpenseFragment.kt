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
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentDeepAnalyzeExpenseBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


private const val TAG = "DeepAnalyzeExpenseFragm"

@AndroidEntryPoint
class DeepAnalyzeExpenseFragment : Fragment(R.layout.fragment_deep_analyze_expense),
    DeepAnalyzeExpenseCategoryAdapter.OnClickListener {

    private var _binding: FragmentDeepAnalyzeExpenseBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()

    private lateinit var pie: Pie

    private lateinit var deepAnalyzeExpenseCategoryAdapter: DeepAnalyzeExpenseCategoryAdapter

    private var isExpenseCategoryRVVisible = true

    private var isDateRangeSelected = false
    private var isBackButtonPressed = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDeepAnalyzeExpenseBinding.bind(view)

        deepAnalyzeExpenseCategoryAdapter = DeepAnalyzeExpenseCategoryAdapter()

        setUpPieChart()

        setUpRecyclerView()

        getAllExpenseCategories()

        initListeners()
    }

    private var startDate = System.currentTimeMillis() - (Constants.ONE_DAY_MILLISECONDS * 30)
    private var endDate = System.currentTimeMillis()

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

        binding.toolbar.menu.findItem(R.id.menu_select_range_deep_analyze_expense)
            .setOnMenuItemClickListener {

                handleDateRangeMenu()

                true
            }

        binding.toolbar.menu.findItem(R.id.menu_clear_date_range_deep_analyze_expense)
            .setOnMenuItemClickListener {

                clearDateRange()
                true
            }

        binding.toolbar.setNavigationOnClickListener {

            isBackButtonPressed = true
            requireActivity().onBackPressed()
        }
    }

    // todo : complete this -
    // todo : 1 : remove menu clear date range
    // todo : 2 : rename the date range menu to select custom time
    // todo : 3 : handle the click on menu

    private fun clearDateRange() {

        binding.toolbar.menu.findItem(R.id.menu_select_range_deep_analyze_expense)
            .setIcon(R.drawable.ic_baseline_date_range_24)

        isDateRangeSelected = false
        binding.toolbar.subtitle = ""

        initChartData()

    }

    private fun handleDateRangeMenu() {

        Functions.showCustomDateRangeOptionMenu(
            requireActivity(),
            binding.toolbar.menu.findItem(R.id.menu_select_range_deep_analyze_expense).actionView
        ) { selectedOption ->

            when (selectedOption) {

                CustomDateRange.ALL_TIME -> {

                    clearDateRange()
                }

                CustomDateRange.LAST_30_DAYS -> {


                }
            }
        }

        Functions.showDateRangePickerDialog(
            startDate,
            endDate,
            {
                requireActivity().supportFragmentManager
            },
            { date ->

                binding.toolbar.menu.findItem(R.id.menu_select_range_deep_analyze_expense)
                    .setIcon(R.drawable.ic_baseline_date_range_24_coloured)

                startDate = date.first
                endDate = date.second

                isDateRangeSelected = true

                binding.toolbar.subtitle =
                    "${WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(startDate)} - " +
                            "${
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    endDate
                                )
                            }"

                initChartData()

            }
        )

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

        initChartData()
    }

    private fun initChartData() {

        val selectedCategories =
            deepAnalyzeExpenseCategoryAdapter.currentList.filter { expenseCategory ->

                expenseCategory.isSelected
            }

        getTotalExpenseAmount(selectedCategories)

        prepareChart(selectedCategories)

    }

    private fun prepareChart(selectedCategories: List<ExpenseCategory>) {

        if (selectedCategories.isNotEmpty()) {

            val data = ArrayList<DataEntry>()

            selectedCategories.forEach { expenseCategory ->

                lifecycleScope.launch {

                    if (!isDateRangeSelected) {

                        // get the all time amount

                        try {
                            expenseViewModel.getExpenseAmountSumByExpenseCategoryKey(
                                expenseCategory.key
                            ).collect { amount ->

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

                    } else {

                        // get the amount by the date range selected

                        try {
                            expenseViewModel.getExpenseAmountSumByExpenseCategoryByDateRange(
                                expenseCategory.key,
                                startDate,
                                endDate + Constants.ONE_DAY_MILLISECONDS
                            ).collect { amount ->

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
            }

            lifecycleScope.launch {

                delay(500)

                if (data.isNotEmpty()) {

                    binding.chart.show()
                    pie.data(data)
                    //binding.chart.setChart(pie)
                } else {

                    binding.chart.hide()
                    showToast(requireContext(), "No Data available!!!")
                }

            }

        }

    }

    private fun getTotalExpenseAmount(selectedCategories: List<ExpenseCategory>) {

        var total = 0.0

        if (!isDateRangeSelected) {

            selectedCategories.forEach { expenseCategory ->

                expenseViewModel.getTotalExpenseAmountByExpenseCategory(expenseCategory.key)
                    .observe(viewLifecycleOwner) { amount ->

                        try {

                            total += amount
                        } catch (e: NullPointerException) {

                            e.printStackTrace()
                        }

                    }
            }
        } else {

            selectedCategories.forEach { expenseCategory ->


                expenseViewModel.getTotalExpenseAmountByCategoryKeyAndDateRange(
                    expenseCategory.key,
                    startDate,
                    endDate + Constants.ONE_DAY_MILLISECONDS
                )
                    .observe(viewLifecycleOwner) { amount ->

                        try {

                            total += amount
                        } catch (e: Exception) {

                            e.printStackTrace()
                        }
                    }

            }

        }

        lifecycleScope.launch {
            delay(500)
            pie.title("Total expense amount: $total")

        }

    }

    private var changeChartDataJob: Job? = null

    override fun onItemClick(expenseCategory: ExpenseCategory, position: Int) {

        if (expenseCategory.isSelected) {

            if (getSelectedExpenseCategoryCount() <= 2) {

                showToast(requireContext(), "Minimum two categories has to be selected")
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

        try {

            if (changeChartDataJob != null && changeChartDataJob?.isActive == true) {

                changeChartDataJob?.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

            changeChartDataJob = lifecycleScope.launch {

                delay(250)

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

    override fun onPause() {
        super.onPause()

        Log.d(TAG, "onPause: ")

        if (!isBackButtonPressed) {

            requireActivity().onBackPressed()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        Log.d(TAG, "onDestroyView: ")

        _binding = null
    }

}
