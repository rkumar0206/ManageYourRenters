package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome.BudgetRVAdapter
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.data.ShowExpenseBottomSheetTagsEnum
import com.rohitthebest.manageyourrenters.data.filter.BudgetAndIncomeExpenseFilter
import com.rohitthebest.manageyourrenters.data.filter.ExpenseFilterDto
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.FragmentBudgetBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.MonthAndYearPickerDialog
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.ShowPaymentMethodSelectorDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.IncomeViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeVisibilityOfViewOnScrolled
import com.rohitthebest.manageyourrenters.utils.convertToJsonString
import com.rohitthebest.manageyourrenters.utils.format
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isNotValid
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "BudgetAndIncomeFragment"

@AndroidEntryPoint
class BudgetAndIncomeOverviewFragment : Fragment(R.layout.fragment_budget), View.OnClickListener,
    BudgetRVAdapter.OnClickListener, MonthAndYearPickerDialog.OnMonthAndYearDialogDismissListener,
    ShowPaymentMethodSelectorDialogFragment.OnClickListener {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val budgetViewModel by viewModels<BudgetViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val incomeViewModel by viewModels<IncomeViewModel>()

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0
    private var oldestYearWhenBudgetWasSaved = 2000
    private var monthList: List<String> = emptyList()

    private lateinit var budgetAdapter: BudgetRVAdapter

    private var expenseFilterDto: ExpenseFilterDto? = null

    private var totalIncome = 0.0
    private var totalExpense = 0.0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBudgetBinding.bind(view)

        monthList = resources.getStringArray(R.array.months).toList()
        budgetAdapter = BudgetRVAdapter()

        initListeners()
        setUpRecyclerView()

        binding.progressBar.show()

        observeBudgetList()

        initUI()
    }

    private fun observeBudgetList() {

        budgetViewModel.allBudgetListByMonthAndYear.observe(viewLifecycleOwner) { budgets ->

            binding.noBudgetAddedTV.isVisible = budgets.isEmpty()

            if (budgets.isNotEmpty()) {
                binding.iabAddBudgetFAB.text = getString(R.string.modify_budgets)
            } else {
                binding.iabAddBudgetFAB.text = getString(R.string.add_budget)
            }

            budgetAdapter.submitList(budgets)
            binding.progressBar.hide()
        }
    }

    private fun setUpRecyclerView() {

        binding.iabBudgetRV.apply {

            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext())
            adapter = budgetAdapter
            changeVisibilityOfViewOnScrolled(binding.iabAddBudgetFAB)
        }

        budgetAdapter.setOnClickListener(this)
    }

    override fun onItemClick(budget: Budget) {

        showExpensesInBottomSheet(budget.expenseCategoryKey)
    }

    override fun onMenuBtnClick(budget: Budget) {

        requireContext().showToast(budget.currentExpenseAmount)
    }

    private fun initUI() {

        selectedMonth = WorkingWithDateAndTime.getCurrentMonth()
        selectedYear = WorkingWithDateAndTime.getCurrentYear()

        binding.iabDateTV.text =
            getString(R.string.month_and_year, monthList[selectedMonth], selectedYear.toString())

        binding.progressBar.show()

        lifecycleScope.launch {
            delay(300)
            handleUiAfterDateChange()
        }

        budgetViewModel.getTheOldestSavedBudgetYear().observe(viewLifecycleOwner) { year ->
            try {
                if (year != null) {
                    oldestYearWhenBudgetWasSaved = year
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.nextMonthBtn.setOnClickListener(this)
        binding.previousMonthBtn.setOnClickListener(this)
        binding.iabIncomeAddBtn.setOnClickListener(this)
        binding.iabBudgetMCV.setOnClickListener(this)
        binding.iabSavingMCV.setOnClickListener(this)
        binding.iabExpenseMCV.setOnClickListener(this)
        binding.iabIncomeMCV.setOnClickListener(this)
        binding.iabAddBudgetFAB.setOnClickListener(this)
        binding.monthMCV.setOnClickListener(this)

        binding.toolbar.menu.findItem(R.id.menu_filter_income_and_budget_overview)
            .setOnMenuItemClickListener {

                showPaymentMethodSelectorDialog()

                true
            }

        binding.toolbar.menu.findItem(R.id.menu_budget_and_income_graph)
            .setOnMenuItemClickListener {

                openGraphFragment()
                true
            }
    }

    private fun openGraphFragment() {

        findNavController().navigate(R.id.action_budgetAndIncomeFragment_to_budgetAndIncomeGraphFragment)
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

        binding.toolbar.menu.findItem(R.id.menu_filter_income_and_budget_overview).apply {

            if (selectedPaymentMethods.isNullOrEmpty()) {
                this.icon =
                    ContextCompat.getDrawable(requireContext(), R.drawable.baseline_filter_list_24)

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

        // apply payment filter on income and expense
        handleUiAfterDateChange()
    }


    override fun onClick(v: View?) {

        when (v?.id) {

            binding.nextMonthBtn.id -> {

                handleNextDateButton()
            }

            binding.previousMonthBtn.id -> {
                handlePreviousDateButton()
            }

            binding.iabIncomeAddBtn.id -> {

                val bundle = Bundle()

                bundle.putBoolean(Constants.IS_FOR_EDIT, false)
                bundle.putInt(Constants.INCOME_MONTH_KEY, selectedMonth)
                bundle.putInt(Constants.INCOME_YEAR_KEY, selectedYear)

                requireActivity().supportFragmentManager.let { fragmentManager ->

                    AddIncomeBottomSheetFragment.newInstance(
                        bundle
                    ).apply {
                        show(fragmentManager, TAG)
                    }
                }
            }

            binding.iabAddBudgetFAB.id -> {

                val action =
                    BudgetAndIncomeOverviewFragmentDirections.actionBudgetAndIncomeFragmentToAddBudgetFragment(
                        selectedMonth, selectedYear
                    )

                findNavController().navigate(action)
            }

            binding.iabIncomeMCV.id -> {

                val action =
                    BudgetAndIncomeOverviewFragmentDirections.actionBudgetAndIncomeFragmentToIncomeFragment(
                        selectedMonth, selectedYear
                    )
                findNavController().navigate(action)
            }

            binding.monthMCV.id -> {

                handleMonthAndYearSelection()
            }

            binding.iabExpenseMCV.id -> {
                showExpensesInBottomSheet()
            }
        }
    }

    private fun showExpensesInBottomSheet(categoryKey: String = "") {

        val datePairForExpense =
            WorkingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonthForGivenMonthAndYear(
                selectedMonth, selectedYear
            )

        if (categoryKey.isNotValid()) {

            // show all the expenses of the category for which budget is added

            budgetViewModel.getAllExpenseCategoryOfBudgetsByMonthAndYear(
                selectedMonth, selectedYear
            )
                .observe(viewLifecycleOwner) { categoryKeys ->

                    if (categoryKeys.isNotEmpty()) {

                        val action =
                            BudgetAndIncomeOverviewFragmentDirections.actionBudgetAndIncomeFragmentToShowExpenseBottomSheetFragment(
                                null,
                                CustomDateRange.CUSTOM_DATE_RANGE,
                                datePairForExpense.first,
                                datePairForExpense.second,
                                ShowExpenseBottomSheetTagsEnum.BUDGET_AND_INCOME_FRAGMENT,
                                BudgetAndIncomeExpenseFilter(
                                    categoryKeys,
                                    if (expenseFilterDto == null) emptyList() else expenseFilterDto!!.paymentMethods
                                )
                            )

                        findNavController().navigate(action)

                    } else {
                        showToast(
                            requireContext(),
                            getString(R.string.click_on_add_budget_button_to_add_budgets_for_this_month)
                        )
                    }
                }

        } else {

            // show all the expenses for a clicked budget category

            val action =
                BudgetAndIncomeOverviewFragmentDirections.actionBudgetAndIncomeFragmentToShowExpenseBottomSheetFragment(
                    null,
                    CustomDateRange.CUSTOM_DATE_RANGE,
                    datePairForExpense.first,
                    datePairForExpense.second,
                    ShowExpenseBottomSheetTagsEnum.BUDGET_AND_INCOME_FRAGMENT,
                    BudgetAndIncomeExpenseFilter(
                        listOf(categoryKey),
                        if (expenseFilterDto == null) emptyList() else expenseFilterDto!!.paymentMethods
                    )
                )

            findNavController().navigate(action)
        }
    }

    private fun handleMonthAndYearSelection() {

        val bundle = Bundle()
        bundle.putInt(Constants.MONTH_YEAR_PICKER_MONTH_KEY, selectedMonth)
        bundle.putInt(Constants.MONTH_YEAR_PICKER_YEAR_KEY, selectedYear)
        bundle.putInt(
            Constants.MONTH_YEAR_PICKER_MIN_YEAR_KEY,
            oldestYearWhenBudgetWasSaved - 4
        )
        bundle.putInt(
            Constants.MONTH_YEAR_PICKER_MAX_YEAR_KEY,
            WorkingWithDateAndTime.getCurrentYear()
        )

        requireActivity().supportFragmentManager.let { fm ->
            MonthAndYearPickerDialog.newInstance(
                bundle
            ).apply {
                show(fm, TAG)
            }
        }.setOnMonthAndYearDialogDismissListener(this)
    }

    override fun onMonthAndYearDialogDismissed(
        isMonthAndYearSelected: Boolean,
        selectedMonth: Int,
        selectedYear: Int
    ) {

        if (isMonthAndYearSelected) {

            this.selectedMonth = selectedMonth
            this.selectedYear = selectedYear
            handleUiAfterDateChange()
        }
    }


    private fun handlePreviousDateButton() {

        if (selectedMonth == 0) {
            selectedYear -= 1
        }

        selectedMonth = WorkingWithDateAndTime.getPreviousMonth(selectedMonth)

        handleUiAfterDateChange()
    }

    private fun handleNextDateButton() {

        if (selectedMonth == 11) {
            selectedYear += 1
        }

        selectedMonth = WorkingWithDateAndTime.getNextMonth(selectedMonth)
        handleUiAfterDateChange()
    }

    private fun handleUiAfterDateChange() {

        binding.progressBar.show()

        setMonthAndYearInTextView()

        // get all budgets (this list is being observed in the method observeBudgetList())
        budgetViewModel.getAllBudgetsByMonthAndYear(
            selectedMonth,
            selectedYear,
            if (expenseFilterDto == null) emptyList() else expenseFilterDto!!.paymentMethods
        )

        binding.progressBar.show()

        lifecycleScope.launch {
            delay(350)
            budgetAdapter.notifyDataSetChanged()
        }

        showTotalExpense()
        showTotalBudget()
        showTotalIncomeAdded()

        lifecycleScope.launch {
            delay(250)
            showTotalSavings()
        }
    }

    private fun showTotalSavings() {

        binding.iabSavingValueTV.text = (totalIncome - totalExpense).format(2)
    }

    private fun showTotalIncomeAdded() {

        if (expenseFilterDto != null && expenseFilterDto!!.paymentMethods.isNotEmpty()) {

            incomeViewModel.getAllIncomesByMonthAndYear(selectedMonth, selectedYear)
                .observe(viewLifecycleOwner) { incomes ->

                    try {
                        if (incomes.isEmpty()) {
                            updateIncomeTotalUI(0.0)
                        } else {
                            val tempIncome = incomeViewModel.applyFilterByPaymentMethods(
                                expenseFilterDto!!.paymentMethods, incomes
                            )

                            updateIncomeTotalUI(tempIncome.sumOf { it.income })
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                        totalIncome = 0.0
                        binding.iabIncomeValueTV.text = getString(R.string._0_0)
                    }
                }
        } else {
            incomeViewModel.getTotalIncomeAddedByMonthAndYear(
                selectedMonth, selectedYear
            ).observe(viewLifecycleOwner) { totalIncomeAdded ->

                try {
                    updateIncomeTotalUI(totalIncomeAdded)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    totalIncome = 0.0
                    binding.iabIncomeValueTV.text = getString(R.string._0_0)
                }
            }
        }
    }

    private fun updateIncomeTotalUI(totalIncomeAdded: Double) {

        totalIncome = totalIncomeAdded
        binding.iabIncomeValueTV.text = totalIncomeAdded.format(2)
        showTotalSavings()
    }


    private fun showTotalExpense() {

        val datePairForExpense =
            WorkingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonthForGivenMonthAndYear(
                selectedMonth, selectedYear
            )

        budgetViewModel.getAllExpenseCategoryOfBudgetsByMonthAndYear(
            selectedMonth, selectedYear
        )
            .observe(viewLifecycleOwner) { categoryKeys ->

                if (categoryKeys.isNotEmpty()) {

                    if (expenseFilterDto != null && expenseFilterDto!!.paymentMethods.isNotEmpty()) {

                        // get total expense by filtering the list by paymentMethods
                        expenseViewModel.getExpenseByCategoryKeysAndDateRange(
                            categoryKeys,
                            datePairForExpense.first,
                            datePairForExpense.second + Constants.ONE_DAY_MILLISECONDS
                        ).observe(viewLifecycleOwner) { expenses ->

                            if (expenses.isEmpty()) {
                                updateExpenseTotalUI(0.0)
                            } else {
                                val tempExpense = expenseViewModel.applyFilterByPaymentMethods(
                                    expenseFilterDto!!.paymentMethods,
                                    expenses
                                )

                                updateExpenseTotalUI(tempExpense.sumOf { it.amount })
                            }
                        }

                    } else {

                        expenseViewModel.getTotalExpenseByCategoryKeysAndDateRange(
                            categoryKeys,
                            datePairForExpense.first,
                            datePairForExpense.second + Constants.ONE_DAY_MILLISECONDS
                        ).observe(viewLifecycleOwner) { total ->

                            try {
                                updateExpenseTotalUI(total)
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                                totalExpense = 0.0
                                binding.iabExpenseValueTV.text = getString(R.string._0_0)
                            }
                        }
                    }
                } else {
                    binding.iabExpenseValueTV.text = getString(R.string._0_0)
                    totalExpense = 0.0
                }
            }
    }

    private fun updateExpenseTotalUI(total: Double) {

        totalExpense = total
        binding.iabExpenseValueTV.text = total.format(2)
        showTotalSavings()
    }

    private fun showTotalBudget() {

        budgetViewModel.getTotalBudgetByMonthAndYear(selectedMonth, selectedYear)
            .observe(viewLifecycleOwner) { totalBudget ->
                try {
                    binding.iabBudgetValueTV.text = totalBudget.format(2)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    binding.iabBudgetValueTV.text = getString(R.string._0_0)
                }
            }
    }


    private fun setMonthAndYearInTextView() {

        binding.iabDateTV.text =
            getString(R.string.month_and_year, monthList[selectedMonth], selectedYear.toString())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
