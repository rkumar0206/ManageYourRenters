package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.RenterTypeAdapter
import com.rohitthebest.manageyourrenters.data.RenterTypes
import com.rohitthebest.manageyourrenters.databinding.FragmentTrackMoneyHomeBinding
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "TrackMoneyHomeFragment"

@AndroidEntryPoint
class TrackMoneyHomeFragment : Fragment(R.layout.fragment_track_money_home),
    RenterTypeAdapter.OnClickListener {

    private var _binding: FragmentTrackMoneyHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var trackMoneyTypeList: ArrayList<RenterTypes>

    // here RenterTypeAdapter is used as the logic and the layout is same in both trackType and ranter type
    private lateinit var trackMoneyTypeAdapter: RenterTypeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTrackMoneyHomeBinding.bind(view)

        populateTrackMoneyTypeList()
        trackMoneyTypeAdapter = RenterTypeAdapter()
        setUpRecyclerView()
        trackMoneyTypeAdapter.submitList(trackMoneyTypeList)


        binding.trackMoneyHomeToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        getMessage()
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args: TrackMoneyHomeFragmentArgs by navArgs()

            val fragmentName = args.shortcutFragmentNameKey

            Log.d(TAG, "getMessage: $fragmentName")

            arguments = Bundle.EMPTY

            if (fragmentName.isValid()) {

                when (fragmentName) {

                    getString(R.string.expenses) -> {

                        findNavController().navigate(R.id.action_trackMoneyHomeFragment_to_expenseCategoryFragment)
                    }

                    getString(R.string.emis) -> {

                        findNavController().navigate(R.id.action_trackMoneyHomeFragment_to_emiFragment)
                    }

                    getString(R.string.monthly_payments) -> {

                        findNavController().navigate(R.id.action_trackMoneyHomeFragment_to_monthlyPaymentCategoryFragment)
                    }

                    else -> {

                        Log.d(TAG, "getMessage: Not mapped")

                        showToast(requireContext(), "Not available")
                        requireActivity().onBackPressed()
                    }
                }
            }
        }
    }

    private fun populateTrackMoneyTypeList() {

        trackMoneyTypeList = ArrayList()

        trackMoneyTypeList.add(
            RenterTypes(
                id = 1,
                renterType = getString(R.string.emi),
                image = R.drawable.ic_emi
            )
        )

        trackMoneyTypeList.add(
            RenterTypes(
                id = 2,
                renterType = getString(R.string.loan),
                image = R.drawable.ic_loan
            )
        )

        trackMoneyTypeList.add(
            RenterTypes(
                id = 3,
                renterType = getString(R.string.contract_deals),
                image = R.drawable.ic_deal_contract
            )
        )

        trackMoneyTypeList.add(
            RenterTypes(
                id = 4,
                renterType = getString(R.string.expenses),
                image = R.drawable.ic_expense
            )
        )

        trackMoneyTypeList.add(
            RenterTypes(
                id = 5,
                renterType = getString(R.string.monthly_payments),
                image = R.drawable.ic_monthly_payments
            )
        )
    }

    private fun setUpRecyclerView() {

        binding.trackMoneyHomeRV.apply {

            setHasFixedSize(true)
            adapter = trackMoneyTypeAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }

        trackMoneyTypeAdapter.setOnClickListener(this)
    }


    override fun onItemClick(renterType: RenterTypes) {

        when (renterType.id) {

            1 -> {

                findNavController().navigate(R.id.action_trackMoneyHomeFragment_to_emiFragment)
            }

            2 -> {

                showToast(requireContext(), renterType.renterType)
            }

            4 -> {

                findNavController().navigate(R.id.action_trackMoneyHomeFragment_to_expenseCategoryFragment)
            }

            5 -> {

                findNavController().navigate(R.id.action_trackMoneyHomeFragment_to_monthlyPaymentCategoryFragment)
            }

            else -> {

                showToast(requireContext(), renterType.renterType)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
