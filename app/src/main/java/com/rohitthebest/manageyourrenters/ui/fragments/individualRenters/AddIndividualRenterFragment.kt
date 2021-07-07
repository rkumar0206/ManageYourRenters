package com.rohitthebest.manageyourrenters.ui.fragments.individualRenters

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentAddInvidualRenterBinding

class AddIndividualRenterFragment : Fragment(R.layout.fragment_add_invidual_renter) {

    private var _binding: FragmentAddInvidualRenterBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddInvidualRenterBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
