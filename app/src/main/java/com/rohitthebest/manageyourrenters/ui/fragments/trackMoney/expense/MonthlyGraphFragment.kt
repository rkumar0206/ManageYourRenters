package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.enums.HoverMode
import com.anychart.enums.TooltipPositionMode
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentMonthlyGraphBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseGraphDataViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.setListToSpinner
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//private const val TAG = "MonthlyGraphFragment"

@AndroidEntryPoint
class MonthlyGraphFragment : Fragment(R.layout.fragment_monthly_graph) {

    private var _binding: FragmentMonthlyGraphBinding? = null
    private val binding get() = _binding!!

    private val expenseGraphDataViewModel by viewModels<ExpenseGraphDataViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()

    private lateinit var cartesian: Cartesian
    private var selectedYear = 2020

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyGraphBinding.bind(view)

        observeExpenseOfEachMonth()

        setUpChart()
        getStartAndEndYear()

        loadData()

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadData() {
        expenseGraphDataViewModel.getExpensesOfAllMonthsOfYear(selectedYear)
    }

    private fun observeExpenseOfEachMonth() {

        expenseGraphDataViewModel.expenseOfEachMonth.observe(viewLifecycleOwner) { expensePerMonth ->

            val data = ArrayList<DataEntry>()

            expensePerMonth.forEach { monthAndAmount ->
                data.add(ValueDataEntry(monthAndAmount.first, monthAndAmount.second))
            }

            lifecycleScope.launch {

                delay(500)

                cartesian.title(
                    getString(
                        R.string.monthly_expense_for_the_year,
                        selectedYear.toString()
                    )
                )
                cartesian.data(data)
            }
        }
    }

    private fun setUpChart() {

        binding.chart.setProgressBar(binding.progressBar)

        cartesian = AnyChart.vertical()

        cartesian.animation(true)
        cartesian.title("")

        cartesian.yScale().minimum(0.0)

        //cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }")

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)

        cartesian.xAxis(0).title(getString(R.string.month))
        cartesian.yAxis(0).title(getString(R.string.expense))

        binding.chart.setChart(cartesian)
    }

    private fun getStartAndEndYear() {

        expenseViewModel.getAllExpenses().observe(viewLifecycleOwner) { expenses ->

            val latestExpense = expenses.first()
            val firstEverExpense = expenses.last()

            val startYear =
                WorkingWithDateAndTime.getDateMonthYearByTimeInMillis(firstEverExpense.created).third
            val endYear =
                WorkingWithDateAndTime.getDateMonthYearByTimeInMillis(latestExpense.created).third

            initYearSpinner(startYear, endYear)
        }
    }

    private fun initYearSpinner(startYear: Int, endYear: Int) {

        val yearList = ArrayList<Int>()

        for (year in endYear downTo startYear) {
            yearList.add(year)
        }

        binding.yearSpinner.setListToSpinner(
            requireContext(),
            yearList,
            { position ->

                selectedYear = yearList[position]
                loadData()
            }, {}
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

