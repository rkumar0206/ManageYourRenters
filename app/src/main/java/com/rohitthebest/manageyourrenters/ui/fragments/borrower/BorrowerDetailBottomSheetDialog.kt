package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.databinding.BorrowerDetailBottomsheetDialogBinding
import com.rohitthebest.manageyourrenters.databinding.BorrowerDetailBottomsheetDialogLayoutBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BorrowerDetailBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: BorrowerDetailBottomsheetDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: BorrowerDetailBottomsheetDialogLayoutBinding

    private val borrowerViewModel by viewModels<BorrowerViewModel>()

    private lateinit var receivedBorrower: Borrower
    private var borrowerKey = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.borrower_detail_bottomsheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = BorrowerDetailBottomsheetDialogBinding.bind(view)
        includeBinding = binding.include

        getMessage()

        initListeners()
    }

    private fun getMessage() {

        arguments?.let { argumentBundle ->

            if (!argumentBundle.isEmpty) {

                val args = BorrowerDetailBottomSheetDialogArgs.fromBundle(argumentBundle)

                borrowerKey = args.borrowerKeyMessage.toString()

                if (borrowerKey.isValid()) {

                    observeBorrower()
                } else {

                    showToast(requireContext(), getString(R.string.something_went_wrong))
                    dismiss()
                }
            }
        }
    }

    private fun observeBorrower() {

        borrowerViewModel.getAllBorrower(borrowerKey)

        borrowerViewModel.allBorrowersList.observe(viewLifecycleOwner) {

            receivedBorrower = it[0]
            updateUI()
        }
    }

    private fun updateUI() {

        includeBinding.apply {

            borrowerNameTV.text = receivedBorrower.name

            borrowerAddedOnTV.text =
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(receivedBorrower.created)

            borrowerDueAmountTV.text =
                getString(R.string.total_dues, receivedBorrower.totalDueAmount.format(2))

            //email
            if (receivedBorrower.emailId.isValid()) borrowerEmailTV.text = receivedBorrower.emailId
            else borrowerEmailTV.text = getString(R.string.BLANK_DASH)

            //mobile
            if (receivedBorrower.mobileNumber.isValid()) borrowerMobileTV.text =
                receivedBorrower.mobileNumber
            else borrowerMobileTV.text = getString(R.string.BLANK_DASH)

            //identification document
            if (receivedBorrower.otherDocumentNumber.isValid()) {

                borrowerIdentificationDocNameTV.text = receivedBorrower.otherDocumentName
                borrowerIdentificationDocNumberTV.text = receivedBorrower.otherDocumentNumber
            } else {

                borrowerIdentificationDocNameTV.hide()
                borrowerIdentificationDocNumberTV.text = getString(R.string.BLANK_DASH)
            }

            // supporting document
            if (receivedBorrower.isSupportingDocAdded && receivedBorrower.supportingDocument != null) borrowerSupportingDocUrlTV.text =
                receivedBorrower.supportingDocument?.documentUrl
            else {
                borrowerSupportingDocUrlTV.text = getString(R.string.BLANK_DASH)
                borrowerSupportingDocUrlTV.changeTextColor(
                    requireContext(),
                    R.color.primaryTextColor
                )
            }
        }

    }

    private fun initListeners() {


        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        includeBinding.borrowerSupportingDocUrlTV.setOnClickListener {

            if (receivedBorrower.isSupportingDocAdded && receivedBorrower.supportingDocument != null) {

                Functions.openLinkInBrowser(
                    requireContext(),
                    receivedBorrower.supportingDocument?.documentUrl!!
                )
            }
        }

        includeBinding.borrowerMobileTV.setOnClickListener {

            if (receivedBorrower.mobileNumber.isValid()) {

                Functions.showMobileNumberOptionMenu(
                    requireActivity(),
                    includeBinding.borrowerMobileTV,
                    receivedBorrower.mobileNumber
                )
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}