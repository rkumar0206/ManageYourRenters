package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentTrackMoneyHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackMoneyHomeFragment : Fragment(R.layout.fragment_track_money_home) {

    private var _binding: FragmentTrackMoneyHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrackMoneyHomeBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
