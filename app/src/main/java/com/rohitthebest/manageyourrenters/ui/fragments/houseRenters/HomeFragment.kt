package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters.ShowRentersAdapter
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentHomeBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showMobileNumberOptionMenu
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

//private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener, ShowRentersAdapter.OnClickListener {

    private val renterViewModel: RenterViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var mAuth: FirebaseAuth? = null

    private lateinit var mAdapter: ShowRentersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = Firebase.auth

        mAdapter = ShowRentersAdapter()

        binding.homeProgressBar.show()

        setupRecyclerView()

        lifecycleScope.launch {

            delay(400)

            getAllRentersList()
        }

        initListeners()
    }

    private fun getAllRentersList() {

        try {

            renterViewModel.getAllRentersList().observe(viewLifecycleOwner, {

                if (it.isNotEmpty()) {

                    hideNoRentersAddedTV()
                    setUpSearchEditText(it)
                } else {

                    showNoRentersAddedTV()
                }

                mAdapter.submitList(it)

                binding.homeProgressBar.hide()
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpSearchEditText(it: List<Renter>?) {

        val searchView =
            binding.houseRentersHomeToolBar.menu.findItem(R.id.menu_search_home).actionView as SearchView

        searchView.searchText { s ->

            if (s?.isEmpty()!!) {

                binding.rentersRV.scrollToPosition(0)
                mAdapter.submitList(it)
            } else {

                val filteredList = it?.filter { renter ->

                    renter.name.lowercase(Locale.ROOT).contains(
                        s.toString().trim().lowercase(Locale.ROOT)
                    )
                            ||
                            renter.roomNumber.lowercase(Locale.ROOT).contains(
                                s.toString().trim().lowercase(Locale.ROOT)
                            )
                }

                mAdapter.submitList(filteredList)

            }

        }
    }

    private fun setupRecyclerView() {

        try {

            binding.rentersRV.apply {

                adapter = mAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                changeVisibilityOfFABOnScrolled(binding.addRenterFAB)
            }


            mAdapter.setOnClickListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRenterClicked(renter: Renter) {

        val action = HomeFragmentDirections.actionHomeFragmentToPaymentFragment(
            renter.key
            //convertRenterToJSONString(renter)
        )
        findNavController().navigate(action)
    }

    override fun onSyncButtonClicked(renter: Renter) {

        if (isInternetAvailable(requireContext())) {

            if (renter.isSynced == getString(R.string.t)) {

                showToast(requireContext(), "Already Synced")
            } else {

                renter.isSynced = getString(R.string.t)

                uploadDocumentToFireStore(
                    requireContext(),
                    getString(R.string.renters),
                    renter.key!!
                )

                renterViewModel.insertRenter(renter)
            }

        } else {

            showNoInternetMessage(requireContext())
        }
    }

    override fun onDeleteClicked(renter: Renter) {

        showAlertDialogForDeletion(
            requireContext(),
            {

                if (renter.isSynced == getString(R.string.f)) {

                    renterViewModel.deleteRenter(requireContext(), renter)
                } else {

                    if (isInternetAvailable(requireContext())) {

                        renterViewModel.deleteRenter(requireContext(), renter)
                    } else {
                        showNoInternetMessage(requireContext())
                    }
                }

                it.dismiss()
            },
            {

                it.dismiss()
            }
        )
    }

    override fun onEditClicked(renter: Renter) {

        val action = HomeFragmentDirections.actionHomeFragmentToAddRenterFragment(
            convertRenterToJSONString(renter)
        )

        findNavController().navigate(action)
    }

    override fun onMobileNumberClicked(mobileNumber: String, view: View) {

        showMobileNumberOptionMenu(requireActivity(), view, mobileNumber)
    }

    private fun hideNoRentersAddedTV() {

        try {

            binding.rentersRV.show()
            binding.noRentersAddedTV.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun showNoRentersAddedTV() {

        try {

            binding.rentersRV.hide()
            binding.noRentersAddedTV.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initListeners() {

        binding.addRenterFAB.setOnClickListener(this)

        binding.rentersRV.changeVisibilityOfFABOnScrolled(
            binding.addRenterFAB
        )

        binding.houseRentersHomeToolBar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addRenterFAB.id -> {

                findNavController().navigate(R.id.action_homeFragment_to_addRenterFragment)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        try {

            hideKeyBoard(requireActivity())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }

}