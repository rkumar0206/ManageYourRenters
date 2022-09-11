package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters.addContentFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.ShowBillLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.ShowRenterPaymentBillBottomsheetDialogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowBillForConfirmationBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: ShowRenterPaymentBillBottomsheetDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeLayout: ShowBillLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
            R.layout.show_renter_payment_bill_bottomsheet_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ShowRenterPaymentBillBottomsheetDialogBinding.bind(view)
        includeLayout = binding.include
    }

    override fun onDestroyView() {
        super.onDestroyView()

        dismiss()
    }
}