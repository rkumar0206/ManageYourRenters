package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.StatusEnum
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.RenterDetailBottomsheetDialogBinding
import com.rohitthebest.manageyourrenters.databinding.RenterDetailBottomsheetDialogLayoutBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.changeTextColor
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RenterDetailBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: RenterDetailBottomsheetDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: RenterDetailBottomsheetDialogLayoutBinding

    private val renterViewModel by viewModels<RenterViewModel>()

    private lateinit var receivedRenter: Renter
    private var renterKey = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.renter_detail_bottomsheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = RenterDetailBottomsheetDialogBinding.bind(view)
        includeBinding = binding.include

        getMessage()

        initListeners()
    }

    private fun getMessage() {

        arguments?.let { argumentBundle ->

            if (!argumentBundle.isEmpty) {

                val args = RenterDetailBottomSheetDialogArgs.fromBundle(argumentBundle)

                renterKey = args.renterKey.toString()

                if (renterKey.isValid()) {

                    observeRenter()
                } else {

                    showToast(requireContext(), getString(R.string.something_went_wrong))
                    dismiss()
                }
            }
        }
    }

    private fun observeRenter() {

        renterViewModel.getRenterByKey(renterKey).observe(viewLifecycleOwner) { renter ->

            receivedRenter = renter
            updateUI()
        }
    }

    private fun updateUI() {

        includeBinding.apply {

            renterNameTV.text = receivedRenter.name

            //status
            if (receivedRenter.status == StatusEnum.ACTIVE) renterStatus.setImageResource(R.drawable.ic_baseline_status_active)
            else renterStatus.setImageResource(R.drawable.ic_baseline_status_inactive)

            renterRoomTV.text = receivedRenter.roomNumber

            //email
            if (receivedRenter.emailId.isValid()) renterEmailTV.text = receivedRenter.emailId
            else renterEmailTV.text = getString(R.string.BLANK_DASH)

            //mobile
            if (receivedRenter.mobileNumber.isValid()) renterMobileTV.text =
                receivedRenter.mobileNumber
            else renterMobileTV.text = getString(R.string.BLANK_DASH)

            //address
            if (receivedRenter.address.isValid()) renterAddressTV.text = receivedRenter.address
            else renterAddressTV.text = getString(R.string.BLANK_DASH)

            //identification document
            if (receivedRenter.otherDocumentNumber.isValid()) {

                renterIdentificationDocNameTV.text = receivedRenter.otherDocumentName
                renterIdentificationDocNumberTV.text = receivedRenter.otherDocumentNumber
            } else {

                renterIdentificationDocNameTV.hide()
                renterIdentificationDocNumberTV.text = getString(R.string.BLANK_DASH)
            }

            // supporting document
            if (receivedRenter.isSupportingDocAdded && receivedRenter.supportingDocument != null) renterSupportingDocUrlTV.text =
                receivedRenter.supportingDocument?.documentUrl
            else {
                renterSupportingDocUrlTV.text = getString(R.string.BLANK_DASH)
                renterSupportingDocUrlTV.changeTextColor(requireContext(), R.color.primaryTextColor)
            }
        }

    }

    private fun initListeners() {


        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        includeBinding.renterSupportingDocUrlTV.setOnClickListener {

            if (receivedRenter.isSupportingDocAdded && receivedRenter.supportingDocument != null) {

                Functions.openLinkInBrowser(
                    requireContext(),
                    receivedRenter.supportingDocument?.documentUrl!!
                )
            }
        }

        includeBinding.renterMobileTV.setOnClickListener {

            if (receivedRenter.emailId.isValid()) {

                Functions.showMobileNumberOptionMenu(
                    requireActivity(),
                    includeBinding.renterMobileTV,
                    receivedRenter.mobileNumber
                )
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}