package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmiFragment : Fragment(R.layout.fragment_emi) {

    private var _binding: FragmentEmiBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmiBinding.bind(view)

        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
