package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters.addContentFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.AddRenterLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddRenterBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertJSONtoRenter
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertRenterToJSONString
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateRenterPassword
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isTextValid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.onTextChangedListener
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.toStringM
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class AddRenterFragment : Fragment(), View.OnClickListener {

    private val renterViewModel: RenterViewModel by viewModels()

    private var _binding: FragmentAddRenterBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddRenterLayoutBinding
    private var selectedDate: Long = 0L

    //received for editing vars
    private var receivedRenter: Renter? = null
    private var isMessageReceivesForEditing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddRenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        includeBinding = binding.include

        selectedDate = System.currentTimeMillis()
        includeBinding.dateAddedTV.text =
            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                selectedDate
            )

        getMessage()
        initListeners()
        textWatchers()
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

            binding.addRenterTitileTV.text = "Edit Renter"

            includeBinding.renterNameET.editText?.setText(
                renter.name
            )

            includeBinding.renterMobileNumberET.setText(
                renter.mobileNumber
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

            includeBinding.renterAddressET.editText?.setText(
                renter.address
            )

            includeBinding.dateAddedTV.text =
                WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                    renter.timeStamp
                )

            selectedDate = renter.timeStamp!!
        }
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.addRenterBtn.setOnClickListener(this)
        includeBinding.dateAddedCalendarPickBtn.setOnClickListener(this)

        includeBinding.mobileNumCodePicker.registerCarrierNumberEditText(includeBinding.renterMobileNumberET)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addRenterBtn.id -> {

                if (isValidForm()) {

                    initRenterForDatabase()
                }

            }

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

            binding.backBtn.id -> {

                requireActivity().onBackPressed()
            }
        }
    }

    private fun initRenterForDatabase() {

        val renter = Renter()

        if (isMessageReceivesForEditing) {

            renter.id = receivedRenter?.id
        }

        renter.apply {
            timeStamp = selectedDate
            name = includeBinding.renterNameET.editText?.text.toString().trim()
            mobileNumber = includeBinding.mobileNumCodePicker.fullNumberWithPlus
            emailId = includeBinding.renterEmailET.editText?.text.toString().trim()
            otherDocumentName = includeBinding.otherDocumentNameET.text.toString().trim()
            otherDocumentNumber = includeBinding.otherDocumentNumber.text.toString().trim()
            roomNumber = includeBinding.renterRoomNumberET.editText?.text.toString().trim()
            address = includeBinding.renterAddressET.editText?.text.toString().trim()
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
                "${System.currentTimeMillis().toStringM(69)}_${
                    Random.nextLong(
                        100,
                        9223372036854775
                    ).toStringM(69)
                }_${getUid()}"
            } else {
                receivedRenter?.key
            }
            dueOrAdvanceAmount = 0.0

            isSynced = getString(R.string.f)
        }

        if (isInternetAvailable(requireContext())) {

            renter.isSynced = getString(R.string.t)

            FirebaseServiceHelper.uploadDocumentToFireStore(
                requireContext(),
                convertRenterToJSONString(renter),
                getString(R.string.renters),
                renter.key!!
            )

            insertToDatabase(renter)
        } else {

            insertToDatabase(renter)
        }

    }

    private fun insertToDatabase(renter: Renter) {

        renterViewModel.insertRenter(renter)
        showToast(requireContext(), "Renter inserted")
        requireActivity().onBackPressed()
    }

    private fun isValidForm(): Boolean {

        if (!includeBinding.renterNameET.editText?.isTextValid()!!) {

            includeBinding.renterNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (!includeBinding.renterMobileNumberET.isTextValid()) {

            showMobileErrorTV()
            return false
        }

        if (!includeBinding.renterAddressET.editText?.isTextValid()!!) {

            includeBinding.renterAddressET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (!includeBinding.renterRoomNumberET.editText?.isTextValid()!!) {

            includeBinding.renterRoomNumberET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (!isMessageReceivesForEditing) {

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
                && includeBinding.renterRoomNumberET.error == null
                && includeBinding.renterAddressET.error == null
                && includeBinding.renterMobileNumberET.isTextValid()

    }

    private fun textWatchers() {

        includeBinding.renterNameET.editText?.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.renterNameET.error = EDIT_TEXT_EMPTY_MESSAGE
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

        includeBinding.renterRoomNumberET.editText?.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.renterRoomNumberET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.renterRoomNumberET.error = null
            }

        }

        includeBinding.renterAddressET.editText?.onTextChangedListener { s ->

            if (s?.isEmpty()!!) {

                includeBinding.renterAddressET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                includeBinding.renterAddressET.error = null
            }

        }
    }

    private fun showMobileErrorTV() {

        try {

            includeBinding.mobileNumErrorTV.show()
            includeBinding.mobileNumErrorTV.text = EDIT_TEXT_EMPTY_MESSAGE
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