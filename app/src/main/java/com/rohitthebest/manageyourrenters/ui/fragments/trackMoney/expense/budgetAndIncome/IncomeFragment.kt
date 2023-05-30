package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.budgetAndIncome.IncomeRVAdapter
import com.rohitthebest.manageyourrenters.databinding.FragmentIncomeBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.IncomeViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.changeVisibilityOfFABOnScrolled
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IncomeFragment : Fragment(R.layout.fragment_income) {

    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!

    private val incomeViewModel by viewModels<IncomeViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0
    private var monthList: List<String> = emptyList()

    private lateinit var incomeRVAdapter: IncomeRVAdapter

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

//        budgetViewModel.getTheOldestSavedBudgetYear().observe(viewLifecycleOwner) { year ->
//            try {
//                if (year != null) {
//                    //todo: initialize year spinner from this year
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
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
