package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentBudgetAndIncomeGraphBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.IncomeViewModel
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.setListToSpinner
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class BudgetAndIncomeGraphFragment : Fragment(R.layout.fragment_budget_and_income_graph) {

    private var _binding: FragmentBudgetAndIncomeGraphBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val budgetViewModel by viewModels<BudgetViewModel>()
    private val incomeViewModel by viewModels<IncomeViewModel>()

    private var oldestYearWhenBudgetWasSaved = 2000
    private var selectedYear = 2020

    private var isRefreshEnabled = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBudgetAndIncomeGraphBinding.bind(view)

        initUI()
        initListeners()

        val barDataSetBudget = BarDataSet(getRandomArrayListOfBarEntry(), "Budget")
        barDataSetBudget.color = ContextCompat.getColor(requireContext(), R.color.blue_text_color)

        val barDataSetIncome = BarDataSet(getRandomArrayListOfBarEntry(), "Income")
        barDataSetIncome.color = ContextCompat.getColor(requireContext(), R.color.color_green)

        val barDataSetExpense = BarDataSet(getRandomArrayListOfBarEntry(), "Expense")
        barDataSetExpense.color = ContextCompat.getColor(requireContext(), R.color.color_orange)

        val barData = BarData(barDataSetIncome, barDataSetBudget, barDataSetExpense)

        binding.chart.data = barData

        val months = arrayOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )

        val xAxis = binding.chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(months)
        xAxis.setCenterAxisLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1F
        xAxis.isGranularityEnabled = true

        binding.chart.isDragEnabled = true
        binding.chart.setVisibleXRangeMaximum(3f)

        val barSpace = 0.05f
        val groupSpace = 0.31f
        barData.barWidth = 0.18f

        binding.chart.apply {
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum =
                0 + binding.chart.barData.getGroupWidth(groupSpace, barSpace) * 12
            axisLeft.axisMinimum = 0f

            groupBars(0f, groupSpace, barSpace)
            description.text = "Income vs Budget vs Expense"
            animateY(600)
        }

        binding.progressBar.hide()
        binding.chart.invalidate()

    }

    private fun initUI() {

        budgetViewModel.getTheOldestSavedBudgetYear().observe(viewLifecycleOwner) { year ->
            try {
                oldestYearWhenBudgetWasSaved = year ?: WorkingWithDateAndTime.getCurrentYear()

                // set up year spinner
                initYearSpinner(
                    oldestYearWhenBudgetWasSaved,
                    WorkingWithDateAndTime.getCurrentYear()
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun initYearSpinner(startYear: Int, endYear: Int) {

        val yearList = ArrayList<Int>()

        for (year in endYear downTo startYear) {
            yearList.add(year)
        }

        binding.yearSpinner.setListToSpinner(
            context = requireContext(),
            list = yearList,
            position = { position ->

                selectedYear = yearList[position]
                loadData()
            }, {}
        )
    }

    private fun loadData() {

        if (isRefreshEnabled) {
            // todo: load budget, then income and then expense data according to month and year
        }
    }


    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun getRandomArrayListOfBarEntry(): ArrayList<BarEntry> {

        val list = ArrayList<BarEntry>()

        for (i in 1..12) {

            list.add(BarEntry(i.toFloat(), Random.nextInt(2000).toFloat()))
        }

        return list
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
