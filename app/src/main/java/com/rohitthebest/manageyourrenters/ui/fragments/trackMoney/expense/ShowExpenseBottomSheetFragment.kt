package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.ExpenseAdapter
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.data.ShowExpenseBottomSheetTagsEnum
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.databinding.FragmentShowExpenseBottomSheetBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowExpenseBottomSheetFragment : BottomSheetDialogFragment(), ExpenseAdapter.OnClickListener {

    private var _binding: FragmentShowExpenseBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private var callingFragmentTag: ShowExpenseBottomSheetTagsEnum =
        ShowExpenseBottomSheetTagsEnum.GRAPH_FRAGMENT

    // if called from GraphFragment vars
    private var dateRangeEnum: CustomDateRange = CustomDateRange.ALL_TIME
    private var date1 = 0L
    private var date2 = 0L

    // if called from PaymentMethodFragment vars
    private var paymentMethodKey = ""

    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_expense_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentShowExpenseBottomSheetBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            this.dismiss()
        }

        expenseAdapter = ExpenseAdapter(getString(R.string.not_specified), true)
        setUpRecyclerView()
        getMessage()
    }

    private fun setUpRecyclerView() {

        binding.expensesBottomSheetRV.apply {
            setHasFixedSize(true)
            adapter = expenseAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        expenseAdapter.setOnClickListener(this)
    }

    override fun onItemClick(expense: Expense) {

        expenseCategoryViewModel.getExpenseCategoryByKey(expense.categoryKey)
            .observe(viewLifecycleOwner) { expenseCategory ->

                paymentMethodViewModel.getAllPaymentMethods()
                    .observe(viewLifecycleOwner) { paymentMethods ->
                        expense.showDetailedInfoInAlertDialog(
                            requireContext(),
                            expenseCategory.categoryName,
                            paymentMethods.associate { it.key to it.paymentMethod }
                        )
                    }
            }
    }

    override fun onMenuBtnClicked(expense: Expense, position: Int) {}

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->
                ShowExpenseBottomSheetFragmentArgs.fromBundle(bundle)
            }

            callingFragmentTag = args?.callingFragementTag!!

            when (callingFragmentTag) {

                ShowExpenseBottomSheetTagsEnum.GRAPH_FRAGMENT -> {
                    showInGraphFragment(args)
                }

                ShowExpenseBottomSheetTagsEnum.PAYMENT_METHOD_FRAGMENT -> {
                    showInPaymentMethodFragment(args)
                }
            }
        }
    }

    private fun showInPaymentMethodFragment(args: ShowExpenseBottomSheetFragmentArgs) {
        paymentMethodKey = args.paymentMethodKey ?: ""

        if (paymentMethodKey.isValid()) {
            getAllExpensesByPaymentMethod()
        } else {
            requireContext().showToast(getString(R.string.something_went_wrong))
            dismiss()
        }
    }

    private fun getAllExpensesByPaymentMethod() {

        if (paymentMethodKey == Constants.PAYMENT_METHOD_OTHER_KEY) {
            // for other payment method, get all the expenses where payment methods is null as well as payment method is other
            expenseViewModel.getExpenseByPaymentMethodsKey(listOf(paymentMethodKey))
            expenseViewModel.expensesByPaymentMethods.observe(viewLifecycleOwner) { expenses ->
                submitListToAdapter(expenses)
            }
        } else {
            expenseViewModel.getExpenseByPaymentMethodsKey(paymentMethodKey)
                .observe(viewLifecycleOwner) { expenses ->
                    submitListToAdapter(expenses)
                }
        }
    }

    private fun showInGraphFragment(args: ShowExpenseBottomSheetFragmentArgs) {
        dateRangeEnum = args.dateRangeMessage
        date1 = args.date1
        date2 = args.date2 + Constants.ONE_DAY_MILLISECONDS

        if (dateRangeEnum == CustomDateRange.ALL_TIME) {
            getAllExpenses()
        } else if (dateRangeEnum == CustomDateRange.CUSTOM_DATE_RANGE) {
            getExpensesByDateRange()
        }
    }

    private fun getExpensesByDateRange() {

        expenseViewModel.getExpensesByDateRange(date1, date2)
            .observe(viewLifecycleOwner) { expenses ->
                submitListToAdapter(expenses)
            }
    }

    private fun getAllExpenses() {
        expenseViewModel.getAllExpenses().observe(viewLifecycleOwner) { expenses ->
            submitListToAdapter(expenses)
        }
    }

    private fun submitListToAdapter(expenses: List<Expense>) {
        binding.noExpenseAdded.isVisible = expenses.isEmpty()
        expenseAdapter.submitList(expenses)
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}