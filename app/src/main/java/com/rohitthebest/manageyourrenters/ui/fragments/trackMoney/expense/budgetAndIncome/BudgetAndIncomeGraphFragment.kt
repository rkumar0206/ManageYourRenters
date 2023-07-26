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
import com.rohitthebest.manageyourrenters.data.filter.ExpenseFilterDto
import com.rohitthebest.manageyourrenters.databinding.FragmentBudgetAndIncomeGraphBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.ShowPaymentMethodSelectorDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetAndIncomeGraphViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.convertToJsonString
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.setListToSpinner
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "BudgetAndIncomeGraphFragment"

@AndroidEntryPoint
class BudgetAndIncomeGraphFragment : Fragment(R.layout.fragment_budget_and_income_graph),
    ShowPaymentMethodSelectorDialogFragment.OnClickListener {

    private var _binding: FragmentBudgetAndIncomeGraphBinding? = null
    private val binding get() = _binding!!

    private val budgetAndIncomeGraphViewModel by viewModels<BudgetAndIncomeGraphViewModel>()
    private val budgetViewModel by viewModels<BudgetViewModel>()

    private var oldestYearWhenBudgetWasSaved = 2000
    private var selectedYear = 2020

    private var expenseFilterDto: ExpenseFilterDto? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBudgetAndIncomeGraphBinding.bind(view)

        initUI()
        initListeners()

        observeBarDataForBudgetIncomeAndExpense()
    }

    private fun observeBarDataForBudgetIncomeAndExpense() {

        budgetAndIncomeGraphViewModel.incomeBudgetAndExpenseBarEntryData.observe(viewLifecycleOwner) { data ->

            binding.progressBar.show()

            data[FirestoreCollectionsConstants.BUDGETS]?.let { budgets ->
                data[FirestoreCollectionsConstants.INCOMES]?.let { incomes ->
                    data[FirestoreCollectionsConstants.EXPENSES]?.let { expenses ->
                        initializeBarDataAndGraph(
                            budgets,
                            incomes,
                            expenses
                        )
                    }
                }
            }
        }
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
                handleGraphData()
            }, {}
        )
    }

    private fun handleGraphData() {

        budgetAndIncomeGraphViewModel.getBarEntryDataForIncomeBudgetAndExpenseByYear(
            selectedYear, expenseFilterDto?.paymentMethods ?: emptyList()
        )
    }


    private fun initializeBarDataAndGraph(
        budgetBarEntry: MutableList<BarEntry>,
        incomeBarEntry: MutableList<BarEntry>,
        expenseBarEntry: MutableList<BarEntry>
    ) {

        val barDataSetBudget = BarDataSet(budgetBarEntry, getString(R.string.budget))
        barDataSetBudget.color = ContextCompat.getColor(requireContext(), R.color.blue_text_color)

        val barDataSetIncome = BarDataSet(incomeBarEntry, getString(R.string.income))
        barDataSetIncome.color = ContextCompat.getColor(requireContext(), R.color.color_green)

        val barDataSetExpense = BarDataSet(expenseBarEntry, getString(R.string.expense))
        barDataSetExpense.color = ContextCompat.getColor(requireContext(), R.color.color_Red)

        val barData = BarData(barDataSetIncome, barDataSetBudget, barDataSetExpense)

        binding.chart.data = barData
        val months = resources.getStringArray(R.array.months)

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
            description.text = context.getString(R.string.income_vs_budget_vs_expense)
            animateY(600)
        }

        binding.toolbar.subtitle = requireContext().getString(R.string.income_vs_budget_vs_expense)
        binding.progressBar.hide()
        binding.chart.invalidate()
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.menu.findItem(R.id.menu_saving_growth).setOnMenuItemClickListener {

            showToast(requireContext(), getString(R.string.coming_soon))
            true
        }

        binding.toolbar.menu.findItem(R.id.menu_filter_income_budget_graph_by_paymentMethods)
            .setOnMenuItemClickListener {

                showPaymentMethodSelectorDialog()

                true
            }

    }

    private fun showPaymentMethodSelectorDialog() {

        requireActivity().supportFragmentManager.let { fragmentManager ->

            val bundle = Bundle()
            bundle.putString(
                Constants.EXPENSE_FILTER_KEY,
                if (expenseFilterDto == null) "" else expenseFilterDto.convertToJsonString()
            )

            ShowPaymentMethodSelectorDialogFragment.newInstance(
                bundle
            ).apply {
                show(fragmentManager, TAG)
            }.setOnClickListener(this)
        }
    }

    override fun onFilterApply(selectedPaymentMethods: List<String>?) {

        binding.toolbar.menu.findItem(R.id.menu_filter_income_budget_graph_by_paymentMethods)
            .apply {

                if (selectedPaymentMethods.isNullOrEmpty()) {
                    this.icon =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.baseline_filter_list_24
                        )

                    expenseFilterDto = null

                } else {
                    this.icon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.baseline_filter_list_colored_24
                    )

                    expenseFilterDto = ExpenseFilterDto()
                    expenseFilterDto!!.isPaymentMethodEnabled = true
                    expenseFilterDto!!.paymentMethods = selectedPaymentMethods
                }
            }

        handleGraphData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
