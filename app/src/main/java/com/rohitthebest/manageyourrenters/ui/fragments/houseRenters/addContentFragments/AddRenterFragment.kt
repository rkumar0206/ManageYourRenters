package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters.addContentFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.AddRenterLayoutV2Binding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddEditRenterBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateRenterPassword
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.toStringM
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "AddRenterFragment"

@AndroidEntryPoint
class AddRenterFragment : Fragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener,
    SupportingDocumentDialogFragment.OnBottomSheetDismissListener {

    private val renterViewModel: RenterViewModel by viewModels()

    private var _binding: FragmentAddEditRenterBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddRenterLayoutV2Binding
    private var selectedDate: Long = 0L

    //received for editing vars
    private var receivedRenter: Renter? = null
    private var isMessageReceivesForEditing = false

    private lateinit var supportingDocmtHelperModel: SupportingDocumentHelperModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddEditRenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        includeBinding = binding.include

        selectedDate = System.currentTimeMillis()
        includeBinding.dateAddedTV.text =
            WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                selectedDate
            )
        supportingDocmtHelperModel = SupportingDocumentHelperModel()

        getMessage()
        initListeners()
        textWatchers()
        setUpRenterAddressET()
    }

    private fun setUpRenterAddressET() {

        renterViewModel.getAllDistinctAddress().observe(viewLifecycleOwner) { addresses ->

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                addresses
            )

            includeBinding.renterAddressET.setAdapter(adapter)
        }
    }

    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    AddRenterFragmentArgs.fromBundle(it)
                }

                val message = args?.editRenterMessage

                receivedRenter = convertJSONtoRenter(message)
                isMessageReceivesForEditing = true

                updateUI()
            }
        } catch (e: Exception) {

            e.printStackTrace()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {

        receivedRenter?.let { renter ->

            binding.addRenterToolbar.menu.findItem(R.id.menu_add_person)
                .setIcon(R.drawable.ic_baseline_save_24)
            binding.addRenterToolbar.title = "Edit Renter"

            includeBinding.renterNameET.editText?.setText(
                renter.name
            )

            includeBinding.renterMobileNumberET.editText?.setText(
                renter.mobileNumber.substring(3)
            )

            includeBinding.renterEmailET.editText?.setText(
                renter.emailId
            )

            includeBinding.otherDocumentNameET.setText(
                renter.otherDocumentName
            )

            includeBinding.otherDocumentNumber.setText(
                renter.otherDocumentNumber
            )

            includeBinding.renterRoomNumberET.editText?.setText(
                renter.roomNumber
            )

            includeBinding.renterAddressET.setText(
                renter.address
            )

            includeBinding.dateAddedTV.text =
                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                    renter.timeStamp
                )

            selectedDate = renter.timeStamp!!
        }

        includeBinding.addSupportingDocCB.hide()
        includeBinding.viewEditSupportingDoc.hide()
    }

    private fun initListeners() {

        binding.addRenterToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addRenterToolbar.menu.findItem(R.id.menu_add_person).setOnMenuItemClickListener {

            if (isValidForm()) {

                initRenterForDatabase()
            }

            true
        }

        includeBinding.dateAddedCalendarPickBtn.setOnClickListener(this)
        includeBinding.addSupportingDocCB.setOnCheckedChangeListener(this)
        includeBinding.viewEditSupportingDoc.setOnClickListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when (buttonView?.id) {

            includeBinding.addSupportingDocCB.id -> {

                if (isChecked) {

                    supportingDocmtHelperModel = SupportingDocumentHelperModel()
                    supportingDocmtHelperModel.modelName = getString(R.string.renters)
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
            Constants.SUPPORTING_DOCUMENT_HELPER_MODEL_KEY,
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
                            WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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

    private fun initRenterForDatabase() {

        var renter = Renter()

        if (isMessageReceivesForEditing) {

            renter = receivedRenter!!.copy()
        }

        renter.modified = System.currentTimeMillis()

        renter.apply {
            timeStamp = selectedDate
            name = includeBinding.renterNameET.editText?.text.toString().trim()
            mobileNumber = includeBinding.renterMobileNumberET.editText?.text.toString().trim()
            emailId = includeBinding.renterEmailET.editText?.text.toString().trim()
            otherDocumentName = includeBinding.otherDocumentNameET.text.toString().trim()
            otherDocumentNumber = includeBinding.otherDocumentNumber.text.toString().trim()
            roomNumber = includeBinding.renterRoomNumberET.editText?.text.toString().trim()
            address = includeBinding.renterAddressET.text.toString().trim()
            uid = getUid()!!

            renterId = if (!isMessageReceivesForEditing) {
                System.currentTimeMillis().toStringM(36)
            } else {
                receivedRenter?.renterId!!
            }

            renterPassword = if (!isMessageReceivesForEditing) {

                generateRenterPassword(renterId, mobileNumber)
            } else {
                receivedRenter?.renterPassword!!
            }

            key = if (!isMessageReceivesForEditing) {

                generateKey("_${getUid()}")

            } else {
                receivedRenter?.key
            }
            dueOrAdvanceAmount =
                if (!isMessageReceivesForEditing) 0.0 else receivedRenter?.dueOrAdvanceAmount!!

            isSynced = getString(R.string.f)

            if (!isMessageReceivesForEditing && includeBinding.addSupportingDocCB.isChecked) {

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

        if (!isMessageReceivesForEditing
            && renter.isSupportingDocAdded
            && supportingDocmtHelperModel.documentType != DocumentType.URL
        ) {

            // if the document type is not URL, then we need internet connection to upload the uri
            if (!Functions.isInternetAvailable(requireContext())) {
                showToast(
                    requireContext(),
                    getString(R.string.internet_required_message_for_uploading_doc),
                    Toast.LENGTH_LONG
                )
                return
            }
        }

        insertRenterToDatabase(renter)
    }

    private fun insertRenterToDatabase(renter: Renter) {

        if (!isMessageReceivesForEditing) {

            // insert
            if (renter.isSupportingDocAdded && renter.supportingDocument?.documentType != DocumentType.URL)
                renterViewModel.insertRenter(
                    renter,
                    supportingDocmtHelperModel
                )
            else
                renterViewModel.insertRenter(renter)

            showToast(requireContext(), "Renter added")
        } else {
            // update
            renterViewModel.updateRenter(receivedRenter!!, renter)
            showToast(requireContext(), "Renter info updated")
        }

        requireActivity().onBackPressed()
    }

    private fun isValidForm(): Boolean {

        if (!includeBinding.renterNameET.editText?.isTextValid()!!) {

            includeBinding.renterNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (!includeBinding.renterRoomNumberET.editText?.isTextValid()!!) {

            includeBinding.renterRoomNumberET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (includeBinding.otherDocumentNameET.isTextValid()) {

            if (!includeBinding.otherDocumentNumber.isTextValid()) {

                includeBinding.otherDocumentNumber.requestFocus()
                showToast(requireContext(), "Add document number")
                return false
            }
        }

        if (includeBinding.otherDocumentNumber.isTextValid()) {

            if (!includeBinding.otherDocumentNameET.isTextValid()) {

                includeBinding.otherDocumentNameET.requestFocus()
                showToast(requireContext(), "Add document name")
                return false
            }
        }


        return includeBinding.renterNameET.error == null
                && includeBinding.renterRoomNumberET.error == null

    }

    private fun textWatchers() {

        includeBinding.renterNameET.editText?.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.renterNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.renterNameET.error = null
            }

        }

        includeBinding.renterRoomNumberET.editText?.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.renterRoomNumberET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.renterRoomNumberET.error = null
            }

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }
}