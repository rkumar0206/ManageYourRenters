package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EMIPaymentFragment : Fragment(R.layout.fragment_emi_payment) {

    private var _binding: FragmentEmiPaymentBinding? = null
    private val binding get() = _binding!!

    private val emiPaymentViewModel by viewModels<EMIPaymentViewModel>()
    private val emiViewModel by viewModels<EMIViewModel>()

    private var receivedEMIKey = ""
    private lateinit var receivedEMI: EMI

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmiPaymentBinding.bind(view)

        shouldShowProgressBar(true)

        getMessage()

        initListeners()
    }


    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {

                EMIPaymentFragmentArgs.fromBundle(it)
            }

            receivedEMIKey = args?.emiKeyMessage!!

            getEMIPayments()
            getEMI()

        }
    }

    private fun getEMI() {

        emiViewModel.getEMIByKey(receivedEMIKey).observe(viewLifecycleOwner, { emi ->

            receivedEMI = emi
        })
    }

    private fun getEMIPayments() {

        emiPaymentViewModel.getAllEMIPaymentsByEMIKey(receivedEMIKey).observe(viewLifecycleOwner, {

            if (it.isEmpty()) {

                shouldShowNoEMIPaymentAddedTV(true)
            } else {

                shouldShowNoEMIPaymentAddedTV(false)
            }

            //todo : adapter.submit list

            shouldShowProgressBar(false)
        })
    }

    private fun initListeners() {

        binding.emiPaymentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.emiPaymentToolbar.menu.findItem(R.id.menu_show_emi_details)
            .setOnMenuItemClickListener {

                val action =
                    EMIPaymentFragmentDirections.actionEMIPaymentFragmentToEmiDetailsBottomSheetFragment(
                        receivedEMIKey
                    )
                findNavController().navigate(action)

                true
            }
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
