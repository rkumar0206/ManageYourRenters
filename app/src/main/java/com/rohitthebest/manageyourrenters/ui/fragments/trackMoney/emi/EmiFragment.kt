package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.emiAdapters.EMIAdapter
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.searchText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "EmiFragment"

@AndroidEntryPoint
class EmiFragment : Fragment(R.layout.fragment_emi), EMIAdapter.OnClickListener,
    EMIMenuItems.OnItemClickListener {

    private var _binding: FragmentEmiBinding? = null
    private val binding get() = _binding!!

    private val emiViewModel by viewModels<EMIViewModel>()

    private lateinit var emiAdapter: EMIAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmiBinding.bind(view)

        initListeners()

        emiAdapter = EMIAdapter()

        setProgressBarVisibility(true)

        lifecycleScope.launch {

            delay(200)
            getEMIs()
        }

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        binding.emiRV.apply {

            adapter = emiAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        emiAdapter.setOnClickListener(this)
    }

    override fun onItemClick(emi: EMI) {

        //TODO("Not yet implemented")
    }

    private lateinit var emiForMenuItems: EMI

    override fun onMenuBtnClicked(emi: EMI, position: Int) {

        emiForMenuItems = emi

        requireActivity().supportFragmentManager.let {

            EMIMenuItems.newInstance(
                null
            ).apply {
                show(it, "emi_menu_bottomsheet_tag")
            }.setOnClickListener(this)
        }
    }

    override fun onSyncBtnClicked(emi: EMI, position: Int) {
        //TODO("Not yet implemented")
    }

    override fun onEditMenuClick() {

        if (this::emiForMenuItems.isInitialized) {

            requireContext().showToast(emiForMenuItems.emiName)
            //TODO("Not yet implemented")
        }
    }

    override fun onDeleteMenuClick() {
        //TODO("Not yet implemented")
    }

    override fun onSupportingDocumentMenuClick() {
        //TODO("Not yet implemented")
    }

    private fun getEMIs() {

        emiViewModel.getAllEMIs().observe(viewLifecycleOwner, { emiList ->

            Log.d(TAG, "getEMIs: $emiList")

            if (emiList.isEmpty()) {

                setNoEMIAddedMessageTVVisibility(true)
            } else {

                setNoEMIAddedMessageTVVisibility(false)
            }

            emiAdapter.submitList(emiList)

            setUpSearchView(emiList)

            setProgressBarVisibility(false)
        })
    }

    private fun setUpSearchView(emiList: List<EMI>?) {

        val searchView =
            binding.emiFragmentToolbar.menu.findItem(R.id.menu_search_home).actionView as SearchView

        searchView.searchText { s ->

            if (s?.trim()?.isEmpty()!!) {

                binding.emiRV.scrollToPosition(0)
                emiAdapter.submitList(emiList)
            } else {

                val filteredList = emiList?.filter { emi ->

                    emi.emiName.lowercase().contains(s.lowercase().trim())
                }

                emiAdapter.submitList(filteredList)

            }

        }

    }

    private fun initListeners() {

        binding.emiFragmentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addEmiCategoryFAB.setOnClickListener {

            findNavController().navigate(R.id.action_emiFragment_to_addEditEMIFragment)
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
