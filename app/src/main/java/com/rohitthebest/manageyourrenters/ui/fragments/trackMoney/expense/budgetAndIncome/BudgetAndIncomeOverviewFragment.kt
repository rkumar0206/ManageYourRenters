package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.view.View
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
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.FragmentBudgetBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.MonthAndYearPickerDialog
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.IncomeViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeVisibilityOfViewOnScrolled
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
    BudgetRVAdapter.OnClickListener, MonthAndYearPickerDialog.OnMonthAndYearDialogDismissListener {

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

        initUI()
    }

    private fun getAllBudgets() {

        budgetViewModel.getAllBudgetsByMonthAndYear(
            selectedMonth, selectedYear
        )

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
                                BudgetAndIncomeExpenseFilter(categoryKeys, emptyList())
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
                    BudgetAndIncomeExpenseFilter(listOf(categoryKey), emptyList())
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

        getAllBudgets()
        showTotalExpense()
        showTotalBudget()
        showTotalIncomeAdded()
        showTotalSavings()
    }

    private fun showTotalSavings() {

        binding.iabSavingValueTV.text = (totalIncome - totalExpense).format(2)
    }

    private fun showTotalIncomeAdded() {

        incomeViewModel.getTotalIncomeAddedByMonthAndYear(
            selectedMonth, selectedYear
        ).observe(viewLifecycleOwner) { totalIncomeAdded ->

            try {
                totalIncome = totalIncomeAdded
                binding.iabIncomeValueTV.text = totalIncomeAdded.format(2)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                totalIncome = 0.0
                binding.iabIncomeValueTV.text = getString(R.string._0_0)
            }

            showTotalSavings()
        }

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
                    expenseViewModel.getTotalExpenseByCategoryKeysAndDateRange(
                        categoryKeys,
                        datePairForExpense.first,
                        datePairForExpense.second + Constants.ONE_DAY_MILLISECONDS
                    ).observe(viewLifecycleOwner) { total ->
                        try {
                            totalExpense = total
                            binding.iabExpenseValueTV.text = total.format(2)
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                            totalExpense = 0.0
                            binding.iabExpenseValueTV.text = getString(R.string._0_0)
                        }

                        showTotalSavings()
                    }
                } else {
                    binding.iabExpenseValueTV.text = getString(R.string._0_0)
                    totalExpense = 0.0
                }
            }
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
