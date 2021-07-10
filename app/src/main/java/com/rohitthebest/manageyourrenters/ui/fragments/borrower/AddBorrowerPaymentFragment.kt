package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentAddBorrowerPaymentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddBorrowerPaymentFragment : Fragment(R.layout.fragment_add_borrower_payment) {

    private var _binding: FragmentAddBorrowerPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBorrowerPaymentBinding.bind(view)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
