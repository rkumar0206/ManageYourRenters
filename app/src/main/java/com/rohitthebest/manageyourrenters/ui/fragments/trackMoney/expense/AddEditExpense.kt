package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.AddExpenseLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddExpenseBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditExpense : Fragment(R.layout.fragment_add_expense) {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddExpenseLayoutBinding

    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    private lateinit var receivedExpenseCategoryKey: String

    private lateinit var receivedExpenseCategory: ExpenseCategory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddExpenseBinding.bind(view)

        includeBinding = binding.includeLayout

        getMessage()

        initListeners()

        textWatchers()
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

                    binding.toolbar.title = "Add ${expenseCategory.categoryName} Expenses"
                })
        }
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            //todo : check form and init expense

            true
        }
    }

    private fun textWatchers() {

        includeBinding.expenseAmountET.editText?.addTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.expenseAmountET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.expenseAmountET.error = null
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
