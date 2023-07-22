package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.AddExpenseLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddExpenseBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddExpenseBottomSheetFragment : BottomSheetDialogFragment(R.layout.fragment_add_expense) {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddExpenseLayoutBinding

    private val expenseViewModel by viewModels<ExpenseViewModel>()
    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private var mListener: OnAddExpenseBottomSheetDismissListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddExpenseBinding.bind(view)


    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): AddExpenseBottomSheetFragment {
            val fragment = AddExpenseBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    interface OnAddExpenseBottomSheetDismissListener {

    }

    fun setOnBottomSheetDismissListener(listener: OnAddExpenseBottomSheetDismissListener) {

        mListener = listener
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
