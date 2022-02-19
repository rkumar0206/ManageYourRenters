package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentDeletedRentersBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeletedRentersFragment : Fragment(R.layout.fragment_deleted_renters) {

    private var _binding: FragmentDeletedRentersBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDeletedRentersBinding.bind(view)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
