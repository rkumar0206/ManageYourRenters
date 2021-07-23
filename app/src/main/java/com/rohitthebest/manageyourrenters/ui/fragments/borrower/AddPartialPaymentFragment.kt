package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentAddPartialPaymentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPartialPaymentFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAddPartialPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_partial_payment, container, false)
    }

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
