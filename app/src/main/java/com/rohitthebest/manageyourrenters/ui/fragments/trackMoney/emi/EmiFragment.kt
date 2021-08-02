package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "EmiFragment"

@AndroidEntryPoint
class EmiFragment : Fragment(R.layout.fragment_emi) {

    private var _binding: FragmentEmiBinding? = null
    private val binding get() = _binding!!

    private val emiViewModel by viewModels<EMIViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmiBinding.bind(view)

        initListeners()

        getEMIs()
    }

    private fun getEMIs() {

        emiViewModel.getAllEMIs().observe(viewLifecycleOwner, {

            Log.d(TAG, "getEMIs: $it")
        })
    }

    private fun initListeners() {

        binding.emiFragmentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addEmiCategoryFAB.setOnClickListener {

            findNavController().navigate(R.id.action_emiFragment_to_addEditEMIFragment)
        }

        binding.emiFragmentToolbar.menu.findItem(R.id.menu_search_home)
            .setOnMenuItemClickListener {

                //todo : add search functionality in this fragment
                true
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
