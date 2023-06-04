package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome.IncomeRVAdapter
import com.rohitthebest.manageyourrenters.database.model.Income
import com.rohitthebest.manageyourrenters.databinding.FragmentIncomeBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.MonthAndYearPickerDialog
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.IncomeViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeVisibilityOfFABOnScrolled
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import com.rohitthebest.manageyourrenters.utils.showAlertDialogForDeletion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "IncomeFragment"

@AndroidEntryPoint
class IncomeFragment : Fragment(R.layout.fragment_income), IncomeRVAdapter.OnClickListener,
    MonthAndYearPickerDialog.OnMonthAndYearDialogDismissListener {

    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!

    private val incomeViewModel by viewModels<IncomeViewModel>()
    private val budgetViewModel by viewModels<BudgetViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0
    private var monthList: List<String> = emptyList()

    private lateinit var incomeRVAdapter: IncomeRVAdapter

    private var oldestYearWhenBudgetWasSaved = 2000
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentIncomeBinding.bind(view)

        monthList = resources.getStringArray(R.array.months).toList()
        getMessage()
        initListener()
    }

    private fun setUpRecyclerView() {

        binding.incomeRV.apply {

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = incomeRVAdapter
            changeVisibilityOfFABOnScrolled(binding.addIncomeFAB)
        }

        incomeRVAdapter.setOnClickListener(this)
    }


    override fun onItemClick(income: Income) {
        // todo
    }

    override fun onIncomeItemMenuBtnClicked(income: Income, view: View) {

        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.income_item_menu, popupMenu.menu)

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {

            return@setOnMenuItemClickListener when (it.itemId) {

                R.id.menu_income_edit -> {

                    handleEditIncomeMenu(income)
                    true
                }

                R.id.menu_delete_income -> {

                    deleteIncome(income)
                    true
                }

                else -> false
            }

        }
    }

    private fun deleteIncome(income: Income) {

        showAlertDialogForDeletion(
            requireContext(),
            {
                incomeViewModel.deleteIncome(income)
                it.dismiss()
            },
            {
                it.dismiss()
            }
        )
    }

    private fun handleEditIncomeMenu(income: Income) {

        showAddEditIncomeBottomSheetFragment(true, income.key)
    }

    private fun initListener() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.previousMonthBtn.setOnClickListener {
            handlePreviousDateButton()
        }

        binding.nextMonthBtn.setOnClickListener {
            handleNextDateButton()
        }

        binding.addIncomeFAB.setOnClickListener {

            showAddEditIncomeBottomSheetFragment(false)

        }

        binding.monthMCV.setOnClickListener {
            handleMonthAndYearSelection()
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


    private fun showAddEditIncomeBottomSheetFragment(isForEdit: Boolean, incomeKey: String = "") {

        val bundle = Bundle()

        bundle.putBoolean(Constants.IS_FOR_EDIT, isForEdit)
        bundle.putInt(Constants.INCOME_MONTH_KEY, selectedMonth)
        bundle.putInt(Constants.INCOME_YEAR_KEY, selectedYear)

        if (isForEdit) {

            bundle.putString(Constants.DOCUMENT_KEY, incomeKey)
        }

        requireActivity().supportFragmentManager.let { fragmentManager ->

            AddIncomeBottomSheetFragment.newInstance(
                bundle
            ).apply {
                show(fragmentManager, TAG)
            }
        }
    }

    private fun getMessage() {

        try {

            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {
                    IncomeFragmentArgs.fromBundle(it)
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

    private fun initUI() {

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

        paymentMethodViewModel.getAllPaymentMethods()
            .observe(viewLifecycleOwner) { paymentMethods ->

                incomeRVAdapter = IncomeRVAdapter(
                    linkedPaymentMethodsMap = paymentMethods.associate { it.key to it.paymentMethod })

                setUpRecyclerView()
                getAllIncomes()
            }
    }

    private fun getAllIncomes() {

        incomeViewModel.getAllIncomesByMonthAndYear(selectedMonth, selectedYear)
            .observe(viewLifecycleOwner) { incomes ->

                if (incomes.isNotEmpty()) {

                    binding.incomeRV.show()
                    binding.noIncomeAddedTV.hide()
                } else {
                    binding.incomeRV.hide()
                    binding.noIncomeAddedTV.show()
                }

                incomeRVAdapter.submitList(incomes)
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
