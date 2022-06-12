package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.databinding.AddRenterLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEditRenterBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.SUPPORTING_DOCUMENT_HELPER_MODEL_KEY
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateRenterPassword
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.toStringM
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddEditBorrowerFragment"

@AndroidEntryPoint
class AddEditBorrowerFragment : Fragment(R.layout.fragment_add_edit_renter), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener,
    SupportingDocumentDialogFragment.OnBottomSheetDismissListener {

    private var _binding: FragmentAddEditRenterBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddRenterLayoutBinding
    private var selectedDate: Long = 0L
    private var isMessageReceivedForEditing = false
    private var receivedBorrower: Borrower? = null
    private var receivedBorrowerKey: String = ""

    private lateinit var supportingDocmtHelperModel: SupportingDocumentHelperModel

    private val borrowerViewModel by viewModels<BorrowerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditRenterBinding.bind(view)

        includeBinding = binding.include

        selectedDate = System.currentTimeMillis()
        includeBinding.dateAddedTV.text =
            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                selectedDate
            )
        supportingDocmtHelperModel = SupportingDocumentHelperModel()

        // removing the views that is not required here
        includeBinding.renterRoomNumberET.hide()
        includeBinding.renterAddressET.hide()

        binding.addRenterToolbar.title = getString(R.string.add_borrower)

        initListeners()
        textWatchers()
        getMessage()
    }

    private fun getMessage() {

        try {

            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    AddEditBorrowerFragmentArgs.fromBundle(it)
                }

                args?.borrowerKey?.let { key ->
                    receivedBorrowerKey = key
                }

                isMessageReceivedForEditing = true

                getTheBorrower()

            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getTheBorrower() {

        borrowerViewModel.getBorrowerByKey(receivedBorrowerKey)
            .observe(viewLifecycleOwner) { borrower ->

                receivedBorrower = borrower
                updateUI()
            }
    }

    private fun updateUI() {

        receivedBorrower?.let { b ->

            binding.addRenterToolbar.title = getString(R.string.edit_borrower)

            includeBinding.renterNameET.editText?.setText(
                b.name
            )

            includeBinding.renterMobileNumberET.setText(
                b.mobileNumber.substring(3)
            )

            includeBinding.renterEmailET.editText?.setText(
                b.emailId
            )

            includeBinding.otherDocumentNameET.setText(
                b.otherDocumentName
            )

            includeBinding.otherDocumentNumber.setText(
                b.otherDocumentNumber
            )

            includeBinding.dateAddedTV.text =
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    b.created
                )

            selectedDate = b.created
        }

        includeBinding.addSupportingDocCB.hide()
        includeBinding.viewEditSupportingDoc.hide()
    }

    private fun initListeners() {

        includeBinding.addSupportingDocCB.isChecked = false

        binding.addRenterToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addRenterToolbar.menu.findItem(R.id.menu_add_person).setOnMenuItemClickListener {

            if (isValidForm()) {

                initBorrowerData()
            }

            true
        }

        includeBinding.dateAddedCalendarPickBtn.setOnClickListener(this)
        includeBinding.mobileNumCodePicker.registerCarrierNumberEditText(includeBinding.renterMobileNumberET)
        includeBinding.addSupportingDocCB.setOnCheckedChangeListener(this)
        includeBinding.viewEditSupportingDoc.setOnClickListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when (buttonView?.id) {

            includeBinding.addSupportingDocCB.id -> {

                if (isChecked) {

                    supportingDocmtHelperModel = SupportingDocumentHelperModel()
                    supportingDocmtHelperModel.modelName = getString(R.string.borrowers)
                    showSupportDocumentBottomSheetDialog()
                    includeBinding.viewEditSupportingDoc.show()
                } else {

                    includeBinding.viewEditSupportingDoc.hide()
                }
            }
        }
    }

    private fun showSupportDocumentBottomSheetDialog() {

        val bundle = Bundle()
        bundle.putString(
            SUPPORTING_DOCUMENT_HELPER_MODEL_KEY,
            supportingDocmtHelperModel.convertToJsonString()
        )

        requireActivity().supportFragmentManager.let {

            SupportingDocumentDialogFragment.newInstance(bundle)
                .apply {
                    show(it, TAG)
                }.setOnBottomSheetDismissListener(this)
        }
    }

    override fun onBottomSheetDismissed(
        isDocumentAdded: Boolean,
        supportingDocumentHelperModel: SupportingDocumentHelperModel
    ) {

        if (!isDocumentAdded) {
            includeBinding.addSupportingDocCB.isChecked = false
        } else {

            supportingDocmtHelperModel = supportingDocumentHelperModel
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            includeBinding.dateAddedCalendarPickBtn.id -> {

                showCalendarDialog(
                    selectedDate,
                    { requireActivity().supportFragmentManager },
                    {

                        selectedDate = it

                        includeBinding.dateAddedTV.text =
                            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                                selectedDate
                            )
                    }
                )
            }

            includeBinding.viewEditSupportingDoc.id -> {

                showSupportDocumentBottomSheetDialog()
            }
        }

    }

    private fun initBorrowerData() {

        var borrower = Borrower()

        if (isMessageReceivedForEditing) {

            borrower = receivedBorrower!!
        }

        borrower.apply {

            created = selectedDate
            modified = System.currentTimeMillis()

            name = includeBinding.renterNameET.editText?.text.toString().trim()
            mobileNumber = includeBinding.mobileNumCodePicker.fullNumberWithPlus
            emailId = includeBinding.renterEmailET.editText?.text.toString().trim()
            otherDocumentName = includeBinding.otherDocumentNameET.text.toString().trim()
            otherDocumentNumber = includeBinding.otherDocumentNumber.text.toString().trim()

            uid = getUid()!!

            borrowerId = if (!isMessageReceivedForEditing)
                System.currentTimeMillis().toStringM(36) else receivedBorrower?.borrowerId!!

            borrowerPassword =
                if (!isMessageReceivedForEditing) generateRenterPassword(borrowerId, mobileNumber)
                else receivedBorrower?.borrowerPassword!!

            key = if (!isMessageReceivedForEditing) generateKey("_${uid}") else receivedBorrowerKey
            totalDueAmount =
                if (!isMessageReceivedForEditing) 0.0 else receivedBorrower?.totalDueAmount!!
            isSynced = false

            if (!isMessageReceivedForEditing && includeBinding.addSupportingDocCB.isChecked) {

                isSupportingDocAdded = true

                if (supportingDocmtHelperModel.documentType == DocumentType.URL) {

                    supportingDocument = SupportingDocument(
                        supportingDocmtHelperModel.documentName,
                        supportingDocmtHelperModel.documentUrl,
                        supportingDocmtHelperModel.documentType
                    )
                }
            }
        }

        if (!isMessageReceivedForEditing
            && borrower.isSupportingDocAdded
            && supportingDocmtHelperModel.documentType != DocumentType.URL
        ) {

            // if the document type is not URL, then we need internet connection to upload the uri
            if (!isInternetAvailable(requireContext())) {
                showToast(
                    requireContext(),
                    getString(R.string.internet_required_message_for_uploading_doc),
                    Toast.LENGTH_LONG
                )
                return
            }
        }

        insertToDatabase(borrower)
    }

    private fun insertToDatabase(borrower: Borrower) {

        if (!isMessageReceivedForEditing) {

            // insert
            if (borrower.isSupportingDocAdded && borrower.supportingDocument?.documentType != DocumentType.URL)
                borrowerViewModel.insertBorrower(
                    requireContext(),
                    borrower,
                    supportingDocmtHelperModel
                )
            else
                borrowerViewModel.insertBorrower(requireContext(), borrower)

            showToast(requireContext(), "Borrower added")
        } else {
            // update
            borrowerViewModel.updateBorrower(requireContext(), borrower)
            showToast(requireContext(), "Borrower info updated")
        }

        requireActivity().onBackPressed()
    }

    private fun isValidForm(): Boolean {

        if (!includeBinding.renterNameET.editText?.isTextValid()!!) {

            includeBinding.renterNameET.error = Constants.EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (!includeBinding.renterMobileNumberET.isTextValid()) {

            showMobileErrorTV()
            return false
        }


        if (!isMessageReceivedForEditing) {

            if (!includeBinding.mobileNumCodePicker.isValidFullNumber) {

                showToast(
                    requireContext(),
                    getString(R.string.mobileNumberErrorMessage),
                    Toast.LENGTH_LONG
                )
                return false
            }
        }

        return includeBinding.renterNameET.error == null
                && includeBinding.renterMobileNumberET.isTextValid()

    }

    private fun textWatchers() {

        includeBinding.renterNameET.editText?.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.renterNameET.error = Constants.EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.renterNameET.error = null
            }

        }

        includeBinding.renterMobileNumberET.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                showMobileErrorTV()
            } else if (!includeBinding.mobileNumCodePicker.isValidFullNumber) {

                showMobileErrorTV()
                includeBinding.mobileNumErrorTV.text = getString(R.string.mobileNumberErrorMessage)

            } else {

                hideMobileErrorTV()
            }
        }
    }

    private fun showMobileErrorTV() {

        try {

            includeBinding.mobileNumErrorTV.show()
            includeBinding.mobileNumErrorTV.text = Constants.EDIT_TEXT_EMPTY_MESSAGE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideMobileErrorTV() {

        try {

            includeBinding.mobileNumErrorTV.hide()
            includeBinding.mobileNumErrorTV.text = ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }
}
