package com.rohitthebest.manageyourrenters.ui.fragments.addContentFragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.databinding.AddRenterLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddRenterBinding
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertRenterToJSONString
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.generateRenterPassword
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.toStringM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class AddRenterFragment : Fragment(), View.OnClickListener {

    private val renterViewModel : RenterViewModel by viewModels()

    private var _binding: FragmentAddRenterBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddRenterLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAddRenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        includeBinding = binding.include
        initListeners()
        textWatchers()
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.addRenterBtn.setOnClickListener(this)

        includeBinding.mobileNumCodePicker.registerCarrierNumberEditText(includeBinding.renterMobileNumberET)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addRenterBtn.id -> {

                if (isValidForm()) {

                    initRenterForDatabase()
                }

            }

            binding.backBtn.id -> {

                requireActivity().onBackPressed()
            }
        }
    }

    private fun initRenterForDatabase() {

        val renter = Renter()

        renter.apply {
            timeStamp = System.currentTimeMillis()
            name = includeBinding.renterNameET.editText?.text.toString().trim()
            mobileNumber = includeBinding.mobileNumCodePicker.fullNumberWithPlus
            emailId = includeBinding.renterEmailET.editText?.text.toString().trim()
            otherDocumentName = includeBinding.otherDocumentNameET.text.toString().trim()
            otherDocumentNumber = includeBinding.otherDocumentNumber.text.toString().trim()
            roomNumber = includeBinding.renterRoomNumberET.editText?.text.toString().trim()
            address = includeBinding.renterAddressET.editText?.text.toString().trim()
            uid = getUid()!!
            renterId = System.currentTimeMillis().toStringM(36)
            renterPassword = generateRenterPassword(renterId, mobileNumber)
            key = "${System.currentTimeMillis().toStringM(69)}_${
                Random.nextLong(
                    100,
                    9223372036854775
                ).toStringM(69)
            }_${getUid()}"
            isSynced = getString(R.string.f)
        }

        if(isInternetAvailable(requireContext())) {

            renter.isSynced = getString(R.string.t)

            FirebaseServiceHelper.uploadDocumentToFireStore(
                requireContext(),
                convertRenterToJSONString(renter),
                getString(R.string.renters),
                renter.key!!
            )

            insertToDatabase(renter)
        }else {

            insertToDatabase(renter)
        }

    }

    private fun insertToDatabase(renter : Renter) {

        renterViewModel.insertRenter(renter)
        showToast(requireContext(), "Renter inserted")
        requireActivity().onBackPressed()
    }

    private fun isValidForm(): Boolean {

        if (includeBinding.renterNameET.editText?.text.toString().trim().isEmpty()) {

            includeBinding.renterNameET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (includeBinding.renterMobileNumberET.text.toString().trim().isEmpty()) {

            showMobileErrorTV()
            return false
        }

        if (includeBinding.renterAddressET.editText?.text.toString().trim().isEmpty()) {

            includeBinding.renterAddressET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (includeBinding.renterRoomNumberET.editText?.text.toString().trim().isEmpty()) {

            includeBinding.renterRoomNumberET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (!includeBinding.mobileNumCodePicker.isValidFullNumber) {

            showToast(requireContext(),"Please enter a valid mobile number!!", Toast.LENGTH_LONG)
            return false
        }

        return includeBinding.renterNameET.error == null
                && includeBinding.renterRoomNumberET.error == null
                && includeBinding.renterAddressET.error == null
                && includeBinding.renterMobileNumberET.text.toString().trim().isNotEmpty()
                && includeBinding.mobileNumCodePicker.isValidFullNumber

    }

    private fun textWatchers() {

        includeBinding.renterNameET.editText?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.isEmpty()!!) {

                    includeBinding.renterNameET.error = EDIT_TEXT_EMPTY_MESSAGE
                } else {

                    includeBinding.renterNameET.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        includeBinding.renterMobileNumberET.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.isEmpty()!!) {

                    showMobileErrorTV()
                } else {

                    hideMobileErrorTV()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        includeBinding.renterRoomNumberET.editText?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.isEmpty()!!) {

                    includeBinding.renterRoomNumberET.error = EDIT_TEXT_EMPTY_MESSAGE
                } else {

                    includeBinding.renterRoomNumberET.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        includeBinding.renterAddressET.editText?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.isEmpty()!!) {

                    includeBinding.renterAddressET.error = EDIT_TEXT_EMPTY_MESSAGE
                } else {

                    includeBinding.renterAddressET.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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