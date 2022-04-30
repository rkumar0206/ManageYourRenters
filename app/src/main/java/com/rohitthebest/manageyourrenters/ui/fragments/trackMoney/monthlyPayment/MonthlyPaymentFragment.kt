package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentMonthlyPaymentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MonthlyPaymentFragment : Fragment(R.layout.fragment_monthly_payment) {

    private var _binding: FragmentMonthlyPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyPaymentBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
