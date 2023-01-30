package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.ExpenseCategoryAdapter
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseCategoryBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "ExpenseCategoryFragment"

@AndroidEntryPoint
class ExpenseCategoryFragment : Fragment(R.layout.fragment_expense_category),
    ExpenseCategoryAdapter.OnClickListener, CustomMenuItems.OnItemClickListener {

    private var _binding: FragmentExpenseCategoryBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    private lateinit var expenseCategoryAdapter: ExpenseCategoryAdapter

    private var rvStateParcelable: Parcelable? = null
    private var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExpenseCategoryBinding.bind(view)

        expenseCategoryAdapter = ExpenseCategoryAdapter()

        initListeners()

        binding.progressbar.show()

        getExpenseCategoryRvState()

        lifecycleScope.launch {

            delay(300)
            observeExpenseCategories()
        }

        setUpRecyclerView()

    }

    private fun getExpenseCategoryRvState() {

        expenseCategoryViewModel.expenseCategoryRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }
    }

    private fun setUpRecyclerView() {

        binding.expenseCategoryRV.apply {

            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = expenseCategoryAdapter
            changeVisibilityOfFABOnScrolled(binding.addExpenseCategoryFAB)
        }

        expenseCategoryAdapter.setOnClickListener(this)
    }

    override fun onItemClick(expenseCategory: ExpenseCategory) {

        val action =
            ExpenseCategoryFragmentDirections.actionExpenseCategoryFragmentToExpenseFragment(
                expenseCategory.key
            )

        findNavController().navigate(action)
    }

    private lateinit var expenseCategoryForMenus: ExpenseCategory
    private var adapterPosition = -1

    override fun onMenuBtnClicked(expenseCategory: ExpenseCategory, position: Int) {

        expenseCategoryForMenus = expenseCategory
        adapterPosition = position

        requireActivity().supportFragmentManager.let { fm ->

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, true)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, true)
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, false)
            bundle.putBoolean(Constants.SHOW_SYNC_MENU, !expenseCategory.isSynced)

            CustomMenuItems.newInstance(
                bundle
            ).apply {

                show(fm, TAG)
            }.setOnClickListener(this)
        }

    }

    override fun onEditMenuClick() {

        if (this::expenseCategoryForMenus.isInitialized) {

            val action =
                ExpenseCategoryFragmentDirections.actionExpenseCategoryFragmentToAddEditExpenseCategoryFragment(
                    expenseCategoryForMenus.key
                )

            findNavController().navigate(action)
        }
    }

    override fun onCopyMenuClick() {}

    override fun onMoveMenuClick() {}

    override fun onDeleteMenuClick() {

        showAlertDialogForDeletion(
            requireContext(),
            { dialog ->

                if (this::expenseCategoryForMenus.isInitialized) {

                    // internet is always required to delete any object,
                    // even if it is not synced to the cloud, as it may have been inserted to the
                    // cloud database and been updated later and is not synced now
                    if (isInternetAvailable(requireContext())) {

                        expenseCategoryViewModel.deleteExpenseCategory(expenseCategoryForMenus)
                    } else {
                        showNoInternetMessage(requireContext())
                    }
                }

                dialog.dismiss()
            },
            { dialog ->

                dialog.dismiss()
            }
        )
    }

    override fun onSyncMenuClick() {

        if (this::expenseCategoryForMenus.isInitialized) {

            if (!expenseCategoryForMenus.isSynced) {

                if (isInternetAvailable(requireContext())) {

                    expenseCategoryViewModel.insertExpenseCategory(expenseCategoryForMenus)
                    expenseCategoryAdapter.notifyItemChanged(adapterPosition)
                } else {

                    showNoInternetMessage(requireContext())
                }

            }
        }
    }

    override fun onViewSupportingDocumentMenuClick() {}

    override fun onReplaceSupportingDocumentClick() {}

    override fun onDeleteSupportingDocumentClick() {}

    private fun observeExpenseCategories() {

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner) { expenseCategories ->

                if (searchView != null && searchView!!.query.toString().isValid()) {

                    setUpSearchView(expenseCategories)
                } else {

                    if (expenseCategories.isNotEmpty()) {

                        setNoExpenseCategoryMessageTvVisibility(false)
                        setUpSearchView(expenseCategories)
                    } else {
                        binding.noExpenseCategoryTV.text =
                            getString(R.string.no_expense_category_added_message)
                        setNoExpenseCategoryMessageTvVisibility(true)
                    }

                    expenseCategoryAdapter.submitList(expenseCategories)
                }
                binding.expenseCategoryRV.layoutManager?.onRestoreInstanceState(rvStateParcelable)
                binding.progressbar.hide()
            }
    }
    private var searchTextDelayJob: Job? = null
    private fun setUpSearchView(expenseCategories: List<ExpenseCategory>) {

        searchView =
            binding.toolbar.menu.findItem(R.id.menu_search_expense_category).actionView as SearchView

        searchView?.let { sv ->

            if (sv.query.toString().isValid()) {
                searchExpenseCategory(sv.query.toString(), expenseCategories)
            }
            sv.onTextSubmit { query -> searchExpenseCategory(query, expenseCategories) }
            sv.onTextChanged { query ->

                searchTextDelayJob = lifecycleScope.launch {
                    searchTextDelayJob?.executeAfterDelay {
                        searchExpenseCategory(query, expenseCategories)
                    }
                }
            }
        }
    }

    private fun searchExpenseCategory(query: String?, expenseCategories: List<ExpenseCategory>) {

        if (query?.isEmpty()!!) {

            binding.expenseCategoryRV.scrollToPosition(0)
            expenseCategoryAdapter.submitList(expenseCategories)
            if (expenseCategories.isNotEmpty()) {
                setNoExpenseCategoryMessageTvVisibility(false)
            } else {
                binding.noExpenseCategoryTV.text =
                    getString(R.string.no_expense_category_added_message)
                setNoExpenseCategoryMessageTvVisibility(true)
            }
        } else {

            val filteredList = expenseCategories.filter { expenseCategory ->

                expenseCategory.categoryName.lowercase(Locale.ROOT).contains(
                    query.toString().trim().lowercase(Locale.ROOT)
                )
            }
            if (filteredList.isNotEmpty()) {
                setNoExpenseCategoryMessageTvVisibility(false)
            } else {
                binding.noExpenseCategoryTV.text =
                    getString(R.string.no_matching_results_found_message)
                setNoExpenseCategoryMessageTvVisibility(true)
            }

            expenseCategoryAdapter.submitList(filteredList)

        }
    }

    private fun initListeners() {

        binding.addExpenseCategoryFAB.setOnClickListener {

            findNavController().navigate(R.id.action_expenseCategoryFragment_to_addEditExpenseCategoryFragment)
        }

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.toolbar.menu.findItem(R.id.menu_expense_graph).setOnMenuItemClickListener {

            findNavController().navigate(R.id.action_expenseCategoryFragment_to_graphFragment)

            //startActivity(Intent(requireContext(), GraphActivity::class.java))

            true
        }

        binding.toolbar.menu.findItem(R.id.menu_show_all_expenses).setOnMenuItemClickListener {

            findNavController().navigate(R.id.action_expenseCategoryFragment_to_expenseFragment)
            true
        }

        binding.toolbar.menu.findItem(R.id.menu_item_payment_methods_expense)
            .setOnMenuItemClickListener {

                findNavController().navigate(R.id.action_expenseCategoryFragment_to_paymentMethodsFragment)
                true
            }
    }

    private fun setNoExpenseCategoryMessageTvVisibility(isVisible: Boolean) {

        binding.expenseCategoryRV.isVisible = !isVisible
        binding.noExpenseCategoryTV.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()

        expenseCategoryViewModel.saveExpenseCategoryRvState(
            binding.expenseCategoryRV.layoutManager?.onSaveInstanceState()
        )

        hideKeyBoard(requireActivity())

        _binding = null
    }

}
