package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.databinding.CalculateInterestBottomSheetLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentCalculateInterestBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel

class CalculateInterestBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCalculateInterestBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: CalculateInterestBottomSheetLayoutBinding

    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()

    private var receivedBorrowerPaymentKey = ""
    private lateinit var receivedBorrowerPayment: BorrowerPayment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_calculate_interest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCalculateInterestBinding.bind(view)
        includeBinding = binding.includeLayout

        initListener()
        getMessage()
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {
                CalculateInterestBottomSheetFragmentArgs.fromBundle(it)
            }

            receivedBorrowerPaymentKey = args?.paymentKey!!

            getBorrowerPayment()
        }
    }

    private fun getBorrowerPayment() {

        borrowerPaymentViewModel.getBorrowerPaymentByKey(receivedBorrowerPaymentKey)
            .observe(viewLifecycleOwner, { borrowerPayment ->

                receivedBorrowerPayment = borrowerPayment
            })
    }

    private fun initListener() {

        binding.calculateIntToolbar.setNavigationOnClickListener {

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}