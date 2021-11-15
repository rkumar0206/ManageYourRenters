package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.ExpenseAdapter
import com.rohitthebest.manageyourrenters.database.model.apiModels.Expense
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ExpenseFragment"

enum class SortExpense {

    BY_DATE_RANGE,
    BY_CREATED
}

@AndroidEntryPoint
class ExpenseFragment : Fragment(R.layout.fragment_expense), ExpenseAdapter.OnClickListener,
    CustomMenuItems.OnItemClickListener {

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    private lateinit var receivedExpenseCategoryKey: String

    private lateinit var receivedExpenseCategory: ExpenseCategory

    private lateinit var expenseAdapter: ExpenseAdapter

    private var startDate = 0L
    private var endDate = 0L

    private var sortBy: SortExpense = SortExpense.BY_CREATED

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExpenseBinding.bind(view)

        sortBy = SortExpense.BY_CREATED
        startDate = System.currentTimeMillis()
        endDate = System.currentTimeMillis()

        binding.progressbar.show()

        getMessage()

        initListeners()
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->

                ExpenseFragmentArgs.fromBundle(bundle)
            }

            receivedExpenseCategoryKey = args?.expenseCategoryKey!!

            lifecycleScope.launch {

                delay(300)
                getExpenseCategory()
            }

        }

    }

    private fun getExpenseCategory() {

        if (this::receivedExpenseCategoryKey.isInitialized) {

            expenseCategoryViewModel.getExpenseCategoryByKey(receivedExpenseCategoryKey)
                .observe(viewLifecycleOwner, { expenseCategory ->

                    receivedExpenseCategory = expenseCategory

                    binding.toolbar.title = "${expenseCategory.categoryName} Expenses"

                    expenseAdapter = ExpenseAdapter(receivedExpenseCategory.categoryName)

                    setUpRecyclerView()

                    observeExpenses()
                })
        }
    }

    private fun setUpRecyclerView() {

        binding.expenseRV.apply {

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
            changeVisibilityOfFABOnScrolled(binding.addExpensesFAB)
        }

        expenseAdapter.setOnClickListener(this)

    }

    override fun onItemClick(expense: Expense) {

        val msg =
            "\nDate : ${WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(expense.created)}\n\n" +
                    "Amount : ${expense.amount}\n\n" +
                    "Spent On : ${expense.spentOn}"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Expense info")
            .setMessage(msg)
            .setPositiveButton("Ok") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()

    }

    private lateinit var expenseForMenuItems: Expense

    override fun onMenuBtnClicked(expense: Expense) {

        expenseForMenuItems = expense

        requireActivity().supportFragmentManager.let { fm ->

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, true)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, true)
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, false)

            if (!expense.isSynced) {

                bundle.putBoolean(Constants.SHOW_SYNC_MENU, true)
            }

            CustomMenuItems.newInstance(
                bundle
            ).apply {

                show(fm, TAG)
            }.setOnClickListener(this)

        }
    }

    override fun onEditMenuClick() {

        if (this::expenseForMenuItems.isInitialized) {

            val action = ExpenseFragmentDirections.actionExpenseFragmentToAddEditExpense(
                receivedExpenseCategoryKey, expenseForMenuItems.key
            )

            findNavController().navigate(action)
        }

    }

    override fun onDeleteMenuClick() {

        if (this::expenseForMenuItems.isInitialized) {

            showAlertDialogForDeletion(
                requireContext(),
                { dialog ->

                    if (expenseForMenuItems.isSynced) {

                        if (isInternetAvailable(requireContext())) {

                            expenseViewModel.deleteExpense(requireContext(), expenseForMenuItems)
                        } else {

                            showNoInternetMessage(requireContext())
                        }
                    } else {

                        expenseViewModel.deleteExpense(requireContext(), expenseForMenuItems)
                    }

                    dialog.dismiss()
                },
                { dialog ->

                    dialog.dismiss()
                }
            )
        }
    }

    override fun onViewSupportingDocumentMenuClick() {}

    override fun onReplaceSupportingDocumentClick() {}

    override fun onDeleteSupportingDocumentClick() {}

    override fun onSyncMenuClick() {

        if (this::expenseForMenuItems.isInitialized) {

            if (!expenseForMenuItems.isSynced) {

                if (isInternetAvailable(requireContext())) {

                    expenseViewModel.insertExpense(requireContext(), expenseForMenuItems)
                } else {

                    showNoInternetMessage(requireContext())
                }
            }
        }

    }

    private fun observeExpenses() {

        if (this::receivedExpenseCategoryKey.isInitialized) {

            if (sortBy == SortExpense.BY_CREATED) {

                expenseViewModel.getExpensesByExpenseCategoryKey(receivedExpenseCategoryKey)
                    .observe(
                        viewLifecycleOwner, { expenses ->

                            showExpenseItems(expenses)
                        })
            } else if (sortBy == SortExpense.BY_DATE_RANGE) {

                // and number of milliseconds in one day to the endDate for accurate result

                expenseViewModel.getExpenseByDateRangeAndExpenseCategoryKey(
                    receivedExpenseCategoryKey, startDate, (endDate + 86400000L)
                ).observe(viewLifecycleOwner, { expenses ->

                    showExpenseItems(expenses)
                })
            }

        }
    }

    private fun showExpenseItems(expenses: List<Expense>) {

        if (expenses.isNotEmpty()) {

            binding.noExpenseCategoryTV.hide()
            binding.expenseRV.show()
        } else {

            binding.noExpenseCategoryTV.show()
            binding.expenseRV.hide()
        }

        Log.d(TAG, "observeExpenses: $expenses")

        expenseAdapter.submitList(expenses)

        binding.progressbar.hide()
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addExpensesFAB.setOnClickListener {

            val action = ExpenseFragmentDirections.actionExpenseFragmentToAddEditExpense(
                receivedExpenseCategoryKey, ""
            )

            findNavController().navigate(action)
        }

        binding.toolbar.menu.findItem(R.id.menu_expense_date_range).setOnMenuItemClickListener {

            handleMenuExpenseDateRange()

            true
        }

        binding.toolbar.menu.findItem(R.id.menu_show_all_expenses).setOnMenuItemClickListener {

            sortBy = SortExpense.BY_CREATED

            binding.progressbar.show()
            lifecycleScope.launch {
                delay(200)
                observeExpenses()
            }

            true
        }

    }

    private fun handleMenuExpenseDateRange() {

        Functions.showDateRangePickerDialog(
            startDate,
            System.currentTimeMillis(),
            {
                requireActivity().supportFragmentManager
            },
            { dates ->

                sortBy = SortExpense.BY_DATE_RANGE
                startDate = dates.first
                endDate = dates.second

                binding.progressbar.show()
                lifecycleScope.launch {
                    delay(200)
                    observeExpenses()
                }
            }
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

