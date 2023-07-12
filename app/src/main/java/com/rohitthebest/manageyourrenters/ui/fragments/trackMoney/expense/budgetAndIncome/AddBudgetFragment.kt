package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome.SetBudgetExpenseCategoryAdapter
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.data.ShowExpenseBottomSheetTagsEnum
import com.rohitthebest.manageyourrenters.data.filter.BudgetAndIncomeExpenseFilter
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.FragmentAddBudgetBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.MonthAndYearPickerDialog
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.convertStringListToJSON
import com.rohitthebest.manageyourrenters.utils.convertToJsonString
import com.rohitthebest.manageyourrenters.utils.executeAfterDelay
import com.rohitthebest.manageyourrenters.utils.format
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.onTextChanged
import com.rohitthebest.manageyourrenters.utils.onTextSubmit
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "AddBudgetFragment"

@AndroidEntryPoint
class AddBudgetFragment : Fragment(R.layout.fragment_add_budget),
    SetBudgetExpenseCategoryAdapter.OnClickListener,
    AddBudgetLimitBottomSheetFragment.OnBottomSheetDismissListener,
    MonthAndYearPickerDialog.OnMonthAndYearDialogDismissListener,
    ChooseMonthAndYearBottomSheetFragment.OnBottomSheetDismissListener {

    private var _binding: FragmentAddBudgetBinding? = null
    private val binding get() = _binding!!

    private val budgetViewModel by viewModels<BudgetViewModel>()

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0
    private var monthList: List<String> = emptyList()

    private lateinit var setBudgetExpenseCategoryAdapter: SetBudgetExpenseCategoryAdapter
    private var adapterPosition = 0
    private var searchView: SearchView? = null

    private var oldestYearWhenBudgetWasSaved = 2000
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBudgetBinding.bind(view)

        monthList = resources.getStringArray(R.array.months).toList()
        setBudgetExpenseCategoryAdapter = SetBudgetExpenseCategoryAdapter()

        binding.progressBar.show()

        getMessage()
        initListeners()
        setUpRecyclerView()
        observerBudgetList()
    }

    private fun observerBudgetList() {

        budgetViewModel.allExpenseCategoryAsBudgets.observe(viewLifecycleOwner) { budgets ->
            setUpSearchViewMenu(budgets)
            getTotalBudget()
            binding.progressBar.hide()
        }
    }

    private fun setUpRecyclerView() {

        binding.setBudgetRV.apply {

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = setBudgetExpenseCategoryAdapter
        }
        setBudgetExpenseCategoryAdapter.setOnClickListener(this)

    }

    override fun onItemClicked(expenseCategoryKey: String, isBudgetLimitAdded: Boolean) {

        if (!isBudgetLimitAdded) {
            showToast(requireContext(), getString(R.string.please_add_limit_for_this_category))
            return
        }

        val datePairForExpense =
            WorkingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonthForGivenMonthAndYear(
                selectedMonth, selectedYear
            )

        val action =
            AddBudgetFragmentDirections.actionAddBudgetFragmentToShowExpenseBottomSheetFragment(
                null,
                CustomDateRange.CUSTOM_DATE_RANGE,
                datePairForExpense.first,
                datePairForExpense.second,
                ShowExpenseBottomSheetTagsEnum.BUDGET_AND_INCOME_FRAGMENT,
                BudgetAndIncomeExpenseFilter(listOf(expenseCategoryKey), emptyList())
            )

        findNavController().navigate(action)
    }

    override fun onAddBudgetClicked(budget: Budget, position: Int) {

        adapterPosition = position
        val bundle = Bundle()
        bundle.putBoolean(Constants.IS_FOR_EDIT, false)
        bundle.putString(Constants.BUDGET, budget.convertToJsonString())

        requireActivity().supportFragmentManager.let { fm ->

            AddBudgetLimitBottomSheetFragment.newInstance(bundle)
                .apply {
                    show(fm, TAG)
                }.setOnBottomSheetDismissListener(this)
        }
    }

    override fun onBottomSheetDismissed(isBudgetLimitAdded: Boolean) {

        if (isBudgetLimitAdded) {

            getAllExpenseCategoriesAsBudget()

            setBudgetExpenseCategoryAdapter.notifyItemChanged(adapterPosition)
        }
    }


    override fun onBudgetMenuBtnClicked(budget: Budget, view: View, position: Int) {

        adapterPosition = position

        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_add_budget_item, popupMenu.menu)

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {

            return@setOnMenuItemClickListener when (it.itemId) {

                R.id.menu_ab_edit -> {

                    handleEditBudgetMenu(budget)
                    true
                }

                R.id.menu_ab_remove_limit -> {

                    handleRemoveBudgetLimitMenu(budget)
                    true
                }

                else -> false
            }

        }
    }

    private fun handleRemoveBudgetLimitMenu(budget: Budget) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.are_you_sure))
            .setMessage(getString(R.string.remove_budget_warning))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                if (requireContext().isInternetAvailable()) {
                    budgetViewModel.deleteBudget(budget)
                    getAllExpenseCategoriesAsBudget()
                    setBudgetExpenseCategoryAdapter.notifyItemChanged(adapterPosition)
                } else {
                    showNoInternetMessage(requireContext())
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

    }

    private fun handleEditBudgetMenu(budget: Budget) {

        val bundle = Bundle()
        bundle.putBoolean(Constants.IS_FOR_EDIT, true)
        bundle.putString(Constants.DOCUMENT_KEY, budget.key)

        requireActivity().supportFragmentManager.let { fm ->

            AddBudgetLimitBottomSheetFragment.newInstance(bundle)
                .apply {
                    show(fm, TAG)
                }.setOnBottomSheetDismissListener(this)
        }
    }

    private fun getMessage() {

        try {

            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {
                    AddBudgetFragmentArgs.fromBundle(it)
                }

                selectedMonth = args?.monthMessage ?: WorkingWithDateAndTime.getCurrentMonth()
                selectedYear = args?.yearMessage ?: WorkingWithDateAndTime.getCurrentYear()
            } else {
                selectedMonth = WorkingWithDateAndTime.getCurrentMonth()
                selectedYear = WorkingWithDateAndTime.getCurrentYear()
            }

            lifecycleScope.launch {
                delay(300)
                initUI()
            }
        } catch (e: Exception) {
            e.printStackTrace()

            selectedMonth = WorkingWithDateAndTime.getCurrentMonth()
            selectedYear = WorkingWithDateAndTime.getCurrentYear()
            lifecycleScope.launch {
                delay(300)
                initUI()
            }
        }

    }

    private fun getAllExpenseCategoriesAsBudget() {

        budgetViewModel.getAllExpenseCategoryAsBudget(selectedMonth, selectedYear)
    }

    private var searchTextDelayJob: Job? = null
    private fun setUpSearchViewMenu(budgets: List<Budget>) {

        searchView =
            binding.toolbar.menu.findItem(R.id.menu_search_budget_list).actionView as SearchView

        searchView?.let { sv ->

            searchBudget(sv.query.toString(), budgets)

            sv.onTextSubmit { query -> searchBudget(query ?: "", budgets) }
            sv.onTextChanged { query ->

                searchTextDelayJob = lifecycleScope.launch {
                    searchTextDelayJob.executeAfterDelay {
                        searchBudget(query ?: "", budgets)
                    }
                }
            }
        }
    }

    private fun searchBudget(query: String, budgets: List<Budget>) {

        if (!query.isValid()) {
            setBudgetExpenseCategoryAdapter.submitList(budgets)

            showNoBudgetAddedTV(
                budgets.isEmpty(),
                getString(R.string.no_expense_category_added_for_budget_message)
            )

        } else {

            val filteredList = budgets.filter { budget ->

                budget.categoryName.lowercase()
                    .contains(query.trim().lowercase())
            }

            showNoBudgetAddedTV(
                filteredList.isEmpty(),
                getString(R.string.no_matching_results_found_message)
            )

            setBudgetExpenseCategoryAdapter.submitList(filteredList)
        }
    }

    private fun showNoBudgetAddedTV(
        isVisible: Boolean,
        noExpenseCategoryAddedForBudgetMessage: String = ""
    ) {

        binding.noBudgetLimitAddedTV.text = noExpenseCategoryAddedForBudgetMessage
        binding.noBudgetLimitAddedTV.isVisible = isVisible
        binding.setBudgetRV.isVisible = !isVisible
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.nextMonthBtn.setOnClickListener {
            handleNextDateButton()
        }
        binding.previousMonthBtn.setOnClickListener {
            handlePreviousDateButton()
        }
        binding.monthMCV.setOnClickListener {
            handleMonthAndYearSelection()
        }
        binding.toolbar.menu.findItem(R.id.copy_previous_months_budget).setOnMenuItemClickListener {

            handleCopyPreviousMonthBudgetMenu()
            true
        }
    }

    private var isBudgetAddedForSelectedMonth = false
    private fun handleCopyPreviousMonthBudgetMenu() {

        var isRefreshEnabled = true

        budgetViewModel.getAllBudgetMonthAndYearForWhichBudgetIsAdded()
            .observe(viewLifecycleOwner) { monthYearStringList ->

                if (isRefreshEnabled) {

                    Log.d(TAG, "initListeners: monthYearString: $monthYearStringList")

                    val monthYearStringForSelectedMonthAndYear =
                        WorkingWithDateAndTime.getMonthAndYearString(
                            selectedMonth,
                            selectedYear
                        )

                    val monthYearStringListAfterRemovingSelectedMonth =
                        ArrayList(monthYearStringList)

                    monthYearStringListAfterRemovingSelectedMonth.remove(
                        monthYearStringForSelectedMonthAndYear
                    )

                    if (monthYearStringListAfterRemovingSelectedMonth.isNotEmpty()) {

                        budgetViewModel.isAnyBudgetAddedForThisMonthAndYear(
                            monthYearStringForSelectedMonthAndYear
                        ).observe(viewLifecycleOwner) { budgetKeys ->

                            if (isRefreshEnabled) {
                                if (budgetKeys.isNotEmpty()) {
                                    MaterialAlertDialogBuilder(requireContext())
                                        .setTitle(getString(R.string.are_you_sure))
                                        .setMessage(getString(R.string.replace_the_current_budget_limit_for_this_month))
                                        .setPositiveButton(getString(R.string.ok)) { dialog, _ ->

                                            isBudgetAddedForSelectedMonth = true
                                            showPreviousMonthAndYearListForWhichBudgetIsAdded(
                                                monthYearStringListAfterRemovingSelectedMonth
                                            )
                                            dialog.dismiss()
                                        }
                                        .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        .create()
                                        .show()

                                } else {

                                    isBudgetAddedForSelectedMonth = false
                                    showPreviousMonthAndYearListForWhichBudgetIsAdded(
                                        monthYearStringList
                                    )
                                }
                            }
                            isRefreshEnabled = false
                        }

                    } else {

                        showToast(
                            requireContext(),
                            getString(R.string.no_budget_limit_added_yet_for_any_month)
                        )
                    }

                }
            }

    }

    private fun showPreviousMonthAndYearListForWhichBudgetIsAdded(monthYearStringList: List<String>?) {

        Log.d(TAG, "showPreviousMonthAndYearListForWhichBudgetIsAdded: monthYearString")

        val bundle = Bundle()
        bundle.putString(
            Constants.COPY_BUDGET_MONTH_AND_YEAR_KEY,
            convertStringListToJSON(monthYearStringList ?: listOf(""))
        )

        requireActivity().supportFragmentManager.let { fm ->

            ChooseMonthAndYearBottomSheetFragment.newInstance(bundle)
                .apply {
                    show(fm, TAG)
                }.setOnBottomSheetDismissListener(this)
        }
    }

    override fun onMonthAndYearSelectedForCopyingBudget(
        isMonthAndYearSelected: Boolean,
        selectedMonthYearString: String     // this is the month from which user want to copy/duplicate the budget
    ) {

        budgetViewModel.duplicateBudgetOfPreviouslyAddedBudgetMonth(
            isBudgetAddedForSelectedMonth,
            Pair(selectedMonth, selectedYear),
            selectedMonthYearString
        )

        lifecycleScope.launch {
            binding.progressBar.show()

            delay(200)

            getAllExpenseCategoriesAsBudget()

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


    private fun initUI() {

        binding.monthAndYearTV.text =
            getString(R.string.month_and_year, monthList[selectedMonth], selectedYear.toString())

        handleUiAfterDateChange()

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

        binding.monthAndYearTV.text =
            getString(R.string.month_and_year, monthList[selectedMonth], selectedYear.toString())

        getAllExpenseCategoriesAsBudget()
        try {
            binding.setBudgetRV.scrollToPosition(0)
        } catch (_: Exception) {
        }

        getTotalBudget()
    }

    private fun getTotalBudget() {

        budgetViewModel.getTotalBudgetByMonthAndYear(selectedMonth, selectedYear)
            .observe(viewLifecycleOwner) { totalBudget ->
                try {
                    binding.toolbar.subtitle =
                        getString(R.string.total_with_arg, totalBudget.format(2))
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    binding.toolbar.subtitle =
                        getString(R.string.total_with_arg, getString(R.string._0_0))
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
