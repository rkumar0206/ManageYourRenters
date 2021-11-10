package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.ExpenseCategoryAdapter
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseCategoryBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.changeVisibilityOfFABOnScrolled
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import com.rohitthebest.manageyourrenters.utils.showAlertDialogForDeletion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ExpenseCategoryFragment"

@AndroidEntryPoint
class ExpenseCategoryFragment : Fragment(R.layout.fragment_expense_category),
    ExpenseCategoryAdapter.OnClickListener, CustomMenuItems.OnItemClickListener {

    private var _binding: FragmentExpenseCategoryBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    private lateinit var expenseCategoryAdapter: ExpenseCategoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExpenseCategoryBinding.bind(view)

        expenseCategoryAdapter = ExpenseCategoryAdapter()

        initListeners()

        binding.progressbar.show()

        lifecycleScope.launch {

            delay(300)
            observeExpenseCategories()
        }

        setUpRecyclerView()

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

    override fun onMenuBtnClicked(expenseCategory: ExpenseCategory) {

        expenseCategoryForMenus = expenseCategory

        requireActivity().supportFragmentManager.let { fm ->

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, true)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, true)
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, false)

            if (!expenseCategory.isSynced) {

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

        if (this::expenseCategoryForMenus.isInitialized) {

            val action =
                ExpenseCategoryFragmentDirections.actionExpenseCategoryFragmentToAddEditExpenseCategoryFragment(
                    expenseCategoryForMenus.key
                )

            findNavController().navigate(action)
        }
    }

    override fun onDeleteMenuClick() {

        showAlertDialogForDeletion(
            requireContext(),
            { dialog ->

                if (this::expenseCategoryForMenus.isInitialized) {

                    if (expenseCategoryForMenus.isSynced) {

                        if (isInternetAvailable(requireContext())) {
                            expenseCategoryViewModel.deleteExpenseCategory(
                                requireContext(), expenseCategoryForMenus
                            )
                        } else {
                            showNoInternetMessage(requireContext())
                        }
                    } else {

                        expenseCategoryViewModel.deleteExpenseCategory(
                            requireContext(),
                            expenseCategoryForMenus
                        )
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

                    expenseCategoryViewModel.insertExpenseCategory(
                        requireContext(),
                        expenseCategoryForMenus
                    )
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
            .observe(viewLifecycleOwner, { expenseCategories ->

                if (expenseCategories.isNotEmpty()) {

                    binding.noExpenseCategoryTV.hide()
                    binding.expenseCategoryRV.show()
                } else {

                    binding.noExpenseCategoryTV.show()
                    binding.expenseCategoryRV.hide()
                }

                binding.progressbar.hide()

                expenseCategoryAdapter.submitList(expenseCategories)
            })
    }

    private fun initListeners() {

        binding.addExpenseCategoryFAB.setOnClickListener {

            findNavController().navigate(R.id.action_expenseCategoryFragment_to_addEditExpenseCategoryFragment)
        }

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
