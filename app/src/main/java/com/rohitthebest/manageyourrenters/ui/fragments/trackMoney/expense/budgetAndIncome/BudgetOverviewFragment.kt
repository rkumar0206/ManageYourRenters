package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.content.res.ColorStateList
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
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.BudgetOverviewLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentBudgetOverviewBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetAndIncomeGraphViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getAppropriateBudgetSuggestionOrMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.setImageToImageViewUsingGlide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.format
import com.rohitthebest.manageyourrenters.utils.isNotValid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetOverviewFragment : Fragment(R.layout.fragment_budget_overview) {

    private var _binding: FragmentBudgetOverviewBinding? = null
    private val binding get() = _binding!!

    private val budgetViewModel by viewModels<BudgetViewModel>()
    private val budgetAndIncomeGraphViewModel by viewModels<BudgetAndIncomeGraphViewModel>()

    private var receivedBudgetKey = ""
    private var receivedMonth = 0
    private var receivedYear = 0
    private lateinit var receivedBudget: Budget
    private lateinit var includeLayoutBinding: BudgetOverviewLayoutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBudgetOverviewBinding.bind(view)

        includeLayoutBinding = binding.includeLayout

        initListeners()
        getMessage()
        observeBudget()
        observeGraph()
    }

    private fun observeGraph() {

        budgetAndIncomeGraphViewModel.budgetExpense.observe(viewLifecycleOwner) { data ->
            setUpGraph(data)
        }
    }

    private fun setUpGraph(dataEntry: List<BarEntry>) {

        val barDataSet = BarDataSet(dataEntry, getString(R.string.expenses))
        barDataSet.color = ContextCompat.getColor(requireContext(), R.color.purple_500)

        val barData = BarData(barDataSet)
        includeLayoutBinding.budgetPerDayChart.data = barData

        val days = ArrayList<String>()

        val numberOfDaysInMonth =
            WorkingWithDateAndTime.getNumberOfDaysInMonth(receivedMonth, receivedYear)

        for (i in 0..numberOfDaysInMonth) {

            days.add(
                when {

                    i.toString().startsWith("0") -> i.toString()
                    i.toString().endsWith("1") -> i.toString() + "st"
                    i.toString().endsWith("2") -> i.toString() + "nd"
                    i.toString().endsWith("3") -> i.toString() + "rd"
                    else -> i.toString() + "th"
                }
            )
        }

        val xAxis = includeLayoutBinding.budgetPerDayChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(days)
        //xAxis.setCenterAxisLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1F
        xAxis.isGranularityEnabled = true

        barData.barWidth = 0.3f

        includeLayoutBinding.budgetPerDayChart.apply {
            xAxis.axisMinimum = 0f
            animateY(600)
        }

        includeLayoutBinding.budgetPerDayChart.isDragEnabled = true
        includeLayoutBinding.budgetPerDayChart.setVisibleXRangeMaximum(3f)


        includeLayoutBinding.budgetPerDayChart.description.text = "Per day expenses"
        includeLayoutBinding.budgetPerDayChart.invalidate()
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {
                BudgetOverviewFragmentArgs.fromBundle(it)
            }

            receivedBudgetKey = args?.budgetKeyMessage ?: ""
            receivedMonth = args?.month ?: 0
            receivedYear = args?.year ?: 0

            if (receivedBudgetKey.isNotValid() || receivedYear == 0) {
                showToast(requireContext(), getString(R.string.something_went_wrong))
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {

                budgetViewModel.getBudgetByKeyWithACategoryAndExpenseDetails(
                    budgetKey = receivedBudgetKey,
                    month = receivedMonth,
                    year = receivedYear,
                    selectedPaymentMethods = emptyList()
                )
            }

        }
    }

    private fun observeBudget() {

        budgetViewModel.budgetByKey.observe(viewLifecycleOwner) { budget ->

            receivedBudget = budget
            updateUI()
            budgetAndIncomeGraphViewModel.getEveryDayExpenseData(
                budget.expenseCategoryKey, receivedMonth, receivedYear
            )
        }
    }

    private fun updateUI() {

        if (!this::receivedBudget.isInitialized) {
            showToast(requireContext(), getString(R.string.something_went_wrong))
            requireActivity().onBackPressedDispatcher.onBackPressed()
        } else {

            binding.toolbar.title = receivedBudget.categoryName

            includeLayoutBinding.apply {


                budgetCatNameTV.text = receivedBudget.categoryName
                setImageToImageViewUsingGlide(
                    requireContext(),
                    budgetCategoryIV,
                    receivedBudget.categoryImageUrl,
                    {},
                    {}
                )

                val numberOfDaysLeftInMonth =
                    WorkingWithDateAndTime.getNumberOfDaysLeftInAnyMonth(
                        receivedBudget.month, receivedBudget.year
                    )

                var perDayExpense = if (numberOfDaysLeftInMonth != 0) {
                    (receivedBudget.budgetLimit - receivedBudget.currentExpenseAmount) / numberOfDaysLeftInMonth
                } else {
                    0.0
                }

                if (perDayExpense < 0) perDayExpense = 0.0

                budgetPerDayTV.text = binding.root.context.getString(
                    R.string.budgetPerDay, perDayExpense.format(2),
                    numberOfDaysLeftInMonth.toString()
                )

                amountSpentBudgetTV.text = receivedBudget.currentExpenseAmount.format(2)
                budgetLimitAmountTV.text = receivedBudget.budgetLimit.format(2)

                var amountLeft = receivedBudget.budgetLimit - receivedBudget.currentExpenseAmount
                if (amountLeft < 0) amountLeft = 0.0

                amountLeftBudgetTV.text =
                    getString(R.string.you_still_have_left_in_your_budget, amountLeft.format(2))

                val progressInPercent =
                    ((receivedBudget.currentExpenseAmount / receivedBudget.budgetLimit) * 100).toInt()

                budgetProgressBar.max = receivedBudget.budgetLimit.toInt()

                if (receivedBudget.currentExpenseAmount > receivedBudget.budgetLimit) {
                    budgetProgressBar.progress = receivedBudget.budgetLimit.toInt()
                } else {
                    budgetProgressBar.progress = receivedBudget.currentExpenseAmount.toInt()
                }

                changeUIRelatedToProgress(progressInPercent)

            }
        }
    }

    private fun changeUIRelatedToProgress(progressInPercent: Int) {

        // budget message
        includeLayoutBinding.budgetMessageTV.text = getAppropriateBudgetSuggestionOrMessage(
            requireContext(),
            progressInPercent,
            receivedBudget.currentExpenseAmount,
            receivedBudget.budgetLimit
        )

        when {

            (progressInPercent in 0..35) -> {
                val colorGreen = ContextCompat.getColor(
                    requireContext(),
                    R.color.color_green
                )
                includeLayoutBinding.budgetProgressBar.progressTintList = ColorStateList.valueOf(
                    colorGreen
                )

                includeLayoutBinding.budgetMessageIV.setImageResource(
                    R.drawable.baseline_check_circle_green_24
                )

            }

            (progressInPercent in 36..68) -> {
                val colorYellow = ContextCompat.getColor(
                    requireContext(),
                    R.color.color_yellow
                )
                includeLayoutBinding.budgetProgressBar.progressTintList = ColorStateList.valueOf(
                    colorYellow
                )

                includeLayoutBinding.budgetMessageIV.setImageResource(
                    R.drawable.baseline_check_circle_yellow_24
                )
            }

            else -> {
                val colorRed = ContextCompat.getColor(
                    requireContext(),
                    R.color.color_Red
                )
                includeLayoutBinding.budgetProgressBar.progressTintList = ColorStateList.valueOf(
                    colorRed
                )
                includeLayoutBinding.budgetMessageIV.setImageResource(
                    R.drawable.baseline_check_circle_red_24
                )
            }
        }

        if (progressInPercent >= 80) {
            includeLayoutBinding.budgetLimitAmountTV.changeTextColor(
                requireContext(), R.color.color_white
            )
        }
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
