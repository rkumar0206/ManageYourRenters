package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.ExpenseAdapter
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.databinding.FragmentShowExpenseBottomSheetBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowExpenseBottomSheetFragment : BottomSheetDialogFragment(), ExpenseAdapter.OnClickListener {

    private var _binding: FragmentShowExpenseBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel by viewModels<ExpenseViewModel>()

    private var dateRangeEnum: CustomDateRange = CustomDateRange.ALL_TIME
    private var date1 = 0L
    private var date2 = 0L

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

        expenseAdapter = ExpenseAdapter("", true)
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

                expense.showDetailedInfoInAlertDialog(
                    requireContext(),
                    expenseCategory.categoryName
                )
            }

    }

    override fun onMenuBtnClicked(expense: Expense, position: Int) {}

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->
                ShowExpenseBottomSheetFragmentArgs.fromBundle(bundle)
            }

            dateRangeEnum = args?.dateRangeMessage!!
            date1 = args.date1
            date2 = args.date2

            if (dateRangeEnum == CustomDateRange.ALL_TIME) {

                getAllExpenses()
            } else if (dateRangeEnum == CustomDateRange.CUSTOM_DATE_RANGE) {
                getExpensesByDateRange()
            }

        }

    }

    private fun getExpensesByDateRange() {

        expenseViewModel.getExpensesByDateRange(date1, date2)
            .observe(viewLifecycleOwner) { expenses ->
                expenseAdapter.submitList(expenses)
            }
    }

    private fun getAllExpenses() {

        expenseViewModel.getAllExpenses().observe(viewLifecycleOwner) { expenses ->

            expenseAdapter.submitList(expenses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}