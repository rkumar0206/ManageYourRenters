package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIPaymentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EMIPaymentFragment : Fragment(R.layout.fragment_emi_payment) {

    private var _binding: FragmentEmiPaymentBinding? = null
    private val binding get() = _binding!!

    private val emiPaymentViewModel by viewModels<EMIPaymentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmiPaymentBinding.bind(view)

        shouldShowProgressBar(true)

        lifecycleScope.launch {

            delay(250)

            getEMIPayments()
        }
    }

    private fun getEMIPayments() {

        //emiPaymentViewModel.getAllEMIPaymentsByEMIKey()
    }

    private fun shouldShowProgressBar(isVisible: Boolean) {

        binding.progressBar.isVisible = isVisible
    }

    private fun shouldShowNoEMIPaymentAddedTV(isVisible: Boolean) {

        binding.noEmiPaymentAddedMessageTV.isVisible = isVisible
        binding.emiPaymentsRV.isVisible = !isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
