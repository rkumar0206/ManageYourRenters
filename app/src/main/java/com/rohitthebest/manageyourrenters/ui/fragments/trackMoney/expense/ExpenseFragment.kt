package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExpenseFragment : Fragment(R.layout.fragment_expense) {

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!

    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    private lateinit var receivedExpenseCategoryKey: String

    private lateinit var receivedExpenseCategory: ExpenseCategory

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
                getALlExpenses()
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
                })
        }
    }

    private fun getALlExpenses() {

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

                    //todo :  submit list to the adapter
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

            // todo : navigate to the add expense fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

