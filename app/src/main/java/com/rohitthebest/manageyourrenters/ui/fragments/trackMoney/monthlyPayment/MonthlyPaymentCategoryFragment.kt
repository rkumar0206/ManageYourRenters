package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentMonthlyPaymentCategoryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MonthlyPaymentCategoryFragment : Fragment(R.layout.fragment_monthly_payment_category) {

    private var _binding: FragmentMonthlyPaymentCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyPaymentCategoryBinding.bind(view)

        initListeners()
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.addMonthlyPaymentCategoryFAB.setOnClickListener {

            findNavController().navigate(R.id.action_monthlyPaymentCategoryFragment_to_addEditMonthlyPaymentCategory)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
