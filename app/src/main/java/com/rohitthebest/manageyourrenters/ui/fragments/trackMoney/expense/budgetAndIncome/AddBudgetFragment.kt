package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome.SetBudgetExpenseCategoryAdapter
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.FragmentAddBudgetBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.convertToJsonString
import com.rohitthebest.manageyourrenters.utils.executeAfterDelay
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
    AddBudgetLimitBottomSheetFragment.OnBottomSheetDismissListener {

    private var _binding: FragmentAddBudgetBinding? = null
    private val binding get() = _binding!!

    private val budgetViewModel by viewModels<BudgetViewModel>()

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0
    private var monthList: List<String> = emptyList()

    private lateinit var setBudgetExpenseCategoryAdapter: SetBudgetExpenseCategoryAdapter
    private var adapterPosition = 0
    private var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBudgetBinding.bind(view)

        monthList = resources.getStringArray(R.array.months).toList()
        setBudgetExpenseCategoryAdapter = SetBudgetExpenseCategoryAdapter()

        binding.progressBar.show()

        getMessage()
        initListeners()
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        binding.setBudgetRV.apply {

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = setBudgetExpenseCategoryAdapter
        }
        setBudgetExpenseCategoryAdapter.setOnClickListener(this)

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
        popupMenu.menuInflater.inflate(R.menu.menu_add_budget, popupMenu.menu)

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
        budgetViewModel.allExpenseCategoryAsBudgets.observe(viewLifecycleOwner) { budgets ->
            setUpSearchViewMenu(budgets)
            binding.progressBar.hide()
        }
    }

    private var searchTextDelayJob: Job? = null
    private fun setUpSearchViewMenu(budgets: List<Budget>) {

        searchView =
            binding.toolbar.menu.findItem(R.id.menu_search).actionView as SearchView

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

        binding.setBudgetNextDateBtn.setOnClickListener {
            handleNextDateButton()
        }
        binding.setBudgetPreviousDateBtn.setOnClickListener {
            handlePreviousDateButton()
        }
    }

    private fun initUI() {

        binding.setBudgetDateTV.text =
            getString(R.string.month_and_year, monthList[selectedMonth], selectedYear.toString())

        handleUiAfterDateChange()

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

        binding.setBudgetDateTV.text =
            getString(R.string.month_and_year, monthList[selectedMonth], selectedYear.toString())

        getAllExpenseCategoriesAsBudget()
        try {
            binding.setBudgetRV.scrollToPosition(0)
        } catch (_: Exception) {
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
