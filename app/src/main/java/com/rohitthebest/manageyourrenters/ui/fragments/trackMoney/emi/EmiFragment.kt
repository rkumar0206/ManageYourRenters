package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        setProgressBarVisibility(true)

        lifecycleScope.launch {

            delay(200)
            getEMIs()
        }

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        binding.emiRV.apply {

            //todo : do set up
        }
    }

    private fun getEMIs() {

        emiViewModel.getAllEMIs().observe(viewLifecycleOwner, { emiList ->

            Log.d(TAG, "getEMIs: $emiList")

            if (emiList.isEmpty()) {

                setNoEMIAddedMessageTVVisibility(true)
            } else {

                setNoEMIAddedMessageTVVisibility(false)
            }

            //todo : adapter.submitList()

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

    private fun setProgressBarVisibility(isVisible: Boolean) {

        binding.emiRV.isVisible = !isVisible
        binding.progressBar.isVisible = isVisible
    }

    private fun setNoEMIAddedMessageTVVisibility(isVisible: Boolean) {

        binding.emiRV.isVisible = !isVisible
        binding.noEmiAddedMessageTV.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
