package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.emiAdapters.EMIPaymentAdapter
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.SHOW_DOCUMENTS_MENU
import com.rohitthebest.manageyourrenters.ui.fragments.AddSupportingDocumentBottomSheetFragment
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "EMIPaymentFragment"

@AndroidEntryPoint
class EMIPaymentFragment : Fragment(R.layout.fragment_emi_payment),
    EMIPaymentAdapter.OnClickListener, CustomMenuItems.OnItemClickListener {

    private var _binding: FragmentEmiPaymentBinding? = null
    private val binding get() = _binding!!

    private val emiPaymentViewModel by viewModels<EMIPaymentViewModel>()
    private val emiViewModel by viewModels<EMIViewModel>()

    private var receivedEMIKey = ""
    private lateinit var receivedEMI: EMI

    private lateinit var emiPaymentAdapter: EMIPaymentAdapter

    private var rvStateParcelable: Parcelable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmiPaymentBinding.bind(view)

        emiPaymentAdapter = EMIPaymentAdapter()

        shouldShowProgressBar(true)

        getMessage()

        initListeners()

        setUpRecyclerView()

        getRvState()
    }

    private fun getRvState() {

        emiPaymentViewModel.emiPaymentRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }

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

        emiViewModel.getEMIByKey(receivedEMIKey).observe(viewLifecycleOwner) { emi ->

            receivedEMI = emi

            binding.emiPaymentToolbar.title = emi.emiName

            getEMIPayments()
        }
    }

    private fun getEMIPayments() {

        emiPaymentViewModel.getAllEMIPaymentsByEMIKey(receivedEMIKey).observe(viewLifecycleOwner) {

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
            binding.emiPaymentsRV.layoutManager?.onRestoreInstanceState(rvStateParcelable)

            shouldShowProgressBar(false)
        }
    }

    private fun setUpRecyclerView() {

        binding.emiPaymentsRV.apply {

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = emiPaymentAdapter
            changeVisibilityOfFABOnScrolled(binding.addEmiPaymentFAB)
        }

        emiPaymentAdapter.setOnClickListener(this)
    }

    private lateinit var emiPaymentForMenus: EMIPayment

    override fun onEMIDocumentBtnClicked(emiPayment: EMIPayment, position: Int) {

        emiPaymentForMenus = emiPayment

        requireActivity().supportFragmentManager.let { fm ->

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, false)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, false)
            bundle.putBoolean(SHOW_DOCUMENTS_MENU, true)

            CustomMenuItems.newInstance(
                bundle
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

    override fun onEMIPaymentMessageBtnClicked(message: String) {

        if (!message.isValid()) {

            showToast(requireContext(), "No message or note added!!!")
        } else {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Message / Note")
                .setMessage(message)
                .setPositiveButton("Ok") { dialog, _ ->

                    dialog.dismiss()
                }
                .create()
                .show()
        }

    }

    override fun onDeleteEMIPaymentBtnClicked(emiPayment: EMIPayment, position: Int) {

        if (position == 0) {

            showAlertDialogForDeletion(
                requireContext(),
                { dialog ->

                    if (!emiPayment.isSynced || !emiPayment.isSupportingDocAdded) {

                        emiPaymentViewModel.deleteEMIPayment(
                            requireContext(),
                            emiPayment
                        )
                    } else {

                        if (isInternetAvailable(requireContext())) {

                            emiPaymentViewModel.deleteEMIPayment(
                                requireContext(),
                                emiPayment
                            )

                        } else {

                            showNoInternetMessage(requireContext())
                        }
                    }

                    dialog.dismiss()
                },
                { dialog ->

                    dialog.dismiss()
                }
            )

        } else {

            showToast(requireContext(), "You cannot delete this payment!!!!")
            emiPaymentAdapter.notifyItemChanged(position)
        }
    }

    //[START OF MENUS]
    override fun onEditMenuClick() {
        // not visible
    }

    override fun onDeleteMenuClick() {

        // not visible
    }

    override fun onViewSupportingDocumentMenuClick() {

        if (this::emiPaymentForMenus.isInitialized) {

            if (!emiPaymentForMenus.isSupportingDocAdded) {

                requireContext().showToast(getString(R.string.no_supporting_doc_added))
            } else {

                emiPaymentForMenus.supportingDocument?.let { supportingDoc ->

                    Functions.onViewOrDownloadSupportingDocument(
                        requireActivity(),
                        supportingDoc
                    )
                }
            }
        }

    }

    override fun onReplaceSupportingDocumentClick() {

        requireActivity().supportFragmentManager.let {

            val bundle = Bundle()
            bundle.putString(Constants.COLLECTION_TAG_KEY, getString(R.string.emiPayments))
            bundle.putString(Constants.DOCUMENT_KEY, fromEMIPaymentToString(emiPaymentForMenus))
            bundle.putBoolean(Constants.IS_DOCUMENT_FOR_EDITING_KEY, true)

            AddSupportingDocumentBottomSheetFragment.newInstance(
                bundle
            ).apply {
                show(it, "AddSupportingDocTag")
            }
        }
    }

    override fun onDeleteSupportingDocumentClick() {

        if (this::emiPaymentForMenus.isInitialized) {

            if (isInternetAvailable(requireContext())) {

                if (emiPaymentForMenus.isSupportingDocAdded) {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Are you sure?")
                        .setMessage("After deleting you cannot retrieve it again.")
                        .setPositiveButton("Yes") { dialog, _ ->

                            if (emiPaymentForMenus.supportingDocument?.documentType != DocumentType.URL) {

                                deleteFileFromFirebaseStorage(
                                    requireContext(),
                                    emiPaymentForMenus.supportingDocument?.documentUrl!!
                                )

                            }

                            emiPaymentForMenus.isSupportingDocAdded = false
                            emiPaymentForMenus.supportingDocument = null

                            val map = HashMap<String, Any?>()
                            map["supportingDocumentAdded"] = false
                            map["supportingDocument"] = null

                            updateDocumentOnFireStore(
                                requireContext(),
                                map,
                                getString(R.string.emiPayments),
                                emiPaymentForMenus.key
                            )

                            emiPaymentViewModel.updateEMIPayment(emiPaymentForMenus)

                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->

                            dialog.dismiss()
                        }
                        .create()
                        .show()

                } else {

                    requireContext().showToast(
                        "No supporting document added!!!"
                    )
                }
            } else {

                showNoInternetMessage(requireContext())
            }
        }

    }

    override fun onSyncMenuClick() {}
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

        emiPaymentViewModel.saveEmiPaymentRvState(
            binding.emiPaymentsRV.layoutManager?.onSaveInstanceState()
        )

        _binding = null
    }

}
