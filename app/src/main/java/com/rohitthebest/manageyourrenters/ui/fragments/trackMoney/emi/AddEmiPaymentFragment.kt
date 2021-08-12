package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEmiPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEmiPaymentFragment : Fragment(R.layout.fragment_add_emi_payment) {

    private var _binding: FragmentAddEmiPaymentBinding? = null
    private val binding get() = _binding!!

    private val emiPaymentViewModel by viewModels<EMIPaymentViewModel>()

    private val emiViewModel by viewModels<EMIViewModel>()

    private var receivedEMIKey = ""
    private lateinit var receivedEMI: EMI


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEmiPaymentBinding.bind(view)

        getMessage()

    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {

                EMIPaymentFragmentArgs.fromBundle(it)
            }

            receivedEMIKey = args?.emiKeyMessage!!

            getEMI()
        }
    }

    private fun getEMI() {

        emiViewModel.getEMIByKey(receivedEMIKey).observe(viewLifecycleOwner, { emi ->

            receivedEMI = emi
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
