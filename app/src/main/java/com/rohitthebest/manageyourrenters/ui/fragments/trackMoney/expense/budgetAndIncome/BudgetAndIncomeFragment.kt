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
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.FragmentBudgetBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeVisibilityOfViewOnScrolled
import com.rohitthebest.manageyourrenters.utils.format
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BudgetAndIncomeFragment : Fragment(R.layout.fragment_budget), View.OnClickListener,
    BudgetRVAdapter.OnClickListener {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val budgetViewModel by viewModels<BudgetViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0
    private var monthList: List<String> = emptyList()

    private lateinit var budgetAdapter: BudgetRVAdapter

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

        requireContext().showToast(budget.toString())
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
                    //todo: initialize year spinner from this year
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

        binding.iabDateNextBtn.setOnClickListener(this)
        binding.iabDateTV.setOnClickListener(this)
        binding.iabDatePreviousBtn.setOnClickListener(this)
        binding.iabIncomeAddBtn.setOnClickListener(this)
        binding.iabBudgetMCV.setOnClickListener(this)
        binding.iabSavingMCV.setOnClickListener(this)
        binding.iabExpenseMCV.setOnClickListener(this)
        binding.iabIncomeMCV.setOnClickListener(this)
        binding.iabAddBudgetFAB.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.iabDateNextBtn.id -> {

                handleNextDateButton()
            }

            binding.iabDatePreviousBtn.id -> {
                handlePreviousDateButton()
            }

            binding.iabIncomeAddBtn.id -> {

                // todo: show bottomSheet for adding income
            }

            binding.iabAddBudgetFAB.id -> {

                val action =
                    BudgetAndIncomeFragmentDirections.actionBudgetAndIncomeFragmentToAddBudgetFragment(
                        selectedMonth, selectedYear
                    )

                findNavController().navigate(action)
            }
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
        showExpenseTotal()
        showTotalBudget()
        // todo: show total income added
        // todo : show total savings done (income - expense)
    }


    private fun showExpenseTotal() {

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
                            binding.iabExpenseValueTV.text = total.format(2)
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                            binding.iabExpenseValueTV.text = getString(R.string._0_0)
                        }
                    }
                } else {
                    binding.iabExpenseValueTV.text = getString(R.string._0_0)
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

    /**
     * Work to be done
     * budget will be calculated only on monthly basis
     * once the month changes income, budget and expense should be reset
     * user can change the budget limit any time they want
     * saving the total of income and expense
     * Tables/ Models:
     * Budget:
     * categoryKey
     * budgetAmount
     * month
     * year
     * monthYearString
     * key
     *
     * Income:
     * source
     * income
     * month
     * year
     * monthYearString
     * key
     */
}
