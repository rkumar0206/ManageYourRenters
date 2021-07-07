package com.rohitthebest.manageyourrenters.ui.fragments.individualRenters

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentIndividualRenterHomeBinding

class IndividualRenterHomeFragment : Fragment(R.layout.fragment_individual_renter_home) {

    private var _binding: FragmentIndividualRenterHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentIndividualRenterHomeBinding.bind(view)

        binding.addIndividualRenterFAB.setOnClickListener {

            findNavController().navigate(R.id.action_individualRenterHomeFragment_to_addIndividualRenterFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
