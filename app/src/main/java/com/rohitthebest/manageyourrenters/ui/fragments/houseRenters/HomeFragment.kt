package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters.ShowRentersAdapter
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentHomeBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getMillisecondsOfStartAndEndUsingConstants
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getPairOfDateInMillisInStringInDateString
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showDateRangePickerDialog
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showMobileNumberOptionMenu
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

//private const val TAG = "HomeFragment"

@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener, ShowRentersAdapter.OnClickListener,
    MenuItem.OnMenuItemClickListener {

    private val renterViewModel: RenterViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var mAuth: FirebaseAuth? = null

    private lateinit var mAdapter: ShowRentersAdapter

    private var rvStateParcelable: Parcelable? = null

    private lateinit var workingWithDateAndTime: WorkingWithDateAndTime

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

        workingWithDateAndTime = WorkingWithDateAndTime()

        setupRecyclerView()

        getRvState()

        lifecycleScope.launch {

            delay(320)

            getAllRentersList()
        }

        initListeners()
    }

    private fun getRvState() {

        renterViewModel.renterRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }

    }

    private fun getAllRentersList() {

        try {

            renterViewModel.getAllRentersList().observe(viewLifecycleOwner) { renters ->

                if (renters.isNotEmpty()) {

                    hideNoRentersAddedTV()
                    setUpSearchEditText(renters)
                } else {

                    showNoRentersAddedTV()
                }

                mAdapter.submitList(renters)
                binding.rentersRV.layoutManager?.onRestoreInstanceState(rvStateParcelable)

                binding.homeProgressBar.hide()
            }
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

                renterViewModel.updateRenter(requireContext(), renter)
            }

        } else {

            showNoInternetMessage(requireContext())
        }
    }

    override fun onDeleteClicked(renter: Renter) {

        showAlertDialogForDeletion(
            requireContext(),
            {

                if (isInternetAvailable(requireContext())) {

                    renterViewModel.deleteRenter(requireContext(), renter)
                } else {
                    showNoInternetMessage(requireContext())
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

        binding.houseRentersHomeToolBar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        handleMenuClickListener()

    }

    private fun handleMenuClickListener() {

        binding.houseRentersHomeToolBar.menu.findItem(R.id.menu_total_number_of_renters)
            .setOnMenuItemClickListener(this)
        binding.houseRentersHomeToolBar.menu.findItem(R.id.menu_show_deleted_renters)
            .setOnMenuItemClickListener(this)
        binding.houseRentersHomeToolBar.menu.findItem(R.id.menu_renter_revenue_all_time)
            .setOnMenuItemClickListener(this)
        binding.houseRentersHomeToolBar.menu.findItem(R.id.menu_renter_revenue_this_month)
            .setOnMenuItemClickListener(this)
        binding.houseRentersHomeToolBar.menu.findItem(R.id.menu_renter_revenue_previous_month)
            .setOnMenuItemClickListener(this)
        binding.houseRentersHomeToolBar.menu.findItem(R.id.menu_renter_revenue_this_week)
            .setOnMenuItemClickListener(this)
        binding.houseRentersHomeToolBar.menu.findItem(R.id.menu_renter_revenue_previous_week)
            .setOnMenuItemClickListener(this)
        binding.houseRentersHomeToolBar.menu.findItem(R.id.menu_renter_revenue_custom_range)
            .setOnMenuItemClickListener(this)

    }

    var d1 = System.currentTimeMillis() - (30 * Constants.ONE_DAY_MILLISECONDS)
    var d2 = System.currentTimeMillis()

    override fun onMenuItemClick(menu: MenuItem?): Boolean {

        return when (menu?.itemId) {

            R.id.menu_total_number_of_renters -> {

                renterViewModel.getRenterCount().observe(viewLifecycleOwner) { count ->

                    showAlertDialogWithTitleAndMessage(
                        "Total renters",
                        "You have $count renters living in your apartment."
                    )
                }

                true
            }
            R.id.menu_show_deleted_renters -> {

                findNavController().navigate(R.id.action_homeFragment_to_deletedRentersFragment)
                true
            }

            R.id.menu_renter_revenue_this_month -> {

                showRevenueMessageByDateRange(
                    CustomDateRange.THIS_MONTH,
                    getMillisecondsOfStartAndEndUsingConstants(CustomDateRange.THIS_MONTH)
                )

                true
            }

            R.id.menu_renter_revenue_previous_month -> {

                showRevenueMessageByDateRange(
                    CustomDateRange.PREVIOUS_MONTH,
                    getMillisecondsOfStartAndEndUsingConstants(CustomDateRange.PREVIOUS_MONTH)
                )

                true
            }

            R.id.menu_renter_revenue_this_week -> {

                showRevenueMessageByDateRange(
                    CustomDateRange.THIS_WEEK,
                    getMillisecondsOfStartAndEndUsingConstants(CustomDateRange.THIS_WEEK)
                )

                true
            }

            R.id.menu_renter_revenue_previous_week -> {

                showRevenueMessageByDateRange(
                    CustomDateRange.PREVIOUS_WEEK,
                    getMillisecondsOfStartAndEndUsingConstants(CustomDateRange.PREVIOUS_WEEK)
                )

                true
            }

            R.id.menu_renter_revenue_custom_range -> {

                showDateRangePickerDialog(
                    d1,
                    d2,
                    { requireActivity().supportFragmentManager },
                    { millis ->

                        d1 = millis.first
                        d2 = millis.second
                        showRevenueMessageByDateRange(
                            CustomDateRange.CUSTOM_DATE_RANGE,
                            Pair(millis.first, millis.second)
                        )
                    }
                )

                true
            }

            R.id.menu_renter_revenue_all_time -> {

                renterViewModel.getRentersWithTheirAmountPaid()
                    .observe(viewLifecycleOwner) { renterNameWithTheirAmountPaid ->

                        showRevenueMessageInAlertDialogBox(
                            "Revenue - All Time",
                            renterNameWithTheirAmountPaid
                        )
                    }
                true
            }

            else -> false
        }
    }

    private fun showRevenueMessageByDateRange(
        customDateRange: CustomDateRange,
        millis: Pair<Long, Long>
    ) {

        renterViewModel.getRentersWithTheirAmountPaidByDateCreated(
            millis.first,
            millis.second
        ).observe(viewLifecycleOwner) { renterNameWithTheirAmountPaid ->

            showRevenueMessageInAlertDialogBox(
                "Revenue - $customDateRange (${getPairOfDateInMillisInStringInDateString(millis)})",
                renterNameWithTheirAmountPaid
            )

        }

    }

    private fun showRevenueMessageInAlertDialogBox(
        title: String,
        renterNameWithTheirAmountPaid: Map<String, List<Double>>
    ) {

        var totalAmountPaid = 0.0

        val message = StringBuilder()

        renterNameWithTheirAmountPaid.forEach { entry ->

            totalAmountPaid += entry.value.sum()

            message.append("${entry.key}  ===>  ${entry.value.sum().format(2)}\n\n")
        }

        message.append("----------------------------------------\n\n")
        message.append("     Total :   ${totalAmountPaid.format(2)}")

        showAlertDialogWithTitleAndMessage(
            title,
            message.toString()
        )
    }

    private fun showAlertDialogWithTitleAndMessage(title: String, message: String) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()
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

        renterViewModel.saveRenterRvState(binding.rentersRV.layoutManager?.onSaveInstanceState())

        hideKeyBoard(requireActivity())

        _binding = null
    }

}