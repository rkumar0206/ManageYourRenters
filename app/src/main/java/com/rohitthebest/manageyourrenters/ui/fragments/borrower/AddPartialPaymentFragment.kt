package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPartialPaymentBinding

class AddPartialPaymentFragment : Fragment(R.layout.fragment_add_partial_payment) {

    private var _binding: FragmentAddPartialPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddPartialPaymentBinding.bind(view)

        binding.addPartialFragmentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
