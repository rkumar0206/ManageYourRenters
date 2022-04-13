package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEditMonthlyPaymentCategoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditMonthlyPaymentCategory :
    Fragment(R.layout.fragment_add_edit_monthly_payment_category) {

    private var _binding: FragmentAddEditMonthlyPaymentCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditMonthlyPaymentCategoryBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


