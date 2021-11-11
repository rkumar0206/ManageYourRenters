package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.ExpenseAdapter
import com.rohitthebest.manageyourrenters.database.model.apiModels.Expense
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import com.rohitthebest.manageyourrenters.utils.showAlertDialogForDeletion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ExpenseFragment"

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExpenseBinding.bind(view)


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
        }

        expenseAdapter.setOnClickListener(this)

    }

    override fun onItemClick(expense: Expense) {

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

            expenseViewModel.getExpensesByExpenseCategoryKey(receivedExpenseCategoryKey).observe(
                viewLifecycleOwner, { expenses ->

                    if (expenses.isNotEmpty()) {

                        binding.noExpenseCategoryTV.hide()
                        binding.expenseRV.show()
                    } else {

                        binding.noExpenseCategoryTV.show()
                        binding.expenseRV.hide()
                    }

                    Log.d(TAG, "observeExpenses: $expenses")

                    expenseAdapter.submitList(expenses)
                }
            )
        }

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

