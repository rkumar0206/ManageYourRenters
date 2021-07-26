package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.CalculateInterestBottomSheetLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentCalculateInterestBinding

class CalculateInterestBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCalculateInterestBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: CalculateInterestBottomSheetLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_calculate_interest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCalculateInterestBinding.bind(view)
        includeBinding = binding.includeLayout

        initListener()
    }

    private fun initListener() {

        binding.calculateIntToolbar.setNavigationOnClickListener {

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}