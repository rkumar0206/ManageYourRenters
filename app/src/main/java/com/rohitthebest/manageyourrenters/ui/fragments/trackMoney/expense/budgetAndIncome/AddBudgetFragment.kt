package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome.SetBudgetExpenseCategoryAdapter
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.FragmentAddBudgetBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddBudgetFragment : Fragment(R.layout.fragment_add_budget),
    SetBudgetExpenseCategoryAdapter.OnClickListener {

    private var _binding: FragmentAddBudgetBinding? = null
    private val binding get() = _binding!!

    private val budgetViewModel by viewModels<BudgetViewModel>()

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0
    private var monthList: List<String> = emptyList()

    private lateinit var setBudgetExpenseCategoryAdapter: SetBudgetExpenseCategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBudgetBinding.bind(view)

        monthList = resources.getStringArray(R.array.months).toList()
        setBudgetExpenseCategoryAdapter = SetBudgetExpenseCategoryAdapter()

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

    override fun onAddBudgetClicked(budget: Budget) {
        //todo: show a dialog or something to add a limit to a budget

        showToast(requireContext(), "$budget")
        if (budget.budgetLimit == 0.0) {
            budget.apply {
                this.budgetLimit = 500.0
                this.uid = getUid()!!
                this.isSynced = false
                this.month = selectedMonth
                this.year = selectedYear
                this.monthYearString = this.generateMonthYearString()
                this.created = System.currentTimeMillis()
                this.modified = System.currentTimeMillis()
                this.key = generateKey(appendString = "_${this.uid}")
            }
            budgetViewModel.insertBudget(budget)
            getAllExpenseCategoriesAsBudget()
            setBudgetExpenseCategoryAdapter.notifyDataSetChanged()
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

            // todo: if no expense category is added yet show a textview for adding an expense category first
            setBudgetExpenseCategoryAdapter.submitList(budgets)
        }

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
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
