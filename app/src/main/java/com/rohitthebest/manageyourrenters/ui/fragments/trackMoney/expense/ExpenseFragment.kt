package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.ExpenseAdapter
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showDateAndTimePickerDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
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
    CustomMenuItems.OnItemClickListener, ChooseExpenseCategoryBottomSheetFragment.OnItemClicked {

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
    private var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExpenseBinding.bind(view)

        sortBy = SortExpense.BY_CREATED

        binding.toolbar.subtitle = getString(R.string.all_time)

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
                .observe(viewLifecycleOwner) { expenseCategory ->

                    receivedExpenseCategory = expenseCategory

                    binding.toolbar.title = "${expenseCategory.categoryName} Expenses"

                    expenseAdapter = ExpenseAdapter(receivedExpenseCategory.categoryName)

                    setUpRecyclerView()

                    observeExpenses()
                }
        }
    }

    private fun observeExpenses() {

        binding.expenseRV.scrollToPosition(0)

        if (!isArgumentEmpty) {

            if (this::receivedExpenseCategoryKey.isInitialized) {

                if (sortBy == SortExpense.BY_CREATED) {

                    expenseViewModel.getExpensesByExpenseCategoryKey(receivedExpenseCategoryKey)
                        .observe(
                            viewLifecycleOwner
                        ) { expenses ->

                            handleExpenseList(expenses)
                        }
                } else if (sortBy == SortExpense.BY_DATE_RANGE) {

                    // adding number of milliseconds in one day to the endDate for accurate result

                    expenseViewModel.getExpenseByDateRangeAndExpenseCategoryKey(
                        receivedExpenseCategoryKey,
                        startDate,
                        (endDate + Constants.ONE_DAY_MILLISECONDS)
                    ).observe(viewLifecycleOwner) { expenses ->

                        handleExpenseList(expenses)
                    }
                }

            }
        } else {

            if (sortBy == SortExpense.BY_CREATED) {

                expenseViewModel.getAllExpenses().observe(viewLifecycleOwner) { expenses ->

                    handleExpenseList(expenses)
                }
            } else if (sortBy == SortExpense.BY_DATE_RANGE) {

                expenseViewModel.getExpensesByDateRange(
                    startDate, (endDate + Constants.ONE_DAY_MILLISECONDS)
                ).observe(viewLifecycleOwner) { expenses ->

                    handleExpenseList(expenses)
                }
            }
        }

    }

    private fun handleExpenseList(expenses: List<Expense>) {

        if (searchView != null && searchView!!.query.toString().isValid()) {

            setUpSearchMenuButton(expenses)
        } else {

            if (expenses.isNotEmpty()) {

                setNoExpenseAddedVisibility(false)
                setUpSearchMenuButton(expenses)
            } else {

                binding.noExpenseCategoryTV.text = getString(R.string.no_expense_added_message)
                setNoExpenseAddedVisibility(true)
            }
            expenseAdapter.submitList(expenses)
        }

        binding.progressbar.hide()
    }

    private var searchTextDelayJob: Job? = null
    private fun setUpSearchMenuButton(expenses: List<Expense>) {

        searchView =
            binding.toolbar.menu.findItem(R.id.menu_expense_search).actionView as SearchView

        searchView?.let { sv ->

            if (sv.query.toString().isValid()) {
                searchExpense(sv.query.toString(), expenses)
            }

            searchExpense(sv.query.toString(), expenses)

            sv.onTextSubmit { query -> searchExpense(query, expenses) }
            sv.onTextChanged { query ->

                searchTextDelayJob = lifecycleScope.launch {
                    searchTextDelayJob.executeAfterDelay {
                        searchExpense(query, expenses)
                    }
                }
            }
        }
    }

    private fun searchExpense(query: String?, expenses: List<Expense>) {

        if (query?.isEmpty()!!) {

            binding.expenseRV.scrollToPosition(0)
            expenseAdapter.submitList(expenses)

            if (expenses.isEmpty()) {

                binding.noExpenseCategoryTV.text = getString(R.string.no_expense_added_message)
                setNoExpenseAddedVisibility(true)
            } else {
                setNoExpenseAddedVisibility(false)
            }
        } else {

            val filteredList = expenses.filter { expense ->

                expense.spentOn.lowercase(Locale.ROOT)
                    .contains(query.trim().lowercase(Locale.ROOT)) || expense.amount.toString()
                    .lowercase(Locale.ROOT).contains(query.trim().lowercase(Locale.ROOT))
            }

            if (filteredList.isNotEmpty()) {
                setNoExpenseAddedVisibility(false)
            } else {
                binding.noExpenseCategoryTV.text =
                    getString(R.string.no_matching_results_found_message)
                setNoExpenseAddedVisibility(true)
            }
            expenseAdapter.submitList(filteredList)
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
            .observe(viewLifecycleOwner) { expenseCategory ->

                expense.showDetailedInfoInAlertDialog(
                    requireContext(),
                    expenseCategory.categoryName
                )
            }
    }

    private lateinit var expenseForMenuItems: Expense
    private var expenseForMenuPosition: Int = 0

    override fun onMenuBtnClicked(expense: Expense, position: Int) {

        expenseForMenuItems = expense
        expenseForMenuPosition = position

        requireActivity().supportFragmentManager.let { fm ->

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, true)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, true)
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, false)
            bundle.putBoolean(Constants.SHOW_COPY_MENU, true)
            bundle.putBoolean(Constants.SHOW_MOVE_MENU, true)
            bundle.putBoolean(Constants.SHOW_SYNC_MENU, !expense.isSynced)
            bundle.putString(Constants.COPY_MENU_TEXT, getString(R.string.duplicate_this_expense))

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

    override fun onCopyMenuClick() {

        // when copying an expense, user can change the date and time

        if (this::expenseForMenuItems.isInitialized) {
            showDateAndTimePickerDialog(
                context = requireContext(),
                selectedDate = WorkingWithDateAndTime.convertMillisecondsToCalendarInstance(System.currentTimeMillis()),
                isFutureDatesValid = false,
                pickedDateListener = { calendar ->

                    val expense = expenseForMenuItems.copy(
                        id = null,
                        created = calendar.timeInMillis,
                        modified = System.currentTimeMillis(),
                        key = generateKey("_${getUid()}"),
                        isSynced = isInternetAvailable(requireContext())
                    )

                    expenseViewModel.insertExpense(expense)
                    showToast(requireContext(), getString(R.string.expense_copied))
                    lifecycleScope.launch {
                        delay(150)
                        binding.expenseRV.scrollToPosition(0)
                    }
                }
            )
        }
    }

    override fun onMoveMenuClick() {

        if (this::expenseForMenuItems.isInitialized) {
            requireActivity().supportFragmentManager.let {

                ChooseExpenseCategoryBottomSheetFragment.newInstance(
                    Bundle()
                ).apply {

                    show(it, TAG)
                }.setOnItemClickedListener(this)
            }
        }
    }

    override fun onCategoryClicked(expenseCategory: ExpenseCategory) {

        val oldValue = expenseForMenuItems.copy()

        expenseForMenuItems.categoryKey = expenseCategory.key
        expenseForMenuItems.modified = System.currentTimeMillis()

        expenseViewModel.updateExpense(oldValue, expenseForMenuItems)
        expenseAdapter.notifyItemChanged(expenseForMenuPosition)
    }

    override fun onDeleteMenuClick() {

        if (this::expenseForMenuItems.isInitialized) {

            showAlertDialogForDeletion(
                requireContext(),
                { dialog ->

                    if (isInternetAvailable(requireContext())) {

                        expenseViewModel.deleteExpense(expenseForMenuItems)
                    } else {

                        showNoInternetMessage(requireContext())
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

                    expenseViewModel.insertExpense(expenseForMenuItems)
                    expenseAdapter.notifyItemChanged(expenseForMenuPosition)

                    // update expense category modified value
                    val oldValue = receivedExpenseCategory.copy()
                    receivedExpenseCategory.modified = System.currentTimeMillis()
                    expenseCategoryViewModel.updateExpenseCategory(
                        oldValue,
                        receivedExpenseCategory
                    )

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
                    startDate, endDate + Constants.ONE_DAY_MILLISECONDS
                ).observe(viewLifecycleOwner) { totalAmount ->

                    val title = "${receivedExpenseCategory.categoryName}\nFrom ${
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            startDate
                        )
                    } to " +
                            "${
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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
                    startDate, (endDate + Constants.ONE_DAY_MILLISECONDS)
                ).observe(viewLifecycleOwner) { totalAmount ->

                    val title = "From ${
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            startDate
                        )
                    } to " +
                            "${
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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

        Functions.showCustomDateRangeOptionMenu(
            requireActivity(),
            binding.divider105
        ) { selectedOption ->

            when (selectedOption) {

                CustomDateRange.ALL_TIME -> {

                    if (sortBy != SortExpense.BY_CREATED) {

                        sortBy = SortExpense.BY_CREATED

                        binding.toolbar.subtitle = getString(R.string.all_time)

                        updateSort()
                    }
                }

                CustomDateRange.LAST_30_DAYS -> {

                    sortBy = SortExpense.BY_DATE_RANGE
                    startDate = System.currentTimeMillis() - (30 * Constants.ONE_DAY_MILLISECONDS)
                    endDate = System.currentTimeMillis()

                    binding.toolbar.subtitle = getString(R.string.last_30_days)
                    updateSort()
                }

                CustomDateRange.LAST_7_DAYS -> {

                    sortBy = SortExpense.BY_DATE_RANGE
                    startDate = System.currentTimeMillis() - (7 * Constants.ONE_DAY_MILLISECONDS)
                    endDate = System.currentTimeMillis()

                    binding.toolbar.subtitle = getString(R.string.last_7_days)

                    updateSort()
                }

                CustomDateRange.LAST_365_DAYS -> {

                    sortBy = SortExpense.BY_DATE_RANGE
                    startDate = System.currentTimeMillis() - (365 * Constants.ONE_DAY_MILLISECONDS)
                    endDate = System.currentTimeMillis()

                    binding.toolbar.subtitle = getString(R.string.last_365_days)

                    updateSort()
                }

                CustomDateRange.CUSTOM_DATE_RANGE -> {

                    Functions.showDateRangePickerDialog(
                        startDate,
                        endDate,
                        {
                            requireActivity().supportFragmentManager
                        },
                        { date ->

                            startDate = date.first
                            endDate = date.second

                            sortBy = SortExpense.BY_DATE_RANGE

                            binding.toolbar.subtitle =
                                "${
                                    WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                        startDate
                                    )
                                } - " +
                                        "${
                                            WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                                endDate
                                            )
                                        }"

                            updateSort()
                        }
                    )

                }

                else -> {

                    sortBy = SortExpense.BY_DATE_RANGE

                    val pair = Functions.getMillisecondsOfStartAndEndUsingConstants(
                        selectedOption
                    )

                    startDate = pair.first
                    endDate = pair.second

                    when (selectedOption) {

                        CustomDateRange.THIS_MONTH -> binding.toolbar.subtitle =
                            getString(R.string.this_month)
                        CustomDateRange.THIS_WEEK -> binding.toolbar.subtitle =
                            getString(R.string.this_week)
                        CustomDateRange.PREVIOUS_MONTH -> binding.toolbar.subtitle =
                            getString(R.string.previous_month)
                        CustomDateRange.PREVIOUS_WEEK -> binding.toolbar.subtitle =
                            getString(R.string.previous_week)
                        else -> {}
                    }

                    updateSort()

                }
            }

        }
    }

    private fun updateSort() {

        binding.progressbar.show()

        lifecycleScope.launch {
            delay(200)
            observeExpenses()
        }
    }

    private fun setNoExpenseAddedVisibility(isVisible: Boolean) {


        binding.expenseRV.isVisible = !isVisible
        binding.noExpenseCategoryTV.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

