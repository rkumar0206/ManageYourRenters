package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.databinding.AddRenterLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddRenterBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.fromBorrowerToString
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateKey
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateRenterPassword
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isTextValid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.onTextChangedListener
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showCalendarDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.toStringM
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddBorrowerFragment : Fragment(R.layout.fragment_add_renter), View.OnClickListener {

    private var _binding: FragmentAddRenterBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddRenterLayoutBinding
    private var selectedDate: Long = 0L
    private var isMessageReceivesForEditing = false

    private val borrowerViewModel by viewModels<BorrowerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddRenterBinding.bind(view)

        includeBinding = binding.include

        selectedDate = System.currentTimeMillis()
        includeBinding.dateAddedTV.text =
            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                selectedDate
            )

        // removing the views that is not required here
        includeBinding.renterRoomNumberET.hide()
        includeBinding.renterAddressET.hide()

        binding.addRenterTitileTV.text = getString(R.string.add_borrower)

        initListeners()
        textWatchers()
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

                if (isInternetAvailable(requireContext())) {

                    if (isValidForm()) {

                        initBorrowerData()
                    }

                } else {

                    showNoInternetMessage(requireContext())
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

    private fun initBorrowerData() {

        val borrower = Borrower()

        borrower.apply {

            created = selectedDate
            modified = selectedDate
            name = includeBinding.renterNameET.editText?.text.toString().trim()
            mobileNumber = includeBinding.mobileNumCodePicker.fullNumberWithPlus
            emailId = includeBinding.renterEmailET.editText?.text.toString().trim()
            otherDocumentName = includeBinding.otherDocumentNameET.text.toString().trim()
            otherDocumentNumber = includeBinding.otherDocumentNumber.text.toString().trim()

            uid = getUid()!!

            borrowerId = System.currentTimeMillis().toStringM(36)
            borrowerPassword = generateRenterPassword(borrowerId, mobileNumber)
            key = generateKey("_${uid}")
            totalDueAmount = 0.0
            isSynced = false
        }

        if (isInternetAvailable(requireContext())) {

            borrower.isSynced = true

            FirebaseServiceHelper.uploadDocumentToFireStore(
                requireContext(),
                fromBorrowerToString(borrower),
                getString(R.string.borrowers),
                borrower.key
            )

            insertToDatabase(borrower)
        } else {

            insertToDatabase(borrower)
        }
    }

    private fun insertToDatabase(borrower: Borrower) {

        borrowerViewModel.insertBorrower(borrower)
        showToast(requireContext(), "Borrower added")
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
