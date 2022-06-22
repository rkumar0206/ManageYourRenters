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
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.SHOW_DOCUMENTS_MENU
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
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
    EMIPaymentAdapter.OnClickListener, CustomMenuItems.OnItemClickListener,
    SupportingDocumentDialogFragment.OnBottomSheetDismissListener {

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
    private var adapterPosition = -1

    override fun onItemClick(emiPayment: EMIPayment) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.payment_info))
            .setMessage(
                emiPaymentViewModel.buildEMIPaymentInfoStringForAlertDialogMessage(
                    emiPayment,
                    receivedEMI
                )
            )
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()

    }

    override fun onMenuButtonBtnClicked(emiPayment: EMIPayment, position: Int) {

        emiPaymentForMenus = emiPayment
        adapterPosition = position

        requireActivity().supportFragmentManager.let {

            val bundle = Bundle()

            bundle.putBoolean(Constants.SHOW_SYNC_MENU, !emiPayment.isSynced)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, position == 0)
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, false)
            bundle.putBoolean(SHOW_DOCUMENTS_MENU, true)

            CustomMenuItems.newInstance(
                bundle
            ).apply {
                show(it, TAG)
            }.setOnClickListener(this)
        }

    }

    //[START OF MENUS]
    override fun onEditMenuClick() {
        // not visible
    }

    override fun onDeleteMenuClick() {

        if (adapterPosition == 0) {

            showAlertDialogForDeletion(
                requireContext(),
                { dialog ->

                    if (isInternetAvailable(requireContext())) {

                        emiPaymentViewModel.deleteEMIPayment(emiPaymentForMenus)
                    } else {

                        showNoInternetMessage(requireContext())
                    }

                    dialog.dismiss()
                },
                { dialog ->

                    dialog.dismiss()
                }
            )

        } else {

            showToast(requireContext(), "You cannot delete this payment!!!!")
            emiPaymentAdapter.notifyItemChanged(adapterPosition)
        }

    }

    override fun onViewSupportingDocumentMenuClick() {

        if (::emiPaymentForMenus.isInitialized && checkSupportingDocumentValidation()) {

            emiPaymentForMenus.supportingDocument?.let { supportingDoc ->

                Functions.onViewOrDownloadSupportingDocument(
                    requireActivity(),
                    supportingDoc
                )
            }

        }
    }

    private fun checkSupportingDocumentValidation(): Boolean {

        if (!emiPaymentForMenus.isSupportingDocAdded) {

            showToast(requireContext(), getString(R.string.no_supporting_doc_added))
            return false
        } else if (emiPaymentForMenus.isSupportingDocAdded && emiPaymentForMenus.supportingDocument == null) {

            showToast(requireContext(), getString(R.string.uploading_doc_progress_message))
            return false
        }

        return true
    }

    override fun onReplaceSupportingDocumentClick() {

        if (isInternetAvailable(requireContext())) {
            if (::emiPaymentForMenus.isInitialized) {

                val supportingDocumentHelperModel = SupportingDocumentHelperModel()
                supportingDocumentHelperModel.modelName = getString(R.string.emiPayments)

                showSupportDocumentBottomSheetDialog(supportingDocumentHelperModel)
            }
        } else {

            showNoInternetMessage(requireContext())
        }

    }

    private fun showSupportDocumentBottomSheetDialog(supportingDocmtHelperModel: SupportingDocumentHelperModel) {

        val bundle = Bundle()
        bundle.putString(
            Constants.SUPPORTING_DOCUMENT_HELPER_MODEL_KEY,
            supportingDocmtHelperModel.convertToJsonString()
        )

        requireActivity().supportFragmentManager.let {

            SupportingDocumentDialogFragment.newInstance(bundle)
                .apply {
                    show(it, TAG)
                }.setOnBottomSheetDismissListener(this)
        }
    }

    override fun onBottomSheetDismissed(
        isDocumentAdded: Boolean,
        supportingDocumentHelperModel: SupportingDocumentHelperModel
    ) {

        if (isInternetAvailable(requireContext())) {
            if (isDocumentAdded) {

                // call the viewmodel method for adding or replacing the document
                emiPaymentViewModel.addOrReplaceBorrowerSupportingDocument(
                    emiPaymentForMenus,
                    supportingDocumentHelperModel
                )
            }
        } else {

            showNoInternetMessage(requireContext())
        }
    }

    override fun onDeleteSupportingDocumentClick() {

        if (::emiPaymentForMenus.isInitialized
            && checkSupportingDocumentValidation()
            && emiPaymentForMenus.supportingDocument != null
        ) {
            showAlertDialogForDeletion(
                requireContext(),
                {
                    if (emiPaymentForMenus.supportingDocument?.documentType != DocumentType.URL) {

                        if (!isInternetAvailable(requireContext())) {

                            showToast(
                                requireContext(),
                                getString(R.string.network_required_for_deleting_file_from_cloud)
                            )
                            return@showAlertDialogForDeletion
                        } else {

                            deleteFileFromFirebaseStorage(
                                requireContext(),
                                emiPaymentForMenus.supportingDocument?.documentUrl!!
                            )
                        }
                        }

                        val emiPayment = emiPaymentForMenus.copy()

                        emiPayment.modified = System.currentTimeMillis()
                        emiPayment.supportingDocument = null
                        emiPayment.isSupportingDocAdded = false

                        emiPaymentViewModel.updateEMIPayment(
                            emiPaymentForMenus,
                            emiPayment
                        )
                        showToast(requireContext(), getString(R.string.supporting_document_deleted))
                        emiPaymentAdapter.notifyItemChanged(adapterPosition)
                        it.dismiss()
                    },
                    {
                        it.dismiss()
                    }
                )
            } else {
                showToast(requireContext(), getString(R.string.no_supporting_doc_added))
            }

    }

    override fun onSyncMenuClick() {

        if (emiPaymentForMenus.isSynced) {

            showToast(
                requireContext(),
                "Already synced!!"
            )
        } else {

            if (isInternetAvailable(requireContext())) {

                // inserting as update is not allowed for emiPayment
                emiPaymentViewModel.updateEMIPayment(emiPaymentForMenus, emiPaymentForMenus)
                emiPaymentAdapter.notifyItemChanged(adapterPosition)
            } else {

                showNoInternetMessage(requireContext())
            }
        }

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

        emiPaymentViewModel.saveEmiPaymentRvState(
            binding.emiPaymentsRV.layoutManager?.onSaveInstanceState()
        )

        _binding = null
    }

}
