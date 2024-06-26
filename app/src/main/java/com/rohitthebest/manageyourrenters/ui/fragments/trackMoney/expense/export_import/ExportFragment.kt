package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.export_import

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.FragmentExportBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExportFragment : Fragment(R.layout.fragment_export) {

    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private lateinit var allExpenseCategories: List<ExpenseCategory>
    private lateinit var allPaymentMethods: List<PaymentMethod>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExportBinding.bind(view)

        getExpenseCategoryListAndPaymentMethodList()
        //initListeners()
        getMessage()
    }

    private fun getMessage() {


    }

    private fun getExpenseCategoryListAndPaymentMethodList() {

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner) { expenseCategories ->
                allExpenseCategories = expenseCategories ?: emptyList()
            }

        paymentMethodViewModel.getAllPaymentMethods()
            .observe(viewLifecycleOwner) { paymentMethods ->
                allPaymentMethods = paymentMethods ?: emptyList()
            }
    }

}