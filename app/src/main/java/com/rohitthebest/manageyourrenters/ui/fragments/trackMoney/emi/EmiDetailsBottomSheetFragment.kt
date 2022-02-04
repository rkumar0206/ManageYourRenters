package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiDetailsBottomsheetBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.setDateInTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmiDetailsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEmiDetailsBottomsheetBinding? = null
    private val binding get() = _binding!!

    private val emiViewModel by viewModels<EMIViewModel>()

    private var receivedEMIKey = ""
    private lateinit var receivedEMI: EMI

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_emi_details_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmiDetailsBottomsheetBinding.bind(view)

        getMessage()

        binding.emiDetailsToolBar.setNavigationOnClickListener {

            dismiss()
        }
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

        emiViewModel.getEMIByKey(receivedEMIKey).observe(viewLifecycleOwner) { emi ->

            receivedEMI = emi
            updateUI()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {

        if (this::receivedEMI.isInitialized) {

            binding.include.apply {

                emiNameTV.text = receivedEMI.emiName
                startEmiDateTV.setDateInTextView(receivedEMI.startDate)
                monthsCompletedByTotalMonthsTV.text =
                    "${receivedEMI.monthsCompleted} / ${receivedEMI.totalMonths}"
                emiAmountPaidPerMonthTV.text =
                    "${receivedEMI.currencySymbol} ${receivedEMI.amountPaidPerMonth}"
                totalEmiAmountTV.text =
                    "${receivedEMI.totalMonths} * ${receivedEMI.amountPaidPerMonth} = ${receivedEMI.currencySymbol} ${calculateTotalEmi()}"
                emiAmountPaidTillNowTV.text =
                    "${receivedEMI.currencySymbol} ${receivedEMI.amountPaid}"
            }
        }
    }

    private fun calculateTotalEmi(): Double {

        return (receivedEMI.totalMonths * receivedEMI.amountPaidPerMonth)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
