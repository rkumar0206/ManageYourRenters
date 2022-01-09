package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
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
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

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
    private var isArgumentEmpty = false

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

        } else {

            // show all the expenses
            isArgumentEmpty = true

            binding.addExpensesFAB.hide()

            binding.toolbar.title = "All Expenses"

            expenseAdapter = ExpenseAdapter("Not specified")

            setUpRecyclerView()

            lifecycleScope.launch {

                delay(300)
                observeExpenses()
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


    private fun observeExpenses() {

        binding.expenseRV.scrollToPosition(0)

        if (!isArgumentEmpty) {

            if (this::receivedExpenseCategoryKey.isInitialized) {

                if (sortBy == SortExpense.BY_CREATED) {

                    expenseViewModel.getExpensesByExpenseCategoryKey(receivedExpenseCategoryKey)
                        .observe(
                            viewLifecycleOwner, { expenses ->

                                handleExpenseList(expenses)
                            })
                } else if (sortBy == SortExpense.BY_DATE_RANGE) {

                    // adding number of milliseconds in one day to the endDate for accurate result

                    expenseViewModel.getExpenseByDateRangeAndExpenseCategoryKey(
                        receivedExpenseCategoryKey, startDate, (endDate + 86400000L)
                    ).observe(viewLifecycleOwner, { expenses ->

                        handleExpenseList(expenses)
                    })
                }

            }
        } else {

            if (sortBy == SortExpense.BY_CREATED) {

                expenseViewModel.getAllExpenses().observe(viewLifecycleOwner) { expenses ->

                    handleExpenseList(expenses)
                }
            } else if (sortBy == SortExpense.BY_DATE_RANGE) {

                expenseViewModel.getExpensesByDateRange(
                    startDate, (endDate + 86400000L)
                ).observe(viewLifecycleOwner) { expenses ->

                    handleExpenseList(expenses)
                }
            }
        }

    }

    private fun handleExpenseList(expenses: List<Expense>) {

        if (expenses.isNotEmpty()) {

            binding.noExpenseCategoryTV.hide()
            binding.expenseRV.show()
        } else {

            binding.noExpenseCategoryTV.show()
            binding.expenseRV.hide()
        }

        Log.d(TAG, "observeExpenses: $expenses")

        expenseAdapter.submitList(expenses)

        setUpSearchMenuButton(expenses)

        binding.progressbar.hide()
    }

    private fun setUpSearchMenuButton(expenses: List<Expense>) {

        val searchView =
            binding.toolbar.menu.findItem(R.id.menu_expense_search).actionView as SearchView

        searchView.searchText { s ->

            if (s?.isEmpty()!!) {

                binding.expenseRV.scrollToPosition(0)
                expenseAdapter.submitList(expenses)
            } else {

                val filteredList = expenses.filter { expense ->

                    expense.spentOn.lowercase(Locale.ROOT)
                        .contains(s.trim().lowercase(Locale.ROOT)) || expense.amount.toString()
                        .lowercase(Locale.ROOT).contains(s.trim().lowercase(Locale.ROOT))
                }

                expenseAdapter.submitList(filteredList)
            }
        }
    }

    private fun setUpRecyclerView() {

        binding.expenseRV.apply {

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter

            if (!isArgumentEmpty) {

                changeVisibilityOfFABOnScrolled(binding.addExpensesFAB)
            }
        }

        expenseAdapter.setOnClickListener(this)

    }

    override fun onItemClick(expense: Expense) {

        expenseCategoryViewModel.getExpenseCategoryByKey(expense.categoryKey)
            .observe(viewLifecycleOwner, { expenseCategory ->

                val msg =
                    "\nDate : ${
                        WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                            expense.created, "dd-MM-yyyy hh:mm a"
                        )
                    }\n\n" +
                            "Amount : ${expense.amount}\n\n" +
                            "Spent On : ${expense.spentOn}\n\n" +
                            "Category : ${expenseCategory.categoryName}"

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Expense info")
                    .setMessage(msg)
                    .setPositiveButton("Ok") { dialog, _ ->

                        dialog.dismiss()
                    }
                    .create()
                    .show()
            })

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
                expenseForMenuItems.categoryKey, expenseForMenuItems.key
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

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addExpensesFAB.setOnClickListener {

            if (receivedExpenseCategoryKey.isValid()) {

                val action = ExpenseFragmentDirections.actionExpenseFragmentToAddEditExpense(
                    receivedExpenseCategoryKey, ""
                )

                findNavController().navigate(action)
            } else {

                showToast(requireContext(), "No category chosen!!!")
            }
        }

        binding.toolbar.menu.findItem(R.id.menu_expense_date_range).setOnMenuItemClickListener {

            handleExpenseDateRangeMenu()

            true
        }

        binding.toolbar.menu.findItem(R.id.menu_expense_clear_date_range)
            .setOnMenuItemClickListener {

                binding.toolbar.menu.findItem(R.id.menu_expense_date_range)
                    .setIcon(R.drawable.ic_baseline_date_range_24)

                sortBy = SortExpense.BY_CREATED

                binding.toolbar.subtitle = ""

                binding.progressbar.show()
                lifecycleScope.launch {
                    delay(200)
                    observeExpenses()
                }

                true
            }

        binding.toolbar.menu.findItem(R.id.menu_total_expense).setOnMenuItemClickListener {

            handleTotalExpenseMenu()
            true
        }

    }

    private fun handleTotalExpenseMenu() {

        if (!isArgumentEmpty) {

            // Expenses by category

            if (sortBy == SortExpense.BY_CREATED) {

                expenseViewModel.getTotalExpenseAmountByExpenseCategory(
                    receivedExpenseCategoryKey
                ).observe(viewLifecycleOwner) { totalAmount ->

                    showTotalAmountSumInAlertDialog(
                        receivedExpenseCategory.categoryName,
                        totalAmount
                    )
                }

            } else {

                expenseViewModel.getTotalExpenseAmountByCategoryKeyAndDateRange(
                    receivedExpenseCategoryKey,
                    startDate, endDate + 86400000L
                ).observe(viewLifecycleOwner) { totalAmount ->

                    val title = "${receivedExpenseCategory.categoryName}\nFrom ${
                        WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                            startDate
                        )
                    } to " +
                            "${
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    endDate
                                )
                            }"

                    showTotalAmountSumInAlertDialog(
                        title,
                        totalAmount
                    )
                }
            }
        } else {

            // All expenses

            if (sortBy == SortExpense.BY_CREATED) {

                expenseViewModel.getTotalExpenseAmount()
                    .observe(viewLifecycleOwner) { totalAmount ->

                        showTotalAmountSumInAlertDialog(
                            "All expenses", totalAmount
                        )

                    }
            } else if (sortBy == SortExpense.BY_DATE_RANGE) {

                expenseViewModel.getTotalExpenseAmountByDateRange(
                    startDate, (endDate + 86400000L)
                ).observe(viewLifecycleOwner) { totalAmount ->

                    val title = "From ${
                        WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                            startDate
                        )
                    } to " +
                            "${
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    endDate
                                )
                            }"
                    showTotalAmountSumInAlertDialog(
                        title, totalAmount
                    )

                }
            }
        }
    }

    private fun showTotalAmountSumInAlertDialog(title: String, totalAmount: Double?) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage("Total expense : $totalAmount")
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()

    }

    private fun handleExpenseDateRangeMenu() {

        Functions.showDateRangePickerDialog(
            startDate,
            System.currentTimeMillis(),
            {
                requireActivity().supportFragmentManager
            },
            { dates ->

                binding.toolbar.menu.findItem(R.id.menu_expense_date_range).setIcon(
                    R.drawable.ic_baseline_date_range_24_coloured
                )

                sortBy = SortExpense.BY_DATE_RANGE
                startDate = dates.first
                endDate = dates.second

                binding.toolbar.subtitle =
                    "${WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(startDate)} - " +
                            "${
                                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                    endDate
                                )
                            }"

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

