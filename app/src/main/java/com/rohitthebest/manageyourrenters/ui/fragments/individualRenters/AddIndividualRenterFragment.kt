package com.rohitthebest.manageyourrenters.ui.fragments.individualRenters

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.AddRenterLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddRenterBinding
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime

class AddIndividualRenterFragment : Fragment(R.layout.fragment_add_renter), View.OnClickListener {

    private var _binding: FragmentAddRenterBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddRenterLayoutBinding
    private var selectedDate: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddRenterBinding.bind(view)

        includeBinding = binding.include

        selectedDate = System.currentTimeMillis()
        includeBinding.dateAddedTV.text =
            WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                selectedDate
            )

        // removing the views that is not required
        includeBinding.renterRoomNumberET.hide()
        includeBinding.renterAddressET.hide()

        initListeners()
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.addRenterBtn.setOnClickListener(this)
        includeBinding.dateAddedCalendarPickBtn.setOnClickListener(this)
        includeBinding.mobileNumCodePicker.registerCarrierNumberEditText(includeBinding.renterMobileNumberET)

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            includeBinding.dateAddedCalendarPickBtn.id -> {

                Functions.showCalendarDialog(
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
