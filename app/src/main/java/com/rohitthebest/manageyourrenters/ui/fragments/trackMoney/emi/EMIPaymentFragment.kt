package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.emiAdapters.EMIPaymentAdapter
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.fromEMIPaymentToString
import com.rohitthebest.manageyourrenters.utils.uploadDocumentToFireStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "EMIPaymentFragment"

@AndroidEntryPoint
class EMIPaymentFragment : Fragment(R.layout.fragment_emi_payment),
    EMIPaymentAdapter.OnClickListener, EMIMenuItems.OnItemClickListener {

    private var _binding: FragmentEmiPaymentBinding? = null
    private val binding get() = _binding!!

    private val emiPaymentViewModel by viewModels<EMIPaymentViewModel>()
    private val emiViewModel by viewModels<EMIViewModel>()

    private var receivedEMIKey = ""
    private lateinit var receivedEMI: EMI

    private lateinit var emiPaymentAdapter: EMIPaymentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmiPaymentBinding.bind(view)

        emiPaymentAdapter = EMIPaymentAdapter()

        shouldShowProgressBar(true)

        getMessage()

        initListeners()

        setUpRecyclerView()
    }



    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let {

                EMIPaymentFragmentArgs.fromBundle(it)
            }

            receivedEMIKey = args?.emiKeyMessage!!

            lifecycleScope.launch {

                delay(300)
                getEMI()
            }
        }
    }

    private fun getEMI() {

        emiViewModel.getEMIByKey(receivedEMIKey).observe(viewLifecycleOwner, { emi ->

            receivedEMI = emi

            getEMIPayments()
        })
    }

    private fun getEMIPayments() {

        emiPaymentViewModel.getAllEMIPaymentsByEMIKey(receivedEMIKey).observe(viewLifecycleOwner, {

            if (it.isEmpty()) {

                shouldShowNoEMIPaymentAddedTV(true)
            } else {

                shouldShowNoEMIPaymentAddedTV(false)
            }

            if (this::receivedEMI.isInitialized) {

                val totalEMI = receivedEMI.totalMonths * receivedEMI.amountPaidPerMonth
                val leftEMIAmount = totalEMI - receivedEMI.amountPaid
                binding.emiPaymentToolbar.subtitle =
                    "Due: ${receivedEMI.currencySymbol} $leftEMIAmount"
            }

            emiPaymentAdapter.submitList(it)

            shouldShowProgressBar(false)
        })
    }

    private fun setUpRecyclerView() {

        binding.emiPaymentsRV.apply {

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = emiPaymentAdapter
        }

        emiPaymentAdapter.setOnClickListener(this)
    }

    private lateinit var emiPaymentForMenus: EMIPayment

    override fun onMenuBtnClicked(emiPayment: EMIPayment, position: Int) {

        emiPaymentForMenus = emiPayment

        requireActivity().supportFragmentManager.let { fm ->

            EMIMenuItems.newInstance(
                null
            ).apply {

                show(fm, TAG)
            }.setOnClickListener(this)
        }

    }

    override fun onSyncBtnClicked(emiPayment: EMIPayment, position: Int) {

        if (emiPayment.isSynced) {

            showToast(
                requireContext(),
                "Already synced!!"
            )
        } else {

            if (isInternetAvailable(requireContext())) {

                emiPayment.isSynced = true

                uploadDocumentToFireStore(
                    requireContext(),
                    fromEMIPaymentToString(emiPayment),
                    getString(R.string.emiPayments),
                    emiPayment.key
                )

                emiPaymentViewModel.updateEMIPayment(emiPayment)

                emiPaymentAdapter.notifyItemChanged(position)
            } else {

                showNoInternetMessage(requireContext())
            }
        }
    }

    //[START OF MENUS]
    override fun onEditMenuClick() {
        //TODO("Not yet implemented")
    }

    override fun onDeleteMenuClick() {
        //TODO("Not yet implemented")
    }

    override fun onViewSupportingDocumentMenuClick() {
        //TODO("Not yet implemented")
    }

    override fun onReplaceSupportingDocumentClick() {
        //TODO("Not yet implemented")
    }

    override fun onDeleteSupportingDocumentClick() {
        //TODO("Not yet implemented")
    }
    //[END OF MENUS]

    private fun initListeners() {

        binding.emiPaymentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.emiPaymentToolbar.menu.findItem(R.id.menu_show_emi_details)
            .setOnMenuItemClickListener {

                val action =
                    EMIPaymentFragmentDirections.actionEMIPaymentFragmentToEmiDetailsBottomSheetFragment(
                        receivedEMIKey
                    )
                findNavController().navigate(action)

                true
            }

        binding.addEmiPaymentFAB.setOnClickListener {

            val action =
                EMIPaymentFragmentDirections.actionEMIPaymentFragmentToAddEmiPaymentFragment(
                    receivedEMIKey
                )
            findNavController().navigate(action)
        }
    }


    private fun shouldShowProgressBar(isVisible: Boolean) {

        binding.progressBar.isVisible = isVisible
    }

    private fun shouldShowNoEMIPaymentAddedTV(isVisible: Boolean) {

        binding.noEmiPaymentAddedMessageTV.isVisible = isVisible
        binding.emiPaymentsRV.isVisible = !isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
