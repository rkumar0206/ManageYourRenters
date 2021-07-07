package com.rohitthebest.manageyourrenters.ui.fragments.individualRenters

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.AddRenterLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentAddRenterBinding
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide

class AddIndividualRenterFragment : Fragment(R.layout.fragment_add_renter), View.OnClickListener {

    private var _binding: FragmentAddRenterBinding? = null
    private val binding get() = _binding!!

    private lateinit var includeBinding: AddRenterLayoutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddRenterBinding.bind(view)

        includeBinding = binding.include

        includeBinding.renterRoomNumberET.hide()
        includeBinding.renterAddressET.hide()

        initListeners()
    }

    private fun initListeners() {

        binding.backBtn.setOnClickListener(this)
        binding.addRenterBtn.setOnClickListener(this)
        includeBinding.dateAddedCalendarPickBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

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
